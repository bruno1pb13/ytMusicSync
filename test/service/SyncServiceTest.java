package service;

import adapter.AudioDownloader;
import adapter.PlaylistFetcher;
import domain.Playlist;
import domain.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.PlaylistRepository;
import repository.VideoRepository;
import service.SyncService.PlaylistStats;
import service.SyncService.SyncResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SyncService Tests")
class SyncServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private PlaylistFetcher playlistFetcher;

    @Mock
    private AudioDownloader audioDownloader;

    private SyncService syncService;
    private static final String DOWNLOAD_DIR = "/tmp/test";

    @BeforeEach
    void setUp() {
        syncService = new SyncService(
                playlistRepository,
                videoRepository,
                playlistFetcher,
                audioDownloader,
                DOWNLOAD_DIR
        );
    }

    @Test
    @DisplayName("Deve adicionar nova playlist com sucesso")
    void shouldAddNewPlaylistSuccessfully() {
        // Arrange
        String playlistUrl = "https://youtube.com/playlist?list=test";
        String playlistId = "test";

        when(playlistFetcher.extractPlaylistId(playlistUrl)).thenReturn(playlistId);
        when(playlistRepository.findById(playlistId)).thenReturn(Optional.empty());

        PlaylistFetcher.PlaylistInfo info = new PlaylistFetcher.PlaylistInfo(
                playlistId,
                "Test Playlist",
                10
        );
        when(playlistFetcher.fetchPlaylistInfo(playlistUrl)).thenReturn(info);

        // Act
        Playlist result = syncService.addPlaylist(playlistUrl);

        // Assert
        assertNotNull(result);
        assertEquals(playlistId, result.getId());
        assertEquals("Test Playlist", result.getTitle());
        assertEquals(10, result.getVideoCount());

        verify(playlistRepository).save(any(Playlist.class));
    }

    @Test
    @DisplayName("Deve retornar playlist existente quando já cadastrada")
    void shouldReturnExistingPlaylistWhenAlreadyRegistered() {
        // Arrange
        String playlistUrl = "https://youtube.com/playlist?list=test";
        String playlistId = "test";

        Playlist existingPlaylist = new Playlist.Builder()
                .id(playlistId)
                .url(playlistUrl)
                .title("Existing Playlist")
                .build();

        when(playlistFetcher.extractPlaylistId(playlistUrl)).thenReturn(playlistId);
        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(existingPlaylist));

        // Act
        Playlist result = syncService.addPlaylist(playlistUrl);

        // Assert
        assertEquals(existingPlaylist, result);
        verify(playlistRepository, never()).save(any());
        verify(playlistFetcher, never()).fetchPlaylistInfo(anyString());
    }

    @Test
    @DisplayName("Deve remover playlist e seus vídeos")
    void shouldRemovePlaylistAndItsVideos() {
        // Arrange
        String playlistId = "playlist123";
        Playlist playlist = new Playlist.Builder()
                .id(playlistId)
                .url("https://youtube.com/playlist?list=test")
                .title("Test Playlist")
                .build();

        Video video1 = new Video.Builder()
                .id("video1")
                .title("Video 1")
                .url("https://youtube.com/watch?v=1")
                .playlistId(playlistId)
                .build();

        Video video2 = new Video.Builder()
                .id("video2")
                .title("Video 2")
                .url("https://youtube.com/watch?v=2")
                .playlistId(playlistId)
                .build();

        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
        when(videoRepository.findByPlaylistId(playlistId)).thenReturn(Arrays.asList(video1, video2));

        // Act
        syncService.removePlaylist(playlistId);

        // Assert
        verify(videoRepository).delete("video1");
        verify(videoRepository).delete("video2");
        verify(playlistRepository).delete(playlistId);
    }

    @Test
    @DisplayName("Não deve fazer nada ao remover playlist inexistente")
    void shouldDoNothingWhenRemovingNonexistentPlaylist() {
        // Arrange
        when(playlistRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act
        syncService.removePlaylist("nonexistent");

        // Assert
        verify(videoRepository, never()).findByPlaylistId(anyString());
        verify(playlistRepository, never()).delete(anyString());
    }

    @Test
    @DisplayName("Deve sincronizar playlist e adicionar novos vídeos")
    void shouldSyncPlaylistAndAddNewVideos() {
        // Arrange
        String playlistId = "playlist123";
        Playlist playlist = new Playlist.Builder()
                .id(playlistId)
                .url("https://youtube.com/playlist?list=test")
                .title("Test Playlist")
                .build();

        Video newVideo1 = new Video.Builder()
                .id("video1")
                .title("New Video 1")
                .url("https://youtube.com/watch?v=1")
                .playlistId(playlistId)
                .build();

        Video newVideo2 = new Video.Builder()
                .id("video2")
                .title("New Video 2")
                .url("https://youtube.com/watch?v=2")
                .playlistId(playlistId)
                .build();

        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
        when(playlistFetcher.fetchVideos(playlist.getUrl())).thenReturn(Arrays.asList(newVideo1, newVideo2));
        when(videoRepository.exists(anyString())).thenReturn(false);
        when(videoRepository.findNotDownloadedByPlaylistId(playlistId)).thenReturn(Collections.emptyList());

        // Act
        SyncResult result = syncService.syncPlaylist(playlistId);

        // Assert
        assertEquals(2, result.newVideos);
        verify(videoRepository, times(2)).save(any(Video.class));
        verify(playlistRepository).save(any(Playlist.class));
    }

    @Test
    @DisplayName("Deve baixar vídeos pendentes durante sincronização")
    void shouldDownloadPendingVideosDuringSync() {
        // Arrange
        String playlistId = "playlist123";
        Playlist playlist = new Playlist.Builder()
                .id(playlistId)
                .url("https://youtube.com/playlist?list=test")
                .title("Test Playlist")
                .build();

        Video pendingVideo = new Video.Builder()
                .id("video1")
                .title("Pending Video")
                .url("https://youtube.com/watch?v=1")
                .playlistId(playlistId)
                .downloaded(false)
                .build();

        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
        when(playlistFetcher.fetchVideos(playlist.getUrl())).thenReturn(Collections.emptyList());
        when(videoRepository.findNotDownloadedByPlaylistId(playlistId))
                .thenReturn(Collections.singletonList(pendingVideo));
        when(audioDownloader.download(any(Video.class), anyString())).thenReturn(true);

        // Act
        SyncResult result = syncService.syncPlaylist(playlistId);

        // Assert
        assertEquals(1, result.downloaded);
        verify(audioDownloader).download(eq(pendingVideo), contains("Test Playlist"));
        verify(videoRepository, times(1)).save(argThat(video ->
                video.getId().equals("video1") && video.isDownloaded()
        ));
    }

    @Test
    @DisplayName("Deve retornar erro ao sincronizar playlist inexistente")
    void shouldReturnErrorWhenSyncingNonexistentPlaylist() {
        // Arrange
        when(playlistRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act
        SyncResult result = syncService.syncPlaylist("nonexistent");

        // Assert
        assertEquals(0, result.newVideos);
        assertEquals(0, result.downloaded);
        assertEquals("Playlist não encontrada", result.message);
    }

    @Test
    @DisplayName("Não deve salvar vídeo novamente se download falhar")
    void shouldNotSaveVideoAgainIfDownloadFails() {
        // Arrange
        String playlistId = "playlist123";
        Playlist playlist = new Playlist.Builder()
                .id(playlistId)
                .url("https://youtube.com/playlist?list=test")
                .title("Test Playlist")
                .build();

        Video pendingVideo = new Video.Builder()
                .id("video1")
                .title("Pending Video")
                .url("https://youtube.com/watch?v=1")
                .playlistId(playlistId)
                .downloaded(false)
                .build();

        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
        when(playlistFetcher.fetchVideos(playlist.getUrl())).thenReturn(Collections.emptyList());
        when(videoRepository.findNotDownloadedByPlaylistId(playlistId))
                .thenReturn(Collections.singletonList(pendingVideo));
        when(audioDownloader.download(any(Video.class), anyString())).thenReturn(false);

        // Act
        SyncResult result = syncService.syncPlaylist(playlistId);

        // Assert
        assertEquals(0, result.downloaded);
        verify(videoRepository, never()).save(argThat(Video::isDownloaded));
    }

    @Test
    @DisplayName("Deve listar todas as playlists")
    void shouldListAllPlaylists() {
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

        when(playlistRepository.findAll()).thenReturn(Arrays.asList(playlist1, playlist2));

        // Act
        List<Playlist> playlists = syncService.listPlaylists();

        // Assert
        assertEquals(2, playlists.size());
        verify(playlistRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar estatísticas corretas da playlist")
    void shouldReturnCorrectPlaylistStats() {
        // Arrange
        String playlistId = "playlist123";
        Playlist playlist = new Playlist.Builder()
                .id(playlistId)
                .url("https://youtube.com/playlist?list=test")
                .title("Test Playlist")
                .build();

        Video downloadedVideo = new Video.Builder()
                .id("video1")
                .title("Downloaded Video")
                .url("https://youtube.com/watch?v=1")
                .playlistId(playlistId)
                .downloaded(true)
                .build();

        Video pendingVideo = new Video.Builder()
                .id("video2")
                .title("Pending Video")
                .url("https://youtube.com/watch?v=2")
                .playlistId(playlistId)
                .downloaded(false)
                .build();

        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
        when(videoRepository.findByPlaylistId(playlistId))
                .thenReturn(Arrays.asList(downloadedVideo, pendingVideo));

        // Act
        PlaylistStats stats = syncService.getPlaylistStats(playlistId);

        // Assert
        assertEquals(2, stats.totalVideos);
        assertEquals(1, stats.downloaded);
        assertEquals(1, stats.pending);
    }

    @Test
    @DisplayName("Deve retornar estatísticas zeradas para playlist inexistente")
    void shouldReturnZeroStatsForNonexistentPlaylist() {
        // Arrange
        when(playlistRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act
        PlaylistStats stats = syncService.getPlaylistStats("nonexistent");

        // Assert
        assertEquals(0, stats.totalVideos);
        assertEquals(0, stats.downloaded);
        assertEquals(0, stats.pending);
    }

    @Test
    @DisplayName("Deve sincronizar todas as playlists")
    void shouldSyncAllPlaylists() {
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

        when(playlistRepository.findAll()).thenReturn(Arrays.asList(playlist1, playlist2));
        when(playlistRepository.findById(anyString())).thenReturn(Optional.of(playlist1), Optional.of(playlist2));
        when(playlistFetcher.fetchVideos(anyString())).thenReturn(Collections.emptyList());
        when(videoRepository.findNotDownloadedByPlaylistId(anyString())).thenReturn(Collections.emptyList());

        // Act
        syncService.syncAllPlaylists();

        // Assert
        verify(playlistFetcher, times(2)).fetchVideos(anyString());
        verify(playlistRepository, times(2)).save(any(Playlist.class));
    }

    @Test
    @DisplayName("Não deve fazer nada ao sincronizar todas quando não há playlists")
    void shouldDoNothingWhenSyncingAllWithNoPlaylists() {
        // Arrange
        when(playlistRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        syncService.syncAllPlaylists();

        // Assert
        verify(playlistFetcher, never()).fetchVideos(anyString());
    }
}
