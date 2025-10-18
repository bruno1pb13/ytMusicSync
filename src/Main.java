/**
 * Ponto de entrada da aplicação YT Music Sync.
 *
 * Esta aplicação permite sincronizar playlists do YouTube,
 * mantendo conteúdo atualizado com verificações periódicas
 * e downloads através do yt-dlp.
 */

import application.Application;
import ui.UIManager;

public class Main {
    public static void main(String[] args) {
        Application app = new Application();

        // Inicializa a interface gráfica
        UIManager uiManager = new UIManager(app);
        uiManager.initialize();

        // Inicia a aplicação (modo CLI em background)
        app.start();
    }
}
