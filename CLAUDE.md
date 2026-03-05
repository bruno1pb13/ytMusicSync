# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Compile
mvn clean compile

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=SchedulerServiceTest

# Build fat JAR (includes all dependencies via maven-shade-plugin)
mvn clean package

# Run GUI mode (default)
java -jar target/ytMusicSync-1.0.0.jar

# Run CLI mode
java -jar target/ytMusicSync-1.0.0.jar --cli

# Run during development
mvn exec:java -Dexec.mainClass="Main"
```

**Prerequisites:** Java 21+, Maven 3.6+, `yt-dlp` (`pip install yt-dlp`), FFmpeg.

## Architecture

The project follows a layered architecture with manual dependency injection wired in `Application`.

### Layer overview

- **`Main`** - Entry point. Detects GUI availability (headless check), starts either `UIManager` or `Application.startCli()`.
- **`application/Application`** - Central coordinator. Wires all dependencies, holds application state (sync progress, auto-sync running status), fires `PropertyChangeEvent`s for UI updates. Also contains the full CLI menu loop (`startCli()`).
- **`service/SyncService`** - Core sync logic: add/remove playlists, fetch video lists via `PlaylistFetcher`, download audio via `AudioDownloader`, persist to repositories. Exposes a `SyncProgressListener` interface for progress callbacks.
- **`service/SchedulerService`** - Wraps `ScheduledExecutorService` to run `SyncService.syncAllPlaylists()` periodically. Uses a daemon thread named `SyncScheduler`.
- **`adapter/`** - Interfaces (`PlaylistFetcher`, `AudioDownloader`) and their yt-dlp implementations (`YtDlpPlaylistFetcher`, `YtDlpAudioDownloader`). These shell out to the `yt-dlp` binary via `ProcessBuilder`.
- **`repository/`** - Interfaces (`PlaylistRepository`, `VideoRepository`) and JSON file implementations (`JsonPlaylistRepository`, `JsonVideoRepository`). Data is persisted to `data/playlists.json` and `data/videos.json`. In-memory cache uses `ConcurrentHashMap`.
- **`domain/`** - Immutable `Playlist` and `Video` value objects using the Builder pattern.
- **`ui/`** - Swing GUI: `UIManager` (initializes FlatLaf theme, creates `MainWindow`), `MainWindow` (main window), `SettingsDialog`, `MaterialTheme`.
- **`util/Config`** - Reads/writes `config.properties` from the working directory. Persists all settings immediately on change.
- **`util/AutostartManager`** - Manages OS-level autostart (systemd/desktop entry on Linux).

### Data flow

1. User adds a YouTube playlist URL via CLI or GUI.
2. `Application.addPlaylist()` -> `SyncService.addPlaylist()` -> `YtDlpPlaylistFetcher.fetchPlaylistInfo()` shells out to yt-dlp to get metadata -> saved to `JsonPlaylistRepository`.
3. On sync, `SyncService.syncPlaylist()` fetches all video IDs via yt-dlp `--flat-playlist --dump-json`, saves new `Video` records to `JsonVideoRepository`, then downloads each un-downloaded video via `YtDlpAudioDownloader` into `<download.directory>/<playlist title>/`.
4. Progress is reported back to `Application` via `SyncProgressListener`, which fires `PropertyChangeEvent`s consumed by the Swing UI.

### Configuration

`config.properties` (auto-created in working directory on first run):
- `download.directory` - target download path (default: `./downloads`)
- `check.interval.minutes` - auto-sync interval (default: 60)
- `yt-dlp.path` - path to yt-dlp binary (default: `yt-dlp`)
- `audio.format` - mp3/m4a/opus (default: mp3)
- `audio.quality` - kbps (default: 320)
- `cookies.enabled` / `cookies.browser` - pass browser cookies to yt-dlp
- `auto.sync.enabled` - persisted toggle for auto-sync on startup
