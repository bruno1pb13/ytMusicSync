package repository;

import domain.Playlist;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonPlaylistRepository Tests")
class JsonPlaylistRepositoryTest {

    private static final String TEST_DATA_FILE = "data/playlists.json";
    private JsonPlaylistRepository repository;

    @BeforeEach
    void setUp() throws IOException {
        cleanupTestFile();
        repository = new JsonPlaylistRepository();
    }

    @AfterEach
    void tearDown() throws IOException {
        cleanupTestFile();
    }

    private void cleanupTestFile() throws IOException {
        Path path = Paths.get(TEST_DATA_FILE);
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    @Test
    @DisplayName("Deve salvar, buscar por ID e URL, e verificar existência")
    void shouldSaveFindByIdAndUrlAndCheckExistence() {
        // Arrange
        Playlist playlist = new Playlist.Builder()
                .id("playlist123")
                .url("https://youtube.com/playlist?list=test")
                .title("Test Playlist")
                .videoCount(10)
                .build();

        // Act
        repository.save(playlist);

        // Assert
        assertTrue(repository.exists("playlist123"));

        Optional<Playlist> foundById = repository.findById("playlist123");
        assertTrue(foundById.isPresent());
        assertEquals("playlist123", foundById.get().getId());

        Optional<Playlist> foundByUrl = repository.findByUrl("https://youtube.com/playlist?list=test");
        assertTrue(foundByUrl.isPresent());
        assertEquals("playlist123", foundByUrl.get().getId());

        // Buscas inexistentes
        assertFalse(repository.exists("nonexistent"));
        assertTrue(repository.findById("nonexistent").isEmpty());
        assertTrue(repository.findByUrl("https://youtube.com/playlist?list=nonexistent").isEmpty());
    }

    @Test
    @DisplayName("Deve listar todas playlists e deletar")
    void shouldFindAllAndDelete() {
        // Arrange
        Playlist playlist1 = new Playlist.Builder()
                .id("playlist1")
                .url("https://youtube.com/playlist?list=1")
                .title("Playlist 1")
                .build();

        Playlist playlist2 = new Playlist.Builder()
                .id("playlist2")
                .url("https://youtube.com/playlist?list=2")
                .title("Playlist 2")
                .build();

        // Act
        repository.save(playlist1);
        repository.save(playlist2);
        List<Playlist> all = repository.findAll();

        // Assert
        assertEquals(2, all.size());

        // Act - Deletar
        repository.delete("playlist1");
        List<Playlist> afterDelete = repository.findAll();

        // Assert
        assertEquals(1, afterDelete.size());
        assertFalse(repository.exists("playlist1"));
        assertTrue(repository.exists("playlist2"));
    }

    @Test
    @DisplayName("Deve atualizar playlist existente")
    void shouldUpdateExistingPlaylist() {
        // Arrange
        Playlist original = new Playlist.Builder()
                .id("playlist123")
                .url("https://youtube.com/playlist?list=test")
                .title("Original Title")
                .videoCount(5)
                .build();

        repository.save(original);

        // Act
        Playlist updated = original.updateSyncTime(15);
        repository.save(updated);

        Optional<Playlist> found = repository.findById("playlist123");

        // Assert
        assertTrue(found.isPresent());
        assertEquals(15, found.get().getVideoCount());
        assertNotNull(found.get().getLastSyncedAt());
    }

    @Test
    @DisplayName("Deve persistir e recarregar dados do arquivo")
    void shouldPersistAndReloadFromFile() {
        // Arrange
        Playlist playlist = new Playlist.Builder()
                .id("playlist123")
                .url("https://youtube.com/playlist?list=test")
                .title("Test Playlist")
                .videoCount(20)
                .lastSyncedAt(LocalDateTime.now())
                .build();

        // Act
        repository.save(playlist);

        // Simula restart - nova instância do repositório
        JsonPlaylistRepository newRepository = new JsonPlaylistRepository();
        Optional<Playlist> reloaded = newRepository.findById("playlist123");

        // Assert
        assertTrue(reloaded.isPresent());
        assertEquals("playlist123", reloaded.get().getId());
        assertEquals("Test Playlist", reloaded.get().getTitle());
        assertEquals(20, reloaded.get().getVideoCount());
        assertNotNull(reloaded.get().getLastSyncedAt());
    }
}
