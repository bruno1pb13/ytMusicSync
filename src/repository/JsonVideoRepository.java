package repository;

import domain.Video;
import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementação de VideoRepository usando JSON.
 * Thread-safe com ConcurrentHashMap.
 */
public class JsonVideoRepository implements VideoRepository {
    private static final String DATA_FILE = "data/videos.json";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Map<String, Video> cache = new ConcurrentHashMap<>();
    private final Gson gson;

    public JsonVideoRepository() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
        loadFromFile();
    }

    @Override
    public void save(Video video) {
        cache.put(video.getId(), video);
        saveToFile();
    }

    @Override
    public Optional<Video> findById(String id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public List<Video> findByPlaylistId(String playlistId) {
        return cache.values().stream()
                .filter(v -> v.getPlaylistId().equals(playlistId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Video> findNotDownloadedByPlaylistId(String playlistId) {
        return cache.values().stream()
                .filter(v -> v.getPlaylistId().equals(playlistId))
                .filter(v -> !v.isDownloaded())
                .collect(Collectors.toList());
    }

    @Override
    public boolean exists(String id) {
        return cache.containsKey(id);
    }

    @Override
    public void delete(String id) {
        cache.remove(id);
        saveToFile();
    }

    @Override
    public int countByPlaylistId(String playlistId) {
        return (int) cache.values().stream()
                .filter(v -> v.getPlaylistId().equals(playlistId))
                .count();
    }

    private void loadFromFile() {
        try {
            Path path = Paths.get(DATA_FILE);
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                return;
            }

            String json = Files.readString(path);
            JsonObject root = gson.fromJson(json, JsonObject.class);

            if (root != null && root.has("videos")) {
                JsonArray videos = root.getAsJsonArray("videos");
                for (JsonElement element : videos) {
                    Video video = deserializeVideo(element.getAsJsonObject());
                    cache.put(video.getId(), video);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar vídeos: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try {
            JsonObject root = new JsonObject();
            JsonArray videos = new JsonArray();

            for (Video video : cache.values()) {
                videos.add(serializeVideo(video));
            }

            root.add("videos", videos);

            Path path = Paths.get(DATA_FILE);
            Files.createDirectories(path.getParent());
            Files.writeString(path, gson.toJson(root));
        } catch (IOException e) {
            System.err.println("Erro ao salvar vídeos: " + e.getMessage());
        }
    }

    private JsonObject serializeVideo(Video video) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", video.getId());
        obj.addProperty("title", video.getTitle());
        obj.addProperty("url", video.getUrl());
        obj.addProperty("playlistId", video.getPlaylistId());
        obj.addProperty("downloaded", video.isDownloaded());

        if (video.getPublishedAt() != null) {
            obj.addProperty("publishedAt", video.getPublishedAt().format(FORMATTER));
        }

        if (video.getDownloadedAt() != null) {
            obj.addProperty("downloadedAt", video.getDownloadedAt().format(FORMATTER));
        }

        return obj;
    }

    private Video deserializeVideo(JsonObject obj) {
        Video.Builder builder = new Video.Builder()
                .id(obj.get("id").getAsString())
                .title(obj.get("title").getAsString())
                .url(obj.get("url").getAsString())
                .playlistId(obj.get("playlistId").getAsString())
                .downloaded(obj.has("downloaded") && obj.get("downloaded").getAsBoolean());

        if (obj.has("publishedAt")) {
            builder.publishedAt(LocalDateTime.parse(obj.get("publishedAt").getAsString(), FORMATTER));
        }

        if (obj.has("downloadedAt")) {
            builder.downloadedAt(LocalDateTime.parse(obj.get("downloadedAt").getAsString(), FORMATTER));
        }

        return builder.build();
    }

    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.format(FORMATTER));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) {
            return LocalDateTime.parse(json.getAsString(), FORMATTER);
        }
    }
}
