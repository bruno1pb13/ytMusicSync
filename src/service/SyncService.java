package service;

import adapter.AudioDownloader;
import adapter.PlaylistFetcher;
import domain.Playlist;
import domain.Video;
import repository.PlaylistRepository;
import repository.VideoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Serviço responsável pela sincronização de playlists.
 * Segue o princípio de Single Responsibility (SOLID).
 */
public class SyncService {
    private final PlaylistRepository playlistRepository;
    private final VideoRepository videoRepository;
    private final PlaylistFetcher playlistFetcher;
    private final AudioDownloader audioDownloader;
    private final String downloadDirectory;

    public SyncService(
            PlaylistRepository playlistRepository,
            VideoRepository videoRepository,
            PlaylistFetcher playlistFetcher,
            AudioDownloader audioDownloader,
            String downloadDirectory) {
        this.playlistRepository = playlistRepository;
        this.videoRepository = videoRepository;
        this.playlistFetcher = playlistFetcher;
        this.audioDownloader = audioDownloader;
        this.downloadDirectory = downloadDirectory;
    }

    /**
     * Adiciona uma nova playlist ao sistema.
     */
    public Playlist addPlaylist(String playlistUrl) {
        String playlistId = playlistFetcher.extractPlaylistId(playlistUrl);

        // Verifica se já existe
        Optional<Playlist> existing = playlistRepository.findById(playlistId);
        if (existing.isPresent()) {
            System.out.println("Playlist já existe: " + existing.get().getTitle());
            return existing.get();
        }

        // Busca informações da playlist
        PlaylistFetcher.PlaylistInfo info = playlistFetcher.fetchPlaylistInfo(playlistUrl);

        Playlist playlist = new Playlist.Builder()
                .id(info.getId())
                .url(playlistUrl)
                .title(info.getTitle())
                .videoCount(info.getVideoCount())
                .build();

        playlistRepository.save(playlist);
        System.out.println("✓ Playlist adicionada: " + playlist.getTitle());

        return playlist;
    }

    /**
     * Remove uma playlist do sistema.
     */
    public void removePlaylist(String playlistId) {
        Optional<Playlist> playlist = playlistRepository.findById(playlistId);
        if (playlist.isEmpty()) {
            System.out.println("Playlist não encontrada");
            return;
        }

        // Remove vídeos associados
        List<Video> videos = videoRepository.findByPlaylistId(playlistId);
        for (Video video : videos) {
            videoRepository.delete(video.getId());
        }

        playlistRepository.delete(playlistId);
        System.out.println("✓ Playlist removida: " + playlist.get().getTitle());
    }

    /**
     * Sincroniza uma playlist específica.
     */
    public SyncResult syncPlaylist(String playlistId) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isEmpty()) {
            return new SyncResult(0, 0, "Playlist não encontrada");
        }

        Playlist playlist = playlistOpt.get();
        System.out.println("\n=== Sincronizando: " + playlist.getTitle() + " ===");

        // Busca vídeos da playlist
        List<Video> fetchedVideos = playlistFetcher.fetchVideos(playlist.getUrl());
        System.out.println("Encontrados " + fetchedVideos.size() + " vídeos na playlist");

        int newVideos = 0;
        int downloaded = 0;

        // Processa cada vídeo
        for (Video video : fetchedVideos) {
            if (!videoRepository.exists(video.getId())) {
                videoRepository.save(video);
                newVideos++;
                System.out.println("  + Novo vídeo: " + video.getTitle());
            }
        }

        // Baixa vídeos pendentes
        List<Video> toDownload = videoRepository.findNotDownloadedByPlaylistId(playlistId);
        System.out.println("\n" + toDownload.size() + " vídeos para baixar");

        for (Video video : toDownload) {
            boolean success = audioDownloader.download(video, downloadDirectory + "/" + playlist.getTitle());
            if (success) {
                Video updatedVideo = video.markAsDownloaded();
                videoRepository.save(updatedVideo);
                downloaded++;
            }
        }

        // Atualiza informações da playlist
        Playlist updatedPlaylist = playlist.updateSyncTime(fetchedVideos.size());
        playlistRepository.save(updatedPlaylist);

        System.out.println("\n✓ Sincronização concluída");
        return new SyncResult(newVideos, downloaded, "Sucesso");
    }

    /**
     * Sincroniza todas as playlists.
     */
    public void syncAllPlaylists() {
        List<Playlist> playlists = playlistRepository.findAll();

        if (playlists.isEmpty()) {
            System.out.println("Nenhuma playlist cadastrada");
            return;
        }

        System.out.println("=== Sincronizando " + playlists.size() + " playlists ===\n");

        int totalNew = 0;
        int totalDownloaded = 0;

        for (Playlist playlist : playlists) {
            SyncResult result = syncPlaylist(playlist.getId());
            totalNew += result.newVideos;
            totalDownloaded += result.downloaded;
        }

        System.out.println("\n=== RESUMO ===");
        System.out.println("Novos vídeos: " + totalNew);
        System.out.println("Downloads: " + totalDownloaded);
    }

    /**
     * Lista todas as playlists cadastradas.
     */
    public List<Playlist> listPlaylists() {
        return playlistRepository.findAll();
    }

    /**
     * Obtém estatísticas de uma playlist.
     */
    public PlaylistStats getPlaylistStats(String playlistId) {
        Optional<Playlist> playlist = playlistRepository.findById(playlistId);
        if (playlist.isEmpty()) {
            return new PlaylistStats(0, 0, 0);
        }

        List<Video> videos = videoRepository.findByPlaylistId(playlistId);
        int total = videos.size();
        int downloaded = (int) videos.stream().filter(Video::isDownloaded).count();
        int pending = total - downloaded;

        return new PlaylistStats(total, downloaded, pending);
    }

    public static class SyncResult {
        public final int newVideos;
        public final int downloaded;
        public final String message;

        public SyncResult(int newVideos, int downloaded, String message) {
            this.newVideos = newVideos;
            this.downloaded = downloaded;
            this.message = message;
        }
    }

    public static class PlaylistStats {
        public final int totalVideos;
        public final int downloaded;
        public final int pending;

        public PlaylistStats(int totalVideos, int downloaded, int pending) {
            this.totalVideos = totalVideos;
            this.downloaded = downloaded;
            this.pending = pending;
        }
    }
}
