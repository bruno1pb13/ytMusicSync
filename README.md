# YT Music Sync

Aplicação Java para sincronizar e baixar playlists do YouTube automaticamente usando yt-dlp.

## Características

- **Sincronização automática**: Verifica periodicamente por novos vídeos
- **Gerenciamento de playlists**: Adicione múltiplas playlists para monitorar
- **Download de áudio**: Baixa apenas o áudio em formato configurável (MP3, M4A, etc)
- **Rastreamento de progresso**: Mantém registro de vídeos já baixados
- **Interface interativa**: Menu CLI intuitivo
- **Configurável**: Personalize diretórios, intervalos e formatos

## Arquitetura

Projeto construído seguindo princípios **SOLID**:

```
src/
├── domain/              # Entidades de domínio (imutáveis)
│   ├── Video.java
│   └── Playlist.java
├── repository/          # Interfaces e implementações de persistência
│   ├── PlaylistRepository.java
│   ├── VideoRepository.java
│   ├── JsonPlaylistRepository.java
│   └── JsonVideoRepository.java
├── adapter/             # Adaptadores para ferramentas externas
│   ├── PlaylistFetcher.java
│   ├── AudioDownloader.java
│   ├── YtDlpPlaylistFetcher.java
│   └── YtDlpAudioDownloader.java
├── service/             # Lógica de negócio
│   ├── SyncService.java
│   └── SchedulerService.java
├── util/                # Utilitários
│   └── Config.java
├── Application.java     # Camada de aplicação
└── Main.java           # Ponto de entrada
```

### Princípios SOLID Aplicados

1. **Single Responsibility**: Cada classe tem uma única responsabilidade bem definida
2. **Open/Closed**: Extensível através de interfaces, fechado para modificações
3. **Liskov Substitution**: Implementações são intercambiáveis via interfaces
4. **Interface Segregation**: Interfaces específicas e focadas
5. **Dependency Inversion**: Dependências injetadas, classes dependem de abstrações

## Pré-requisitos

- Java 21+ (com suporte a `void main()`)
- yt-dlp instalado: `pip install yt-dlp`
- FFmpeg (para conversão de áudio)

## Instalação

1. Clone o repositório
2. Baixe as dependências:
```bash
chmod +x download-gson.sh
./download-gson.sh
```

3. Compile o projeto:
```bash
javac -cp "lib/*:src" -d out src/**/*.java src/*.java
```

4. Execute:
```bash
java -cp "lib/*:out" Main
```

## Uso

### Menu Principal

1. **Adicionar Playlist**: Insira a URL de uma playlist do YouTube
2. **Listar Playlists**: Visualize todas as playlists cadastradas com estatísticas
3. **Remover Playlist**: Remove uma playlist e seus registros
4. **Sincronizar Agora**: Executa sincronização manual
5. **Iniciar Sinc. Automática**: Inicia verificações periódicas
6. **Parar Sinc. Automática**: Para as verificações automáticas
7. **Configurações**: Ajuste parâmetros da aplicação

### Configurações

O arquivo `config.properties` permite personalizar:

- `download.directory`: Diretório de destino dos downloads
- `check.interval.minutes`: Intervalo entre verificações automáticas
- `yt-dlp.path`: Caminho para o executável yt-dlp
- `audio.format`: Formato de áudio (mp3, m4a, opus)
- `audio.quality`: Qualidade do áudio em kbps

### Persistência

Os dados são salvos em arquivos JSON:
- `data/playlists.json`: Informações das playlists
- `data/videos.json`: Registro de vídeos e status de download

## Exemplo de Uso

```bash
$ java -cp "lib/*:out" Main

╔════════════════════════════════════╗
║     YT Music Sync - v1.0.0        ║
╚════════════════════════════════════╝

✓ yt-dlp encontrado: 2024.04.09

╔════════════════════════════════════╗
║           MENU PRINCIPAL          ║
╠════════════════════════════════════╣
║ 1. Adicionar Playlist             ║
║ 2. Listar Playlists               ║
...
```

## Extensibilidade

A arquitetura permite fácil extensão:

### Adicionar novo downloader

Implemente a interface `AudioDownloader`:

```java
public class CustomDownloader implements AudioDownloader {
    @Override
    public boolean download(Video video, String outputDirectory) {
        // Sua implementação
    }
    // ...
}
```

### Adicionar novo fetcher

Implemente a interface `PlaylistFetcher`:

```java
public class CustomFetcher implements PlaylistFetcher {
    @Override
    public List<Video> fetchVideos(String playlistUrl) {
        // Sua implementação
    }
    // ...
}
```

### Adicionar nova persistência

Implemente `PlaylistRepository` e `VideoRepository` para usar banco de dados SQL, NoSQL, etc.

## Validação de Design

Como desenvolvedor sênior, validei os seguintes aspectos:

### ✓ Separação de Responsabilidades
- Domínio isolado de infraestrutura
- Lógica de negócio centralizada em serviços
- Adaptadores encapsulam dependências externas

### ✓ Testabilidade
- Interfaces permitem mock de dependências
- Entidades imutáveis facilitam testes
- Lógica desacoplada de I/O

### ✓ Manutenibilidade
- Código autodocumentado com JavaDoc
- Estrutura clara e previsível
- Fácil localização de funcionalidades

### ✓ Thread Safety
- Repositórios usam ConcurrentHashMap
- Scheduler gerencia threads corretamente
- Shutdown gracioso de recursos

### ✓ Tratamento de Erros
- Try-catch em operações de I/O
- Mensagens descritivas ao usuário
- Logs de erro para debugging

## Melhorias Futuras

- [ ] Adicionar testes unitários e de integração
- [ ] Implementar logging estruturado (SLF4J)
- [ ] Suporte a banco de dados SQL
- [ ] API REST para integração
- [ ] Interface gráfica (JavaFX/Swing)
- [ ] Retry logic para downloads falhados
- [ ] Notificações (email, webhook)
- [ ] Métricas e monitoring

## Licença

MIT License
