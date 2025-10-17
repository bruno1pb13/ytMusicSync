package domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade de domínio representando um vídeo.
 * Imutável para garantir integridade dos dados.
 */
public class Video {
    private final String id;
    private final String title;
    private final String url;
    private final LocalDateTime publishedAt;
    private final String playlistId;
    private final boolean downloaded;
    private final LocalDateTime downloadedAt;

    private Video(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "ID não pode ser nulo");
        this.title = Objects.requireNonNull(builder.title, "Título não pode ser nulo");
        this.url = Objects.requireNonNull(builder.url, "URL não pode ser nula");
        this.publishedAt = builder.publishedAt;
        this.playlistId = Objects.requireNonNull(builder.playlistId, "Playlist ID não pode ser nulo");
        this.downloaded = builder.downloaded;
        this.downloadedAt = builder.downloadedAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public LocalDateTime getDownloadedAt() {
        return downloadedAt;
    }

    public Video markAsDownloaded() {
        return new Builder()
                .id(this.id)
                .title(this.title)
                .url(this.url)
                .publishedAt(this.publishedAt)
                .playlistId(this.playlistId)
                .downloaded(true)
                .downloadedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Video video = (Video) o;
        return Objects.equals(id, video.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Video{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", downloaded=" + downloaded +
                '}';
    }

    public static class Builder {
        private String id;
        private String title;
        private String url;
        private LocalDateTime publishedAt;
        private String playlistId;
        private boolean downloaded = false;
        private LocalDateTime downloadedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder publishedAt(LocalDateTime publishedAt) {
            this.publishedAt = publishedAt;
            return this;
        }

        public Builder playlistId(String playlistId) {
            this.playlistId = playlistId;
            return this;
        }

        public Builder downloaded(boolean downloaded) {
            this.downloaded = downloaded;
            return this;
        }

        public Builder downloadedAt(LocalDateTime downloadedAt) {
            this.downloadedAt = downloadedAt;
            return this;
        }

        public Video build() {
            return new Video(this);
        }
    }
}
