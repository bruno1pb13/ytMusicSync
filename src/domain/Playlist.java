package domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade de domínio representando uma playlist.
 * Imutável para garantir integridade dos dados.
 */
public class Playlist {
    private final String id;
    private final String url;
    private final String title;
    private final LocalDateTime lastSyncedAt;
    private final int videoCount;

    private Playlist(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "ID não pode ser nulo");
        this.url = Objects.requireNonNull(builder.url, "URL não pode ser nula");
        this.title = builder.title;
        this.lastSyncedAt = builder.lastSyncedAt;
        this.videoCount = builder.videoCount;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public Playlist updateSyncTime(int newVideoCount) {
        return new Builder()
                .id(this.id)
                .url(this.url)
                .title(this.title)
                .lastSyncedAt(LocalDateTime.now())
                .videoCount(newVideoCount)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playlist playlist = (Playlist) o;
        return Objects.equals(id, playlist.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", videoCount=" + videoCount +
                ", lastSyncedAt=" + lastSyncedAt +
                '}';
    }

    public static class Builder {
        private String id;
        private String url;
        private String title;
        private LocalDateTime lastSyncedAt;
        private int videoCount = 0;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder lastSyncedAt(LocalDateTime lastSyncedAt) {
            this.lastSyncedAt = lastSyncedAt;
            return this;
        }

        public Builder videoCount(int videoCount) {
            this.videoCount = videoCount;
            return this;
        }

        public Playlist build() {
            return new Playlist(this);
        }
    }
}
