/**
 * Ponto de entrada da aplicação YT Music Sync.
 *
 * Esta aplicação permite sincronizar playlists do YouTube,
 * mantendo conteúdo atualizado com verificações periódicas
 * e downloads através do yt-dlp.
 */

import application.Application;
import ui.TrayManager;

public class Main {
    public static void main(String[] args) {
        Application app = new Application();
        new TrayManager(app).initTray();

        app.start();

    }
}
