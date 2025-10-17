package adapter;

import domain.Video;
import java.util.List;

/**
 * Interface para buscar informações de playlists do YouTube.
 * Abstração permite trocar implementação (API, yt-dlp, etc).
 */
public interface PlaylistFetcher {
    /**
     * Busca todos os vídeos de uma playlist.
     * @param playlistUrl URL da playlist
     * @return Lista de vídeos encontrados
     */
    List<Video> fetchVideos(String playlistUrl);

    /**
     * Extrai o ID da playlist de uma URL.
     */
    String extractPlaylistId(String playlistUrl);

    /**
     * Busca informações básicas da playlist (título, etc).
     */
    PlaylistInfo fetchPlaylistInfo(String playlistUrl);

    class PlaylistInfo {
        private final String id;
        private final String title;
        private final int videoCount;

        public PlaylistInfo(String id, String title, int videoCount) {
            this.id = id;
            this.title = title;
            this.videoCount = videoCount;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public int getVideoCount() {
            return videoCount;
        }
    }
}
