package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SchedulerService Tests")
class SchedulerServiceTest {

    @Mock
    private SyncService syncService;

    private SchedulerService schedulerService;
    private static final int TEST_INTERVAL_MINUTES = 60;

    @BeforeEach
    void setUp() {
        schedulerService = new SchedulerService(syncService, TEST_INTERVAL_MINUTES);
    }

    @Test
    @DisplayName("Deve criar scheduler com intervalo configurado")
    void shouldCreateSchedulerWithConfiguredInterval() {
        // Assert
        assertEquals(TEST_INTERVAL_MINUTES, schedulerService.getIntervalMinutes());
        assertFalse(schedulerService.isRunning());
    }

    @Test
    @DisplayName("Deve iniciar o scheduler com sucesso")
    void shouldStartSchedulerSuccessfully() {
        // Act
        schedulerService.start();

        // Assert
        assertTrue(schedulerService.isRunning(), "Scheduler deveria estar rodando");
        assertEquals(TEST_INTERVAL_MINUTES, schedulerService.getIntervalMinutes());

        // Cleanup
        schedulerService.stop();
    }

    @Test
    @DisplayName("Não deve iniciar o scheduler se já estiver rodando")
    void shouldNotStartSchedulerIfAlreadyRunning() {
        // Arrange
        schedulerService.start();
        assertTrue(schedulerService.isRunning());

        // Act
        schedulerService.start(); // Tentativa de iniciar novamente

        // Assert
        assertTrue(schedulerService.isRunning());
        // Não deve lançar exceção nem criar múltiplos schedulers

        // Cleanup
        schedulerService.stop();
    }

    @Test
    @DisplayName("Deve parar o scheduler com sucesso")
    void shouldStopSchedulerSuccessfully() {
        // Arrange
        schedulerService.start();
        assertTrue(schedulerService.isRunning());

        // Act
        schedulerService.stop();

        // Assert
        assertFalse(schedulerService.isRunning(), "Scheduler deveria estar parado");
    }

    @Test
    @DisplayName("Não deve fazer nada ao parar scheduler que não está rodando")
    void shouldDoNothingWhenStoppingInactiveScheduler() {
        // Assert - não deve lançar exceção
        assertDoesNotThrow(() -> schedulerService.stop());
        assertFalse(schedulerService.isRunning());
    }

    @Test
    @DisplayName("Deve retornar null para lastRunAt antes da primeira execução")
    void shouldReturnNullForLastRunAtBeforeFirstExecution() {
        // Act
        LocalDateTime lastRunAt = schedulerService.getLastRunAt();

        // Assert
        assertNull(lastRunAt, "lastRunAt deveria ser null antes da primeira execução");
    }

    @Test
    @DisplayName("Deve retornar intervalo configurado")
    void shouldReturnConfiguredInterval() {
        // Act
        int interval = schedulerService.getIntervalMinutes();

        // Assert
        assertEquals(TEST_INTERVAL_MINUTES, interval);
    }

    @Test
    @DisplayName("Deve retornar status info quando inativo")
    void shouldReturnStatusInfoWhenInactive() {
        // Act
        String statusInfo = schedulerService.getStatusInfo();

        // Assert
        assertNotNull(statusInfo);
        assertTrue(statusInfo.contains("INATIVA"), "Status deveria indicar inativo");
        assertTrue(statusInfo.contains("Intervalo: " + TEST_INTERVAL_MINUTES + " minutos"));
        assertTrue(statusInfo.contains("Nunca executada"), "Deveria indicar que nunca foi executada");
    }

    @Test
    @DisplayName("Deve retornar status info quando ativo")
    void shouldReturnStatusInfoWhenActive() {
        // Arrange
        schedulerService.start();

        // Act
        String statusInfo = schedulerService.getStatusInfo();

        // Assert
        assertNotNull(statusInfo);
        assertTrue(statusInfo.contains("ATIVA"), "Status deveria indicar ativo");
        assertTrue(statusInfo.contains("Intervalo: " + TEST_INTERVAL_MINUTES + " minutos"));

        // Cleanup
        schedulerService.stop();
    }

    @Test
    @DisplayName("Deve criar scheduler com diferentes intervalos")
    void shouldCreateSchedulerWithDifferentIntervals() {
        // Arrange
        int interval1 = 5;
        int interval2 = 60;
        int interval3 = 120;

        // Act
        SchedulerService scheduler1 = new SchedulerService(syncService, interval1);
        SchedulerService scheduler2 = new SchedulerService(syncService, interval2);
        SchedulerService scheduler3 = new SchedulerService(syncService, interval3);

        // Assert
        assertEquals(interval1, scheduler1.getIntervalMinutes());
        assertEquals(interval2, scheduler2.getIntervalMinutes());
        assertEquals(interval3, scheduler3.getIntervalMinutes());

        assertFalse(scheduler1.isRunning());
        assertFalse(scheduler2.isRunning());
        assertFalse(scheduler3.isRunning());
    }

    @Test
    @DisplayName("Deve funcionar com start/stop múltiplas vezes")
    void shouldWorkWithMultipleStartStopCycles() {
        // Ciclo 1
        schedulerService.start();
        assertTrue(schedulerService.isRunning());
        schedulerService.stop();
        assertFalse(schedulerService.isRunning());

        // Ciclo 2
        schedulerService.start();
        assertTrue(schedulerService.isRunning());
        schedulerService.stop();
        assertFalse(schedulerService.isRunning());

        // Ciclo 3
        schedulerService.start();
        assertTrue(schedulerService.isRunning());
        schedulerService.stop();
        assertFalse(schedulerService.isRunning());
    }

    @Test
    @DisplayName("Status info deve refletir mudanças de estado")
    void statusInfoShouldReflectStateChanges() {
        // Estado inicial - inativo
        String inactiveStatus = schedulerService.getStatusInfo();
        assertTrue(inactiveStatus.contains("INATIVA"));
        assertTrue(inactiveStatus.contains("Nunca executada"));

        // Após iniciar - ativo
        schedulerService.start();
        String activeStatus = schedulerService.getStatusInfo();
        assertTrue(activeStatus.contains("ATIVA"));

        // Após parar - inativo novamente
        schedulerService.stop();
        String inactiveAgainStatus = schedulerService.getStatusInfo();
        assertTrue(inactiveAgainStatus.contains("INATIVA"));
    }

    @Test
    @DisplayName("Deve retornar false para isRunning em scheduler novo")
    void shouldReturnFalseForIsRunningOnNewScheduler() {
        // Arrange
        SchedulerService newScheduler = new SchedulerService(syncService, 30);

        // Assert
        assertFalse(newScheduler.isRunning());
    }

    @Test
    @DisplayName("Deve manter intervalo após start e stop")
    void shouldMaintainIntervalAfterStartAndStop() {
        // Act
        int intervalBefore = schedulerService.getIntervalMinutes();

        schedulerService.start();
        int intervalDuringRun = schedulerService.getIntervalMinutes();

        schedulerService.stop();
        int intervalAfterStop = schedulerService.getIntervalMinutes();

        // Assert
        assertEquals(TEST_INTERVAL_MINUTES, intervalBefore);
        assertEquals(TEST_INTERVAL_MINUTES, intervalDuringRun);
        assertEquals(TEST_INTERVAL_MINUTES, intervalAfterStop);
    }

    @Test
    @DisplayName("Status info deve conter todas as informações necessárias")
    void statusInfoShouldContainAllNecessaryInformation() {
        // Act
        String statusInfo = schedulerService.getStatusInfo();

        // Assert
        assertTrue(statusInfo.contains("Status:"), "Deve conter label 'Status:'");
        assertTrue(statusInfo.contains("Intervalo:"), "Deve conter label 'Intervalo:'");
        assertTrue(statusInfo.contains("minutos"), "Deve mencionar 'minutos'");
        assertTrue(statusInfo.contains("Última execução:"), "Deve conter label 'Última execução:'");
    }

    @Test
    @DisplayName("Deve permitir parar scheduler parado sem exceção")
    void shouldAllowStoppingStoppedSchedulerWithoutException() {
        // Arrange - scheduler já está parado
        assertFalse(schedulerService.isRunning());

        // Act & Assert
        assertDoesNotThrow(() -> {
            schedulerService.stop();
            schedulerService.stop();
            schedulerService.stop();
        });

        assertFalse(schedulerService.isRunning());
    }

    @Test
    @DisplayName("Deve permitir iniciar scheduler parado múltiplas vezes")
    void shouldAllowStartingStoppedSchedulerMultipleTimes() {
        // Teste 1
        schedulerService.start();
        assertTrue(schedulerService.isRunning());
        schedulerService.stop();
        assertFalse(schedulerService.isRunning());

        // Teste 2
        schedulerService.start();
        assertTrue(schedulerService.isRunning());
        schedulerService.stop();
        assertFalse(schedulerService.isRunning());
    }
}