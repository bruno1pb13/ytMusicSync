package repository;

import domain.Playlist;
import java.util.List;
import java.util.Optional;

/**
 * Interface para persistência de playlists.
 * Segue o princípio de Inversão de Dependência (SOLID).
 */
public interface PlaylistRepository {
    /**
     * Salva ou atualiza uma playlist.
     */
    void save(Playlist playlist);

    /**
     * Busca uma playlist por ID.
     */
    Optional<Playlist> findById(String id);

    /**
     * Busca uma playlist por URL.
     */
    Optional<Playlist> findByUrl(String url);

    /**
     * Lista todas as playlists.
     */
    List<Playlist> findAll();

    /**
     * Remove uma playlist por ID.
     */
    void delete(String id);

    /**
     * Verifica se uma playlist existe.
     */
    boolean exists(String id);
}
