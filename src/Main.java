/**
 * Ponto de entrada da aplicação YT Music Sync.
 *
 * Esta aplicação permite sincronizar playlists do YouTube,
 * mantendo conteúdo atualizado com verificações periódicas
 * e downloads através do yt-dlp.
 *
 * Arquitetura baseada em princípios SOLID:
 * - Single Responsibility: Cada classe tem uma única responsabilidade
 * - Open/Closed: Extensível via interfaces
 * - Liskov Substitution: Implementações intercambiáveis
 * - Interface Segregation: Interfaces específicas e focadas
 * - Dependency Inversion: Dependências de abstrações, não implementações
 */
public class Main {
    public static void main(String[] args) {
        Application app = new Application();
        app.start();
    }
}
