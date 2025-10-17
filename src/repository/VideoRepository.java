package repository;

import domain.Video;
import java.util.List;
import java.util.Optional;

/**
 * Interface para persistência de vídeos.
 * Segue o princípio de Inversão de Dependência (SOLID).
 */
public interface VideoRepository {
    /**
     * Salva ou atualiza um vídeo.
     */
    void save(Video video);

    /**
     * Busca um vídeo por ID.
     */
    Optional<Video> findById(String id);

    /**
     * Lista todos os vídeos de uma playlist.
     */
    List<Video> findByPlaylistId(String playlistId);

    /**
     * Lista vídeos não baixados de uma playlist.
     */
    List<Video> findNotDownloadedByPlaylistId(String playlistId);

    /**
     * Verifica se um vídeo existe.
     */
    boolean exists(String id);

    /**
     * Remove um vídeo por ID.
     */
    void delete(String id);

    /**
     * Conta vídeos de uma playlist.
     */
    int countByPlaylistId(String playlistId);
}
