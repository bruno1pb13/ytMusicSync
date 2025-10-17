package repository;

import domain.Playlist;
import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementação de PlaylistRepository usando JSON.
 * Thread-safe com ConcurrentHashMap.
 */
public class JsonPlaylistRepository implements PlaylistRepository {
    private static final String DATA_FILE = "data/playlists.json";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Map<String, Playlist> cache = new ConcurrentHashMap<>();
    private final Gson gson;

    public JsonPlaylistRepository() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
        loadFromFile();
    }

    @Override
    public void save(Playlist playlist) {
        cache.put(playlist.getId(), playlist);
        saveToFile();
    }

    @Override
    public Optional<Playlist> findById(String id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public Optional<Playlist> findByUrl(String url) {
        return cache.values().stream()
                .filter(p -> p.getUrl().equals(url))
                .findFirst();
    }

    @Override
    public List<Playlist> findAll() {
        return new ArrayList<>(cache.values());
    }

    @Override
    public void delete(String id) {
        cache.remove(id);
        saveToFile();
    }

    @Override
    public boolean exists(String id) {
        return cache.containsKey(id);
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

            if (root != null && root.has("playlists")) {
                JsonArray playlists = root.getAsJsonArray("playlists");
                for (JsonElement element : playlists) {
                    Playlist playlist = deserializePlaylist(element.getAsJsonObject());
                    cache.put(playlist.getId(), playlist);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar playlists: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try {
            JsonObject root = new JsonObject();
            JsonArray playlists = new JsonArray();

            for (Playlist playlist : cache.values()) {
                playlists.add(serializePlaylist(playlist));
            }

            root.add("playlists", playlists);

            Path path = Paths.get(DATA_FILE);
            Files.createDirectories(path.getParent());
            Files.writeString(path, gson.toJson(root));
        } catch (IOException e) {
            System.err.println("Erro ao salvar playlists: " + e.getMessage());
        }
    }

    private JsonObject serializePlaylist(Playlist playlist) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", playlist.getId());
        obj.addProperty("url", playlist.getUrl());
        obj.addProperty("title", playlist.getTitle());
        obj.addProperty("videoCount", playlist.getVideoCount());
        if (playlist.getLastSyncedAt() != null) {
            obj.addProperty("lastSyncedAt", playlist.getLastSyncedAt().format(FORMATTER));
        }
        return obj;
    }

    private Playlist deserializePlaylist(JsonObject obj) {
        Playlist.Builder builder = new Playlist.Builder()
                .id(obj.get("id").getAsString())
                .url(obj.get("url").getAsString())
                .videoCount(obj.has("videoCount") ? obj.get("videoCount").getAsInt() : 0);

        if (obj.has("title")) {
            builder.title(obj.get("title").getAsString());
        }

        if (obj.has("lastSyncedAt")) {
            builder.lastSyncedAt(LocalDateTime.parse(obj.get("lastSyncedAt").getAsString(), FORMATTER));
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
