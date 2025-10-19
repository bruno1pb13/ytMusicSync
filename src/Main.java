/**
 * Ponto de entrada da aplicação YT Music Sync.
 *
 * Esta aplicação permite sincronizar playlists do YouTube,
 * mantendo conteúdo atualizado com verificações periódicas
 * e downloads através do yt-dlp.
 *
 * Modos de execução:
 * - --cli: Força o modo linha de comando (CLI)
 */

import application.Application;
import ui.UIManager;

import java.awt.GraphicsEnvironment;

public class Main {
    public static void main(String[] args) {
        Application app = new Application();

        // Verifica se foi solicitado modo CLI via argumento
        boolean forceCliMode = false;
        for (String arg : args) {
            if ("--cli".equals(arg)) {
                forceCliMode = true;
                break;
            }
        }

        // Se modo CLI foi forçado, inicia apenas CLI
        if (forceCliMode) {
            System.out.println("Iniciando em modo CLI (solicitado via --cli)");
            app.startCli();
            return;
        }

        // Tenta iniciar GUI (padrão)
        if (isGuiAvailable()) {
            try {
                UIManager uiManager = new UIManager(app);
                uiManager.initialize();
                // Não inicia o CLI quando GUI está ativa
            } catch (Exception e) {
                System.err.println("Erro ao iniciar interface gráfica: " + e.getMessage());
                System.out.println("Fazendo fallback para modo CLI...\n");
                app.startCli();
            }
        } else {
            // Ambiente headless - usa CLI automaticamente
            System.out.println("Interface gráfica não disponível (ambiente headless)");
            System.out.println("Iniciando em modo CLI...\n");
            app.startCli();
        }
    }

    /**
     * Verifica se a interface gráfica está disponível no ambiente.
     * @return true se GUI pode ser iniciada, false caso contrário
     */
    private static boolean isGuiAvailable() {
        return !GraphicsEnvironment.isHeadless();
    }
}
