package adapter;

import domain.Video;
import util.Config;
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
    private final Config config;
    private final Gson gson;

    public YtDlpPlaylistFetcher(Config config) {
        this.config = config;
        this.gson = new GsonBuilder().create();
    }

    @Override
    public List<Video> fetchVideos(String playlistUrl) {
        List<Video> videos = new ArrayList<>();
        String playlistId = extractPlaylistId(playlistUrl);

        try {
            List<String> command = new ArrayList<>();
            command.add(config.getYtDlpPath());
            command.add("--flat-playlist");
            command.add("--dump-json");
            if (config.getCookiesEnabled()) {
                command.add("--cookies-from-browser");
                command.add(config.getCookiesBrowser());
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
    public boolean isChannelUrl(String url) {
        return url.contains("youtube.com/@") ||
               url.contains("youtube.com/c/") ||
               url.contains("youtube.com/channel/") ||
               url.contains("youtube.com/user/");
    }

    @Override
    public String extractPlaylistId(String playlistUrl) {
        // Playlist ID via list= parameter
        Pattern listPattern = Pattern.compile("list=([a-zA-Z0-9_-]+)");
        Matcher listMatcher = listPattern.matcher(playlistUrl);
        if (listMatcher.find()) {
            return listMatcher.group(1);
        }

        // Channel @handle
        Pattern handlePattern = Pattern.compile("youtube\\.com/@([a-zA-Z0-9_.-]+)");
        Matcher handleMatcher = handlePattern.matcher(playlistUrl);
        if (handleMatcher.find()) {
            return "@" + handleMatcher.group(1);
        }

        // /c/, /user/, /channel/
        Pattern channelPattern = Pattern.compile("youtube\\.com/(?:c|user|channel)/([a-zA-Z0-9_-]+)");
        Matcher channelMatcher = channelPattern.matcher(playlistUrl);
        if (channelMatcher.find()) {
            return channelMatcher.group(1);
        }

        return Integer.toHexString(playlistUrl.hashCode());
    }

    @Override
    public PlaylistInfo fetchPlaylistInfo(String playlistUrl) {
        try {
            List<String> command = new ArrayList<>();
            command.add(config.getYtDlpPath());
            command.add("--flat-playlist");
            command.add("--dump-json");
            command.add("--playlist-end");
            command.add("1");
            if (config.getCookiesEnabled()) {
                command.add("--cookies-from-browser");
                command.add(config.getCookiesBrowser());
            }
            command.add(playlistUrl);

            ProcessBuilder pb = new ProcessBuilder(command);

            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = reader.readLine();
            if (line != null) {
                JsonObject obj = gson.fromJson(line, JsonObject.class);

                String title = null;
                if (obj.has("playlist_title") && !obj.get("playlist_title").isJsonNull()) {
                    title = obj.get("playlist_title").getAsString();
                } else if (obj.has("channel") && !obj.get("channel").isJsonNull()) {
                    title = obj.get("channel").getAsString();
                } else if (obj.has("uploader") && !obj.get("uploader").isJsonNull()) {
                    title = obj.get("uploader").getAsString();
                }
                if (title == null || title.isBlank()) {
                    title = isChannelUrl(playlistUrl) ? "Canal sem título" : "Playlist sem título";
                }

                int videoCount = 0;
                if (obj.has("playlist_count") && !obj.get("playlist_count").isJsonNull()) {
                    videoCount = obj.get("playlist_count").getAsInt();
                }

                String id = extractPlaylistId(playlistUrl);

                process.waitFor();
                return new PlaylistInfo(id, title, videoCount);
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Erro ao buscar informações da playlist: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Erro ao parsear informações da playlist: " + e.getMessage());
        }

        String id = extractPlaylistId(playlistUrl);
        String fallbackTitle = isChannelUrl(playlistUrl) ? "Canal desconhecido" : "Desconhecida";
        return new PlaylistInfo(id, fallbackTitle, 0);
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
