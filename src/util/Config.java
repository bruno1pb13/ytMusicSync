package util;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Gerenciador de configurações da aplicação.
 * Carrega e salva configurações em arquivo properties.
 */
public class Config {
    private static final String CONFIG_FILE = "config.properties";
    private final Properties properties;

    public Config() {
        this.properties = new Properties();
        loadConfig();
    }

    private void loadConfig() {
        Path configPath = Paths.get(CONFIG_FILE);
        if (Files.exists(configPath)) {
            try (InputStream input = new FileInputStream(CONFIG_FILE)) {
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
        properties.setProperty("download.directory", "./downloads");
        properties.setProperty("check.interval.minutes", "60");
        properties.setProperty("yt-dlp.path", "yt-dlp");
        properties.setProperty("audio.format", "mp3");
        properties.setProperty("audio.quality", "320");
        saveConfig();
        System.out.println("✓ Configuração padrão criada");
    }

    public void saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "YT Music Sync Configuration");
        } catch (IOException e) {
            System.err.println("Erro ao salvar configuração: " + e.getMessage());
        }
    }

    public String getDownloadDirectory() {
        return properties.getProperty("download.directory", "./downloads");
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

    public void displayConfig() {
        System.out.println("\n=== Configurações ===");
        System.out.println("Diretório de downloads: " + getDownloadDirectory());
        System.out.println("Intervalo de verificação: " + getCheckIntervalMinutes() + " minutos");
        System.out.println("Caminho yt-dlp: " + getYtDlpPath());
        System.out.println("Formato de áudio: " + getAudioFormat());
        System.out.println("Qualidade de áudio: " + getAudioQuality() + "kbps");
        System.out.println();
    }
}
