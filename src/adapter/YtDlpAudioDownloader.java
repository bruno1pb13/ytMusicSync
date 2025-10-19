package adapter;

import domain.Video;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação de AudioDownloader usando yt-dlp.
 * Faz download de áudio em formato configurável.
 */
public class YtDlpAudioDownloader implements AudioDownloader {
    private final String ytDlpPath;
    private final String audioFormat;
    private final String audioQuality;
    private final boolean cookiesEnabled;
    private final String cookiesBrowser;

    public YtDlpAudioDownloader(String ytDlpPath, String audioFormat, String audioQuality,
                                boolean cookiesEnabled, String cookiesBrowser) {
        this.ytDlpPath = ytDlpPath;
        this.audioFormat = audioFormat;
        this.audioQuality = audioQuality;
        this.cookiesEnabled = cookiesEnabled;
        this.cookiesBrowser = cookiesBrowser;
    }

    @Override
    public boolean download(Video video, String outputDirectory) {
        try {
            Path outputPath = Paths.get(outputDirectory);
            Files.createDirectories(outputPath);

            String outputTemplate = outputDirectory + "/%(title)s.%(ext)s";

            List<String> command = new ArrayList<>();
            command.add(ytDlpPath);
            command.add("-x");  // Extrai apenas áudio
            command.add("--audio-format");
            command.add(audioFormat);
            command.add("--audio-quality");
            command.add(audioQuality);
            command.add("--no-playlist");  // Baixa apenas o vídeo específico
            command.add("--output");
            command.add(outputTemplate);
            command.add("--no-mtime");  // Não preserva timestamp original
            command.add("--embed-thumbnail");  // Embute thumbnail no arquivo
            command.add("--add-metadata");  // Adiciona metadados
            if (cookiesEnabled) {
                command.add("--cookies-from-browser");
                command.add(cookiesBrowser);
            }
            command.add(video.getUrl());

            ProcessBuilder pb = new ProcessBuilder(command);

            System.out.println("Baixando: " + video.getTitle());

            Process process = pb.start();

            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("  " + line);
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao ler saída: " + e.getMessage());
                }
            });

            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println("  ERRO: " + line);
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao ler erro: " + e.getMessage());
                }
            });

            outputThread.start();
            errorThread.start();

            int exitCode = process.waitFor();

            outputThread.join();
            errorThread.join();

            if (exitCode == 0) {
                System.out.println("✓ Download concluído: " + video.getTitle());
                return true;
            } else {
                System.err.println("✗ Falha no download: " + video.getTitle() + " (código: " + exitCode + ")");
                return false;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Erro ao baixar vídeo: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder(ytDlpPath, "--version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    @Override
    public String getVersion() {
        try {
            ProcessBuilder pb = new ProcessBuilder(ytDlpPath, "--version");
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String version = reader.readLine();

            process.waitFor();
            return version != null ? version : "Desconhecida";
        } catch (IOException | InterruptedException e) {
            return "Erro ao obter versão";
        }
    }
}
