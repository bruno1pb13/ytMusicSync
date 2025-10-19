package exception;

/**
 * Exceção lançada quando uma playlist privada é acessada sem autenticação adequada.
 */
public class PrivatePlaylistException extends RuntimeException {
    private final int videoCount;
    private final boolean cookiesEnabled;

    public PrivatePlaylistException(String message, int videoCount, boolean cookiesEnabled) {
        super(message);
        this.videoCount = videoCount;
        this.cookiesEnabled = cookiesEnabled;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public boolean isCookiesEnabled() {
        return cookiesEnabled;
    }
}
