package adapter;

import domain.Video;
import java.io.*;
import java.nio.file.*;

/**
 * Implementação de AudioDownloader usando yt-dlp.
 * Faz download de áudio em formato configurável.
 */
public class YtDlpAudioDownloader implements AudioDownloader {
    private final String ytDlpPath;
    private final String audioFormat;
    private final String audioQuality;

    public YtDlpAudioDownloader(String ytDlpPath, String audioFormat, String audioQuality) {
        this.ytDlpPath = ytDlpPath;
        this.audioFormat = audioFormat;
        this.audioQuality = audioQuality;
    }

    @Override
    public boolean download(Video video, String outputDirectory) {
        try {
            // Cria diretório se não existir
            Path outputPath = Paths.get(outputDirectory);
            Files.createDirectories(outputPath);

            // Template de saída: Playlist/Nome do vídeo
            String outputTemplate = outputDirectory + "/%(title)s.%(ext)s";

            ProcessBuilder pb = new ProcessBuilder(
                    ytDlpPath,
                    "-x",  // Extrai apenas áudio
                    "--audio-format", audioFormat,
                    "--audio-quality", audioQuality,
                    "--no-playlist",  // Baixa apenas o vídeo específico
                    "--output", outputTemplate,
                    "--no-mtime",  // Não preserva timestamp original
                    "--embed-thumbnail",  // Embute thumbnail no arquivo
                    "--add-metadata",  // Adiciona metadados
                    video.getUrl()
            );

            System.out.println("Baixando: " + video.getTitle());

            Process process = pb.start();

            // Captura saída para feedback
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
