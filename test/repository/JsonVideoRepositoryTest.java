package repository;

import domain.Video;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonVideoRepository Tests")
class JsonVideoRepositoryTest {

    private static final String TEST_DATA_FILE = "data/videos.json";
    private JsonVideoRepository repository;

    @BeforeEach
    void setUp() throws IOException {
        cleanupTestFile();
        repository = new JsonVideoRepository();
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
    @DisplayName("Deve salvar, buscar e verificar existência de vídeo")
    void shouldSaveFindAndCheckExistence() {
        // Arrange
        Video video = new Video.Builder()
                .id("video123")
                .title("Test Video")
                .url("https://youtube.com/watch?v=test")
                .playlistId("playlist123")
                .build();

        // Act
        repository.save(video);

        // Assert
        assertTrue(repository.exists("video123"));
        Optional<Video> found = repository.findById("video123");
        assertTrue(found.isPresent());
        assertEquals("video123", found.get().getId());
        assertEquals("Test Video", found.get().getTitle());

        // Busca por ID inexistente
        assertFalse(repository.exists("nonexistent"));
        assertTrue(repository.findById("nonexistent").isEmpty());
    }

    @Test
    @DisplayName("Deve buscar vídeos por playlist e filtrar não baixados")
    void shouldFindVideosByPlaylistAndFilterNotDownloaded() {
        // Arrange
        Video downloaded = new Video.Builder()
                .id("video1")
                .title("Downloaded")
                .url("https://youtube.com/watch?v=1")
                .playlistId("playlist123")
                .downloaded(true)
                .downloadedAt(LocalDateTime.now())
                .build();

        Video notDownloaded1 = new Video.Builder()
                .id("video2")
                .title("Not Downloaded 1")
                .url("https://youtube.com/watch?v=2")
                .playlistId("playlist123")
                .build();

        Video notDownloaded2 = new Video.Builder()
                .id("video3")
                .title("Not Downloaded 2")
                .url("https://youtube.com/watch?v=3")
                .playlistId("playlist123")
                .build();

        Video otherPlaylist = new Video.Builder()
                .id("video4")
                .title("Other Playlist")
                .url("https://youtube.com/watch?v=4")
                .playlistId("playlist456")
                .build();

        // Act
        repository.save(downloaded);
        repository.save(notDownloaded1);
        repository.save(notDownloaded2);
        repository.save(otherPlaylist);

        List<Video> allPlaylist123 = repository.findByPlaylistId("playlist123");
        List<Video> notDownloadedPlaylist123 = repository.findNotDownloadedByPlaylistId("playlist123");
        int count = repository.countByPlaylistId("playlist123");

        // Assert
        assertEquals(3, allPlaylist123.size());
        assertEquals(2, notDownloadedPlaylist123.size());
        assertEquals(3, count);
        assertTrue(notDownloadedPlaylist123.stream().noneMatch(Video::isDownloaded));
    }

    @Test
    @DisplayName("Deve atualizar e deletar vídeo")
    void shouldUpdateAndDeleteVideo() {
        // Arrange
        Video video = new Video.Builder()
                .id("video123")
                .title("Test Video")
                .url("https://youtube.com/watch?v=test")
                .playlistId("playlist123")
                .build();

        repository.save(video);

        // Act - Atualizar
        Video updated = video.markAsDownloaded();
        repository.save(updated);
        Optional<Video> found = repository.findById("video123");

        // Assert - Atualização
        assertTrue(found.isPresent());
        assertTrue(found.get().isDownloaded());

        // Act - Deletar
        repository.delete("video123");

        // Assert - Deletado
        assertFalse(repository.exists("video123"));
        assertTrue(repository.findById("video123").isEmpty());
    }

    @Test
    @DisplayName("Deve persistir e recarregar dados do arquivo")
    void shouldPersistAndReloadFromFile() {
        // Arrange
        Video video = new Video.Builder()
                .id("video123")
                .title("Test Video")
                .url("https://youtube.com/watch?v=test")
                .playlistId("playlist123")
                .publishedAt(LocalDateTime.now())
                .downloaded(true)
                .downloadedAt(LocalDateTime.now())
                .build();

        // Act
        repository.save(video);

        // Simula restart - nova instância do repositório
        JsonVideoRepository newRepository = new JsonVideoRepository();
        Optional<Video> reloaded = newRepository.findById("video123");

        // Assert
        assertTrue(reloaded.isPresent());
        assertEquals("video123", reloaded.get().getId());
        assertEquals("Test Video", reloaded.get().getTitle());
        assertTrue(reloaded.get().isDownloaded());
        assertNotNull(reloaded.get().getPublishedAt());
        assertNotNull(reloaded.get().getDownloadedAt());
    }
}
