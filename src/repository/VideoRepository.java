package repository;

import domain.Video;
import java.util.List;
import java.util.Optional;

/**
 * Interface para persistência de vídeos.
 */
public interface VideoRepository {

    void save(Video video);

    Optional<Video> findById(String id);

    List<Video> findByPlaylistId(String playlistId);

    List<Video> findNotDownloadedByPlaylistId(String playlistId);

    boolean exists(String id);

    void delete(String id);

    int countByPlaylistId(String playlistId);
}
