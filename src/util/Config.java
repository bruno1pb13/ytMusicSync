package util;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Gerenciador de configurações da aplicação.
 * Carrega e salva configurações em arquivo properties.
 */
public class Config {
    private final Path configFile;
    private final Properties properties;

    public Config() {
        this.configFile = AppDataDir.getConfigFile();
        this.properties = new Properties();
        loadConfig();
    }

    private void loadConfig() {
        if (Files.exists(configFile)) {
            try (InputStream input = new FileInputStream(configFile.toFile())) {
                properties.load(input);
            } catch (IOException e) {
                System.err.println("Erro ao carregar configuração: " + e.getMessage());
                createDefaultConfig();
            }
        } else {
            createDefaultConfig();
        }
    }

    private void createDefaultConfig() {
        properties.setProperty("download.directory", AppDataDir.getDefaultDownloadDir().toString());
        properties.setProperty("check.interval.minutes", "60");
        properties.setProperty("yt-dlp.path", "yt-dlp");
        properties.setProperty("audio.format", "mp3");
        properties.setProperty("audio.quality", "320");
        properties.setProperty("cookies.enabled", "false");
        properties.setProperty("cookies.browser", "chrome");
        properties.setProperty("auto.sync.enabled", "false");
        saveConfig();
        System.out.println("✓ Configuração padrão criada em: " + configFile);
    }

    public void saveConfig() {
        try {
            Files.createDirectories(configFile.getParent());
            try (OutputStream output = new FileOutputStream(configFile.toFile())) {
                properties.store(output, "YT Music Sync Configuration");
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar configuração: " + e.getMessage());
        }
    }

    public String getDownloadDirectory() {
        return properties.getProperty("download.directory", AppDataDir.getDefaultDownloadDir().toString());
    }

    public void setDownloadDirectory(String directory) {
        properties.setProperty("download.directory", directory);
        saveConfig();
    }

    public int getCheckIntervalMinutes() {
        return Integer.parseInt(properties.getProperty("check.interval.minutes", "60"));
    }

    public void setCheckIntervalMinutes(int minutes) {
        properties.setProperty("check.interval.minutes", String.valueOf(minutes));
        saveConfig();
    }

    public String getYtDlpPath() {
        return properties.getProperty("yt-dlp.path", "yt-dlp");
    }

    public void setYtDlpPath(String path) {
        properties.setProperty("yt-dlp.path", path);
        saveConfig();
    }

    public String getAudioFormat() {
        return properties.getProperty("audio.format", "mp3");
    }

    public void setAudioFormat(String format) {
        properties.setProperty("audio.format", format);
        saveConfig();
    }

    public String getAudioQuality() {
        return properties.getProperty("audio.quality", "320");
    }

    public void setAudioQuality(String quality) {
        properties.setProperty("audio.quality", quality);
        saveConfig();
    }

    public boolean getCookiesEnabled() {
        return Boolean.parseBoolean(properties.getProperty("cookies.enabled", "false"));
    }

    public void setCookiesEnabled(boolean enabled) {
        properties.setProperty("cookies.enabled", String.valueOf(enabled));
        saveConfig();
    }

    public String getCookiesBrowser() {
        return properties.getProperty("cookies.browser", "chrome");
    }

    public void setCookiesBrowser(String browser) {
        properties.setProperty("cookies.browser", browser);
        saveConfig();
    }

    public boolean getAutoSyncEnabled() {
        return Boolean.parseBoolean(properties.getProperty("auto.sync.enabled", "false"));
    }

    public void setAutoSyncEnabled(boolean enabled) {
        properties.setProperty("auto.sync.enabled", String.valueOf(enabled));
        saveConfig();
    }

    public void displayConfig() {
        System.out.println("\n=== Configurações ===");
        System.out.println("Diretório de downloads: " + getDownloadDirectory());
        System.out.println("Intervalo de verificação: " + getCheckIntervalMinutes() + " minutos");
        System.out.println("Caminho yt-dlp: " + getYtDlpPath());
        System.out.println("Formato de áudio: " + getAudioFormat());
        System.out.println("Qualidade de áudio: " + getAudioQuality() + "kbps");
        System.out.println("Cookies habilitados: " + (getCookiesEnabled() ? "Sim" : "Não"));
        System.out.println("Navegador para cookies: " + getCookiesBrowser());
        System.out.println();
    }
}
