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


## Pré-requisitos

- Java 21+
- Maven 3.6+ (ou use o Maven integrado da sua IDE)
- yt-dlp instalado: `pip install yt-dlp`
- FFmpeg (para conversão de áudio)

## Instalação

### Usando IntelliJ IDEA (Recomendado)

1. Clone o repositório
2. Abra o projeto no IntelliJ IDEA
3. A IDE detectará automaticamente o `pom.xml` e baixará as dependências
4. Execute clicando no botão ▶️ ao lado do método `main` em `Main.java`

### Usando Maven (Linha de Comando)

1. Clone o repositório
2. Compile o projeto:
```bash
mvn clean compile
```

3. Execute:
```bash
mvn exec:java -Dexec.mainClass="Main"
```

Ou crie um JAR executável:
```bash
mvn clean package
java -jar target/ytMusicSync-1.0.0.jar
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
