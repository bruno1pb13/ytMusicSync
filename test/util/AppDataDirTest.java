package util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AppDataDir Tests")
class AppDataDirTest {

    @Test
    @DisplayName("Deve retornar um Path não-nulo")
    void shouldReturnNonNullPath() {
        Path path = AppDataDir.get();
        assertNotNull(path);
    }

    @Test
    @DisplayName("Deve terminar com o segmento 'data'")
    void shouldEndWithDataSegment() {
        Path path = AppDataDir.get();
        assertEquals("data", path.getFileName().toString());
    }

    @Test
    @DisplayName("Deve conter o nome da aplicação no path")
    void shouldContainAppNameInPath() {
        Path path = AppDataDir.get();
        String pathStr = path.toString();
        assertTrue(pathStr.contains("ytmusicsync"),
                "O path deveria conter 'ytmusicsync', mas foi: " + pathStr);
    }

    @Test
    @DisplayName("Deve retornar path correto para o sistema operacional atual")
    void shouldReturnOsSpecificPath() {
        String os = System.getProperty("os.name", "").toLowerCase();
        Path path = AppDataDir.get();
        String pathStr = path.toString();

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                assertTrue(pathStr.startsWith(appData),
                        "No Windows, o path deveria começar com APPDATA: " + appData);
            }
        } else {
            assertTrue(pathStr.contains("ytmusicsync"),
                    "No Linux/Mac, o path deveria conter 'ytmusicsync', mas foi: " + pathStr);
            assertTrue(pathStr.endsWith("data"),
                    "No Linux/Mac, o path deveria terminar com 'data', mas foi: " + pathStr);
        }
    }

    @Test
    @DisplayName("Deve retornar o mesmo path em chamadas consecutivas")
    void shouldReturnConsistentPath() {
        Path path1 = AppDataDir.get();
        Path path2 = AppDataDir.get();
        assertEquals(path1, path2);
    }
}
