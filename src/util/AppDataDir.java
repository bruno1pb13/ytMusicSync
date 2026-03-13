package util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolve os diretórios padrão da aplicação de forma portável entre sistemas operacionais.
 *
 * Linux/Mac:
 *   Config: ~/.config/ytmusicsync/config.properties
 *   Data:   ~/.local/share/ytmusicsync/data/
 *   Music:  ~/Music/ytMusicSync/
 *
 * Windows:
 *   Config: %APPDATA%\ytmusicsync\config.properties
 *   Data:   %APPDATA%\ytmusicsync\data\
 *   Music:  %USERPROFILE%\Music\ytMusicSync\
 */
public class AppDataDir {
    private static final String APP_NAME = "ytmusicsync";
    private static final String APP_DISPLAY_NAME = "ytMusicSync";

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    /** Diretório raiz de configuração (contém config.properties). */
    public static Path getConfigDir() {
        if (isWindows()) {
            String appData = System.getenv("APPDATA");
            if (appData != null) return Paths.get(appData, APP_NAME);
        }
        String xdgConfig = System.getenv("XDG_CONFIG_HOME");
        if (xdgConfig != null && !xdgConfig.isEmpty()) return Paths.get(xdgConfig, APP_NAME);
        return Paths.get(System.getProperty("user.home"), ".config", APP_NAME);
    }

    /** Arquivo de configuração. */
    public static Path getConfigFile() {
        return getConfigDir().resolve("config.properties");
    }

    /** Diretório de dados (playlists.json, videos.json). */
    public static Path get() {
        if (isWindows()) {
            String appData = System.getenv("APPDATA");
            if (appData != null) return Paths.get(appData, APP_NAME, "data");
        }
        String xdgData = System.getenv("XDG_DATA_HOME");
        if (xdgData != null && !xdgData.isEmpty()) return Paths.get(xdgData, APP_NAME, "data");
        return Paths.get(System.getProperty("user.home"), ".local", "share", APP_NAME, "data");
    }

    /** Diretório padrão de downloads de música. */
    public static Path getDefaultDownloadDir() {
        if (isWindows()) {
            String userProfile = System.getenv("USERPROFILE");
            if (userProfile != null) return Paths.get(userProfile, "Music", APP_DISPLAY_NAME);
        }
        String xdgMusic = System.getenv("XDG_MUSIC_DIR");
        if (xdgMusic != null && !xdgMusic.isEmpty()) return Paths.get(xdgMusic, APP_DISPLAY_NAME);
        return Paths.get(System.getProperty("user.home"), "Music", APP_DISPLAY_NAME);
    }
}
