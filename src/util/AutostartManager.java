package util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AutostartManager {

    private static final String APP_NAME = "YTMusicSync";
    private static final String DESKTOP_FILENAME = "ytmusicsync.desktop";

    public static boolean isSupported() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("linux") || os.contains("windows");
    }

    public static boolean isEnabled() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("linux")) {
            return getLinuxDesktopFile().exists();
        } else if (os.contains("windows")) {
            return isWindowsRegistryEntryPresent();
        }
        return false;
    }

    public static void setAutostart(boolean enable) throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("linux")) {
            if (enable) {
                enableLinux();
            } else {
                disableLinux();
            }
        } else if (os.contains("windows")) {
            if (enable) {
                enableWindows();
            } else {
                disableWindows();
            }
        }
    }

    private static Path getJarPath() {
        try {
            java.net.URL location = AutostartManager.class.getProtectionDomain()
                    .getCodeSource().getLocation();
            Path path = Paths.get(location.toURI());
            if (!path.toString().endsWith(".jar")) {
                throw new IllegalStateException("Nao esta rodando a partir de um JAR empacotado.");
            }
            return path.toAbsolutePath();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Nao foi possivel determinar o caminho do JAR.", e);
        }
    }

    private static String getJavaExecutable() {
        return ProcessHandle.current().info().command()
                .orElse(Paths.get(System.getProperty("java.home"), "bin", "java").toString());
    }

    // -------------------------------------------------------------------------
    // Linux
    // -------------------------------------------------------------------------

    private static File getLinuxDesktopFile() {
        String configHome = System.getenv("XDG_CONFIG_HOME");
        if (configHome == null || configHome.isEmpty()) {
            configHome = System.getProperty("user.home") + "/.config";
        }
        return new File(configHome + "/autostart/" + DESKTOP_FILENAME);
    }

    private static void enableLinux() throws IOException {
        Path jarPath = getJarPath();
        String javaExe = getJavaExecutable();
        String workDir = jarPath.getParent().toString();

        String content = "[Desktop Entry]\n"
                + "Type=Application\n"
                + "Name=YT Music Sync\n"
                + "Exec=" + javaExe + " -jar " + jarPath + "\n"
                + "Path=" + workDir + "\n"
                + "Hidden=false\n"
                + "NoDisplay=false\n"
                + "X-GNOME-Autostart-enabled=true\n";

        File desktopFile = getLinuxDesktopFile();
        desktopFile.getParentFile().mkdirs();
        Files.writeString(desktopFile.toPath(), content);
    }

    private static void disableLinux() throws IOException {
        File desktopFile = getLinuxDesktopFile();
        if (desktopFile.exists()) {
            Files.delete(desktopFile.toPath());
        }
    }

    // -------------------------------------------------------------------------
    // Windows
    // -------------------------------------------------------------------------

    private static final String REG_KEY = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";

    private static boolean isWindowsRegistryEntryPresent() {
        try {
            Process proc = Runtime.getRuntime().exec(
                    new String[]{"reg", "query", REG_KEY, "/v", APP_NAME});
            return proc.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static void enableWindows() throws IOException {
        Path jarPath = getJarPath();
        String jarDir = jarPath.getParent().toString();
        String value = "cmd /c \"cd /d \\\"" + jarDir + "\\\" && javaw -jar \\\"" + jarPath + "\\\"\"";

        try {
            Process proc = Runtime.getRuntime().exec(
                    new String[]{"reg", "add", REG_KEY, "/v", APP_NAME, "/t", "REG_SZ", "/d", value, "/f"});
            int exit = proc.waitFor();
            if (exit != 0) {
                throw new IOException("Falha ao adicionar entrada no registro do Windows (codigo " + exit + ").");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Operacao interrompida ao modificar o registro.", e);
        }
    }

    private static void disableWindows() throws IOException {
        try {
            Process proc = Runtime.getRuntime().exec(
                    new String[]{"reg", "delete", REG_KEY, "/v", APP_NAME, "/f"});
            int exit = proc.waitFor();
            if (exit != 0 && isWindowsRegistryEntryPresent()) {
                throw new IOException("Falha ao remover entrada do registro do Windows (codigo " + exit + ").");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Operacao interrompida ao modificar o registro.", e);
        }
    }
}
