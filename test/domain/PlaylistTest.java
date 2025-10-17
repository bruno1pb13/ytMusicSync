package domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Playlist Domain Tests")
class PlaylistTest {

    @Test
    @DisplayName("Deve criar playlist com builder e valores padrão")
    void shouldCreatePlaylistWithBuilderAndDefaultValues() {
        // Arrange & Act
        Playlist playlist = new Playlist.Builder()
                .id("playlist123")
                .url("https://youtube.com/playlist?list=test")
                .title("Test Playlist")
                .build();

        // Assert
        assertEquals("playlist123", playlist.getId());
        assertEquals("https://youtube.com/playlist?list=test", playlist.getUrl());
        assertEquals("Test Playlist", playlist.getTitle());
        assertEquals(0, playlist.getVideoCount()); // Padrão
        assertNull(playlist.getLastSyncedAt()); // Padrão
    }

    @Test
    @DisplayName("Deve lançar exceção quando campos obrigatórios forem nulos")
    void shouldThrowExceptionWhenRequiredFieldsAreNull() {
        // ID nulo
        assertThrows(NullPointerException.class, () ->
            new Playlist.Builder()
                .url("https://youtube.com/playlist?list=test")
                .title("Test")
                .build()
        );

        // URL nula
        assertThrows(NullPointerException.class, () ->
            new Playlist.Builder()
                .id("playlist123")
                .title("Test")
                .build()
        );

        // Título pode ser nulo (opcional)
        assertDoesNotThrow(() ->
            new Playlist.Builder()
                .id("playlist123")
                .url("https://youtube.com/playlist?list=test")
                .build()
        );
    }

    @Test
    @DisplayName("Deve atualizar sync time mantendo imutabilidade")
    void shouldUpdateSyncTimeMaintainingImmutability() {
        // Arrange
        Playlist originalPlaylist = new Playlist.Builder()
                .id("playlist123")
                .url("https://youtube.com/playlist?list=test")
                .title("Test Playlist")
                .videoCount(5)
                .build();

        LocalDateTime originalSyncTime = originalPlaylist.getLastSyncedAt();

        // Act
        Playlist updatedPlaylist = originalPlaylist.updateSyncTime(10);

        // Assert - Original permanece inalterado
        assertEquals(5, originalPlaylist.getVideoCount());
        assertEquals(originalSyncTime, originalPlaylist.getLastSyncedAt());

        // Novo objeto atualizado
        assertEquals(10, updatedPlaylist.getVideoCount());
        assertNotNull(updatedPlaylist.getLastSyncedAt());

        // Outros campos mantidos
        assertEquals(originalPlaylist.getId(), updatedPlaylist.getId());
        assertEquals(originalPlaylist.getTitle(), updatedPlaylist.getTitle());

        // Objetos diferentes
        assertNotSame(originalPlaylist, updatedPlaylist);
    }

    @Test
    @DisplayName("Deve comparar playlists por ID (equals e hashCode)")
    void shouldComparePlaylistsByIdEqualsAndHashCode() {
        // Arrange
        Playlist playlist1 = new Playlist.Builder()
                .id("playlist123")
                .url("https://youtube.com/playlist?list=1")
                .title("Title 1")
                .build();

        Playlist playlist2 = new Playlist.Builder()
                .id("playlist123")
                .url("https://youtube.com/playlist?list=2")
                .title("Title 2")
                .build();

        Playlist playlist3 = new Playlist.Builder()
                .id("playlist456")
                .url("https://youtube.com/playlist?list=3")
                .title("Title 3")
                .build();

        // Assert - Mesmo ID = iguais
        assertEquals(playlist1, playlist2);
        assertEquals(playlist1.hashCode(), playlist2.hashCode());

        // ID diferente = diferentes
        assertNotEquals(playlist1, playlist3);
    }
}
