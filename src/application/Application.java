package application;

import adapter.*;
import domain.Playlist;
import repository.*;
import service.*;
import util.Config;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.util.List;
import java.util.Scanner;

/**
 * Classe de aplicação que coordena os componentes.
 * Implementa padrão Dependency Injection manual.
 */
public class Application {
    private final Config config;
    private final SyncService syncService;
    private SchedulerService schedulerService;
    private final Scanner scanner;


    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean autoSyncRunning;
    private boolean syncInProgress;

    public Application() {
        this.config = new Config();
        this.scanner = new Scanner(System.in);

        PlaylistRepository playlistRepository = new JsonPlaylistRepository();
        VideoRepository videoRepository = new JsonVideoRepository();

        PlaylistFetcher playlistFetcher = new YtDlpPlaylistFetcher(
                config.getYtDlpPath(),
                config.getCookiesEnabled(),
                config.getCookiesBrowser()
        );

        AudioDownloader audioDownloader = new YtDlpAudioDownloader(
                config.getYtDlpPath(),
                config.getAudioFormat(),
                config.getAudioQuality(),
                config.getCookiesEnabled(),
                config.getCookiesBrowser()
        );

        this.syncService = new SyncService(
                playlistRepository,
                videoRepository,
                playlistFetcher,
                audioDownloader,
                config.getDownloadDirectory()
        );

        this.schedulerService = new SchedulerService(syncService, config.getCheckIntervalMinutes());
    }

    public void triggerSyncNow() {
        setSyncInProgress(true);
        try {
            syncService.syncAllPlaylists();
        } finally {
            setSyncInProgress(false);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public boolean isAutoSyncRunning() {
        return autoSyncRunning;
    }

    public List<Playlist> getPlaylists() {
        return syncService.listPlaylists();
    }

    public SyncService.PlaylistStats getPlaylistStats(String playlistId) {
        return syncService.getPlaylistStats(playlistId);
    }

    public Config getConfig() {
        return config;
    }

    public Playlist addPlaylist(String url) {
        Playlist playlist = syncService.addPlaylist(url);
        pcs.firePropertyChange("playlistsChanged", null, playlist);
        return playlist;
    }

    public void removePlaylist(String playlistId) {
        syncService.removePlaylist(playlistId);
        pcs.firePropertyChange("playlistsChanged", playlistId, null);
    }

    public void syncPlaylist(String playlistId) {
        setSyncInProgress(true);
        try {
            syncService.syncPlaylist(playlistId);
        } finally {
            setSyncInProgress(false);
        }
    }

    public void startAutoSync() {
        if (!schedulerService.isRunning()) {
            schedulerService = new SchedulerService(syncService, config.getCheckIntervalMinutes());
            schedulerService.start();
            setAutoSyncRunning(true);
        }
    }

    public void stopAutoSync() {
        if (schedulerService.isRunning()) {
            schedulerService.stop();
            setAutoSyncRunning(false);
        }
    }

    public void shutdown() {
        if (schedulerService.isRunning()) {
            schedulerService.stop();
        }
        scanner.close();
    }

    public void start() {
        System.out.println("╔════════════════════════════════════╗");
        System.out.println("║     YT Music Sync - v1.0.0        ║");
        System.out.println("╚════════════════════════════════════╝");

        AudioDownloader downloader = new YtDlpAudioDownloader(
                config.getYtDlpPath(),
                config.getAudioFormat(),
                config.getAudioQuality(),
                config.getCookiesEnabled(),
                config.getCookiesBrowser()
        );

        if (!downloader.isAvailable()) {
            System.err.println("\n⚠ AVISO: yt-dlp não encontrado!");
            System.err.println("Instale com: pip install yt-dlp");
            System.err.println("Ou ajuste o caminho em configurações\n");
        } else {
            System.out.println("\n✓ yt-dlp encontrado: " + downloader.getVersion());
        }

        showMenu();
    }

    private void showMenu() {
        while (true) {
            System.out.println("\n╔════════════════════════════════════╗");
            System.out.println("║           MENU PRINCIPAL          ║");
            System.out.println("╠════════════════════════════════════╣");
            System.out.println("║ 1. Adicionar Playlist             ║");
            System.out.println("║ 2. Listar Playlists               ║");
            System.out.println("║ 3. Remover Playlist               ║");
            System.out.println("║ 4. Sincronizar Agora              ║");
            System.out.println("║ 5. Iniciar Sinc. Automática       ║");
            System.out.println("║ 6. Parar Sinc. Automática         ║");
            System.out.println("║ 7. Configurações                  ║");
            System.out.println("║ 0. Sair                           ║");
            System.out.println("╠════════════════════════════════════╣");
            System.out.println("║ SINCRONIZAÇÃO AUTOMÁTICA          ║");

            String[] statusLines = schedulerService.getStatusInfo().split("\n");
            for (String line : statusLines) {
                System.out.printf("║ %-34s ║%n", line);
            }

            System.out.println("╚════════════════════════════════════╝");
            System.out.print("\nEscolha uma opção: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> addPlaylistMenu();
                    case "2" -> listPlaylists();
                    case "3" -> removePlaylistMenu();
                    case "4" -> syncNowMenu();
                    case "5" -> startAutoSyncMenu();
                    case "6" -> stopAutoSyncMenu();
                    case "7" -> showSettings();
                    case "0" -> {
                        exit();
                        return;
                    }
                    default -> System.out.println("Opção inválida!");
                }
            } catch (Exception e) {
                System.err.println("Erro: " + e.getMessage());
            }
        }
    }

    private void addPlaylistMenu() {
        System.out.print("\nURL da playlist: ");
        String url = scanner.nextLine().trim();

        if (url.isEmpty()) {
            System.out.println("URL inválida");
            return;
        }

        Playlist playlist = addPlaylist(url);
        System.out.println("\n✓ Playlist adicionada: " + playlist.getTitle());
        System.out.print("\nDeseja sincronizar agora? (s/n): ");

        if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
            syncPlaylist(playlist.getId());
        }
    }

    private void listPlaylists() {
        List<Playlist> playlists = syncService.listPlaylists();

        if (playlists.isEmpty()) {
            System.out.println("\nNenhuma playlist cadastrada");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                    PLAYLISTS CADASTRADAS                   ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");

        for (int i = 0; i < playlists.size(); i++) {
            Playlist p = playlists.get(i);
            SyncService.PlaylistStats stats = syncService.getPlaylistStats(p.getId());

            System.out.printf("║ %d. %-50s ║%n", (i + 1), truncate(p.getTitle(), 50));
            System.out.printf("║    Vídeos: %d | Baixados: %d | Pendentes: %d%n",
                    stats.totalVideos, stats.downloaded, stats.pending);
            System.out.println("║    ID: " + p.getId());
            if (p.getLastSyncedAt() != null) {
                System.out.println("║    Última sinc: " + p.getLastSyncedAt());
            }
            System.out.println("╠════════════════════════════════════════════════════════════╣");
        }
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }

    private void removePlaylistMenu() {
        listPlaylists();
        System.out.print("\nID da playlist para remover: ");
        String id = scanner.nextLine().trim();

        if (!id.isEmpty()) {
            removePlaylist(id);
        }
    }

    private void syncNowMenu() {
        List<Playlist> playlists = syncService.listPlaylists();

        if (playlists.isEmpty()) {
            System.out.println("\nNenhuma playlist cadastrada");
            return;
        }

        System.out.println("\n1. Sincronizar todas");
        System.out.println("2. Sincronizar uma específica");
        System.out.print("\nEscolha: ");

        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            setSyncInProgress(true);
            try {
                syncService.syncAllPlaylists();
            } finally {
                setSyncInProgress(false);
            }
        } else if (choice.equals("2")) {
            listPlaylists();
            System.out.print("\nID da playlist: ");
            String id = scanner.nextLine().trim();
            if (!id.isEmpty()) {
                syncPlaylist(id);
            }
        }
    }

    private void startAutoSyncMenu() {
        if (schedulerService.isRunning()) {
            System.out.println("\nSincronização automática já está rodando");
            return;
        }

        startAutoSync();
    }

    private void stopAutoSyncMenu() {
        if (!schedulerService.isRunning()) {
            System.out.println("\nSincronização automática não está rodando");
            return;
        }

        stopAutoSync();
    }

    private void setAutoSyncRunning(boolean running) {
        boolean old = this.autoSyncRunning;
        this.autoSyncRunning = running;
        pcs.firePropertyChange("autoSyncRunning", old, running);
    }

    private void setSyncInProgress(boolean syncing) {
        boolean old = this.syncInProgress;
        this.syncInProgress = syncing;
        pcs.firePropertyChange("syncInProgress", old, syncing);
    }

    private void showSettings() {
        config.displayConfig();

        System.out.println("\n1. Alterar diretório de downloads");
        System.out.println("2. Alterar intervalo de verificação");
        System.out.println("3. Alterar caminho yt-dlp");
        System.out.println("4. Alterar formato de áudio");
        System.out.println("5. Alterar qualidade de áudio");
        System.out.println("0. Voltar");
        System.out.print("\nEscolha: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                System.out.print("Novo diretório: ");
                config.setDownloadDirectory(scanner.nextLine().trim());
                System.out.println("✓ Atualizado");
            }
            case "2" -> {
                System.out.print("Novo intervalo (minutos): ");
                try {
                    int minutes = Integer.parseInt(scanner.nextLine().trim());
                    if (minutes < 1) {
                        System.out.println("✗ Intervalo deve ser no mínimo 1 minuto");
                        return;
                    }
                    config.setCheckIntervalMinutes(minutes);
                    System.out.println("✓ Intervalo atualizado para " + minutes + " minutos");

                    if (schedulerService.isRunning()) {
                        System.out.println("\n⚠ Sincronização automática ativa detectada");
                        System.out.print("Deseja reiniciar para aplicar o novo intervalo? (s/n): ");
                        String restart = scanner.nextLine().trim();
                        if (restart.equalsIgnoreCase("s")) {
                            System.out.println("Reiniciando sincronização automática...");
                            schedulerService.stop();
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            schedulerService = new SchedulerService(syncService, minutes);
                            schedulerService.start();
                            System.out.println("✓ Sincronização reiniciada com novo intervalo!");
                        } else {
                            System.out.println("✓ O novo intervalo será aplicado no próximo início.");
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("✗ Valor inválido. Digite apenas números.");
                }
            }
            case "3" -> {
                System.out.print("Novo caminho: ");
                config.setYtDlpPath(scanner.nextLine().trim());
                System.out.println("✓ Atualizado");
            }
            case "4" -> {
                System.out.print("Novo formato (mp3/m4a/opus): ");
                String format = scanner.nextLine().trim().toLowerCase();
                if (format.equals("mp3") || format.equals("m4a") || format.equals("opus")) {
                    config.setAudioFormat(format);
                    System.out.println("✓ Formato atualizado para " + format);
                } else {
                    System.out.println("✗ Formato inválido. Use: mp3, m4a ou opus");
                }
            }
            case "5" -> {
                System.out.print("Nova qualidade (kbps, ex: 128, 192, 256, 320): ");
                String quality = scanner.nextLine().trim();
                try {
                    int qualityNum = Integer.parseInt(quality);
                    if (qualityNum < 64 || qualityNum > 320) {
                        System.out.println("✗ Qualidade deve estar entre 64 e 320 kbps");
                        return;
                    }
                    config.setAudioQuality(quality);
                    System.out.println("✓ Qualidade atualizada para " + quality + "kbps");
                } catch (NumberFormatException e) {
                    System.out.println("✗ Valor inválido. Digite apenas números.");
                }
            }
        }
    }

    private void exit() {
        System.out.println("\nEncerrando...");
        shutdown();
        System.out.println("Até logo!");
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }
}
