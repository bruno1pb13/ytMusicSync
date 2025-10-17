# YT Music Sync

![Tests](https://github.com/bruno1pb13/ytMusicSync/actions/workflows/pr-tests.yml/badge.svg)
![CI](https://github.com/bruno1pb13/ytMusicSync/actions/workflows/ci.yml/badge.svg)

Aplicação Java para sincronizar e baixar playlists do YouTube automaticamente usando yt-dlp.

## Características

- Sincronização automática de playlists do YouTube
- Download de áudio em formatos configuráveis (MP3, M4A, OPUS)
- Rastreamento de vídeos já baixados
- Interface CLI interativa
- Persistência em JSON
- Configuração através de arquivo properties

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

test/
├── domain/              # Testes das entidades
│   ├── VideoTest.java
│   └── PlaylistTest.java
├── repository/          # Testes de persistência
│   ├── JsonVideoRepositoryTest.java
│   └── JsonPlaylistRepositoryTest.java
└── service/             # Testes de lógica de negócio
    └── SyncServiceTest.java
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

## Testes

O projeto possui 27 testes unitários cobrindo Domain, Repository e Service layers. Veja [TESTING.md](TESTING.md) para detalhes completos.

**Via IntelliJ IDEA:**
- Clique com botão direito na pasta `test` → Run 'All Tests'

**Via Maven:**
```bash
mvn test
```

### CI/CD

- **PR Tests**: Executa testes unitários e build em pull requests
- **Continuous Integration**: Valida compilação e testes em todos os pushes

## Uso

### Menu Principal

1. Adicionar Playlist
2. Listar Playlists
3. Remover Playlist
4. Sincronizar Agora
5. Iniciar Sincronização Automática
6. Parar Sincronização Automática
7. Configurações

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

## Tecnologias

- Java 21
- Maven
- JUnit 5 + Mockito
- Gson (JSON)
- yt-dlp
- GitHub Actions

## Licença

MIT License
