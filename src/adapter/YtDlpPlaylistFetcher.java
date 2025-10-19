package adapter;

import domain.Video;
import com.google.gson.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;

/**
 * Implementação de PlaylistFetcher usando yt-dlp.
 * Executa comandos do yt-dlp e parseia a saída JSON.
 */
public class YtDlpPlaylistFetcher implements PlaylistFetcher {
    private final String ytDlpPath;
    private final Gson gson;
    private final boolean cookiesEnabled;
    private final String cookiesBrowser;

    public YtDlpPlaylistFetcher(String ytDlpPath, boolean cookiesEnabled, String cookiesBrowser) {
        this.ytDlpPath = ytDlpPath;
        this.gson = new GsonBuilder().create();
        this.cookiesEnabled = cookiesEnabled;
        this.cookiesBrowser = cookiesBrowser;
    }

    @Override
    public List<Video> fetchVideos(String playlistUrl) {
        List<Video> videos = new ArrayList<>();
        String playlistId = extractPlaylistId(playlistUrl);

        try {
            List<String> command = new ArrayList<>();
            command.add(ytDlpPath);
            command.add("--flat-playlist");
            command.add("--dump-json");
            if (cookiesEnabled) {
                command.add("--cookies-from-browser");
                command.add(cookiesBrowser);
            }
            command.add(playlistUrl);

            ProcessBuilder pb = new ProcessBuilder(command);

            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    JsonObject obj = gson.fromJson(line, JsonObject.class);
                    Video video = parseVideo(obj, playlistId);
                    videos.add(video);
                } catch (JsonSyntaxException e) {
                    System.err.println("Erro ao parsear JSON: " + e.getMessage());
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    System.err.println("yt-dlp erro: " + errorLine);
                }
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Erro ao buscar vídeos da playlist: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        return videos;
    }

    @Override
    public String extractPlaylistId(String playlistUrl) {
        Pattern pattern = Pattern.compile("list=([a-zA-Z0-9_-]+)");
        Matcher matcher = pattern.matcher(playlistUrl);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return Integer.toHexString(playlistUrl.hashCode());
    }

    @Override
    public PlaylistInfo fetchPlaylistInfo(String playlistUrl) {
        try {
            List<String> command = new ArrayList<>();
            command.add(ytDlpPath);
            command.add("--flat-playlist");
            command.add("--dump-json");
            command.add("--playlist-end");
            command.add("1");
            if (cookiesEnabled) {
                command.add("--cookies-from-browser");
                command.add(cookiesBrowser);
            }
            command.add(playlistUrl);

            ProcessBuilder pb = new ProcessBuilder(command);

            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = reader.readLine();
            if (line != null) {
                JsonObject obj = gson.fromJson(line, JsonObject.class);

                String title = obj.has("playlist_title")
                        ? obj.get("playlist_title").getAsString()
                        : "Playlist sem título";

                int videoCount = obj.has("playlist_count")
                        ? obj.get("playlist_count").getAsInt()
                        : 0;

                String id = extractPlaylistId(playlistUrl);

                process.waitFor();
                return new PlaylistInfo(id, title, videoCount);
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Erro ao buscar informações da playlist: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        String id = extractPlaylistId(playlistUrl);
        return new PlaylistInfo(id, "Desconhecida", 0);
    }

    private Video parseVideo(JsonObject obj, String playlistId) {
        String id = obj.has("id") ? obj.get("id").getAsString() : UUID.randomUUID().toString();
        String title = obj.has("title") ? obj.get("title").getAsString() : "Sem título";
        String url = obj.has("url") ? obj.get("url").getAsString() : "";

        if (!url.startsWith("http")) {
            url = "https://www.youtube.com/watch?v=" + id;
        }

        Video.Builder builder = new Video.Builder()
                .id(id)
                .title(title)
                .url(url)
                .playlistId(playlistId);

        if (obj.has("upload_date")) {
            try {
                String uploadDate = obj.get("upload_date").getAsString();
                LocalDateTime publishedAt = LocalDate.parse(
                        uploadDate,
                        DateTimeFormatter.ofPattern("yyyyMMdd")
                ).atStartOfDay();
                builder.publishedAt(publishedAt);
            } catch (Exception e) {
                // Ignora erro de parsing de data
            }
        }

        return builder.build();
    }
}
