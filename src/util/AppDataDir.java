package util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolve o diretório de dados da aplicação de forma portável entre sistemas operacionais.
 * Linux/Mac: ~/.ytmusicsync/data
 * Windows:   %APPDATA%\ytmusicsync\data
 */
public class AppDataDir {
    private static final String APP_NAME = "ytmusicsync";

    public static Path get() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                return Paths.get(appData, APP_NAME, "data");
            }
        }
        return Paths.get(System.getProperty("user.home"), "." + APP_NAME, "data");
    }
}
