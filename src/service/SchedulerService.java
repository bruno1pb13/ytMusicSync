package service;

import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Serviço responsável por agendar verificações periódicas.
 * Thread-safe e gerencia lifecycle do scheduler.
 */
public class SchedulerService {
    private final SyncService syncService;
    private final int intervalMinutes;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledTask;
    private volatile boolean running = false;
    private volatile LocalDateTime lastRunAt = null;

    public SchedulerService(SyncService syncService, int intervalMinutes) {
        this.syncService = syncService;
        this.intervalMinutes = intervalMinutes;
    }

    public synchronized void start() {
        if (running) {
            System.out.println("Scheduler já está rodando");
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "SyncScheduler");
            thread.setDaemon(true);
            return thread;
        });

        Runnable task = () -> {
            try {
                lastRunAt = LocalDateTime.now();
                String timestamp = lastRunAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                System.out.println("\n[" + timestamp + "] Iniciando sincronização automática...");
                syncService.syncAllPlaylists();
            } catch (Exception e) {
                System.err.println("Erro na sincronização automática: " + e.getMessage());
                e.printStackTrace();
            }
        };

        // Agenda com delay inicial de 1 minuto e depois a cada intervalo configurado
        scheduledTask = scheduler.scheduleAtFixedRate(
                task,
                1,
                intervalMinutes,
                TimeUnit.MINUTES
        );

        running = true;
        System.out.println("✓ Verificações automáticas iniciadas (intervalo: " + intervalMinutes + " minutos)");
    }

    public synchronized void stop() {
        if (!running) {
            System.out.println("Scheduler não está rodando");
            return;
        }

        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }

        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        running = false;
        System.out.println("✓ Verificações automáticas paradas");
    }

    public boolean isRunning() {
        return running;
    }

    public int getIntervalMinutes() {
        return intervalMinutes;
    }

    public LocalDateTime getLastRunAt() {
        return lastRunAt;
    }

    public String getStatusInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Status: ");

        if (running) {
            sb.append("✓ ATIVA");
        } else {
            sb.append("○ INATIVA");
        }

        sb.append("\n");
        sb.append("Intervalo: ").append(intervalMinutes).append(" minutos\n");

        if (lastRunAt != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            sb.append("Última execução: ").append(lastRunAt.format(formatter));
        } else {
            sb.append("Última execução: Nunca executada");
        }

        return sb.toString();
    }
}
