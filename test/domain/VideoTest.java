package domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Video Domain Tests")
class VideoTest {

    @Test
    @DisplayName("Deve criar vídeo com builder e validar propriedades obrigatórias")
    void shouldCreateVideoWithBuilderAndValidateRequiredFields() {
        LocalDateTime now = LocalDateTime.now();

        Video video = new Video.Builder()
                .id("video123")
                .title("Test Video")
                .url("https://youtube.com/watch?v=test")
                .publishedAt(now)
                .playlistId("playlist123")
                .build();

        assertEquals("video123", video.getId());
        assertEquals("Test Video", video.getTitle());
        assertEquals("https://youtube.com/watch?v=test", video.getUrl());
        assertEquals(now, video.getPublishedAt());
        assertEquals("playlist123", video.getPlaylistId());
        assertFalse(video.isDownloaded());
        assertNull(video.getDownloadedAt());
    }

    @Test
    @DisplayName("Deve lançar exceção quando campos obrigatórios forem nulos")
    void shouldThrowExceptionWhenRequiredFieldsAreNull() {
        assertThrows(NullPointerException.class, () ->
            new Video.Builder()
                .title("Test")
                .url("https://youtube.com/watch?v=test")
                .playlistId("playlist123")
                .build()
        );

        assertThrows(NullPointerException.class, () ->
            new Video.Builder()
                .id("video123")
                .url("https://youtube.com/watch?v=test")
                .playlistId("playlist123")
                .build()
        );

        assertThrows(NullPointerException.class, () ->
            new Video.Builder()
                .id("video123")
                .title("Test")
                .playlistId("playlist123")
                .build()
        );

        assertThrows(NullPointerException.class, () ->
            new Video.Builder()
                .id("video123")
                .title("Test")
                .url("https://youtube.com/watch?v=test")
                .build()
        );
    }

    @Test
    @DisplayName("Deve marcar vídeo como baixado mantendo imutabilidade")
    void shouldMarkVideoAsDownloadedMaintainingImmutability() {
        Video originalVideo = new Video.Builder()
                .id("video123")
                .title("Test Video")
                .url("https://youtube.com/watch?v=test")
                .playlistId("playlist123")
                .build();

        Video downloadedVideo = originalVideo.markAsDownloaded();

        assertFalse(originalVideo.isDownloaded());
        assertNull(originalVideo.getDownloadedAt());

        assertTrue(downloadedVideo.isDownloaded());
        assertNotNull(downloadedVideo.getDownloadedAt());

        assertEquals(originalVideo.getId(), downloadedVideo.getId());
        assertEquals(originalVideo.getTitle(), downloadedVideo.getTitle());

        assertNotSame(originalVideo, downloadedVideo);
    }

    @Test
    @DisplayName("Deve comparar vídeos por ID (equals e hashCode)")
    void shouldCompareVideosByIdEqualsAndHashCode() {
        Video video1 = new Video.Builder()
                .id("video123")
                .title("Title 1")
                .url("https://youtube.com/watch?v=1")
                .playlistId("playlist123")
                .build();

        Video video2 = new Video.Builder()
                .id("video123")
                .title("Title 2")
                .url("https://youtube.com/watch?v=2")
                .playlistId("playlist456")
                .build();

        Video video3 = new Video.Builder()
                .id("video456")
                .title("Title 3")
                .url("https://youtube.com/watch?v=3")
                .playlistId("playlist123")
                .build();

        assertEquals(video1, video2);
        assertEquals(video1.hashCode(), video2.hashCode());

        assertNotEquals(video1, video3);
    }
}
