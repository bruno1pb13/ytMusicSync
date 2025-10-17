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

### CI/CD - GitHub Actions

O projeto utiliza GitHub Actions para garantir qualidade do código:

- **PR Tests** (`.github/workflows/pr-tests.yml`): Executa automaticamente em PRs para `main`
  - ✅ Roda todos os testes unitários
  - ✅ Gera relatório de testes
  - ✅ Faz build do projeto
  - ✅ Upload de artefatos (JAR e test reports)

- **Continuous Integration** (`.github/workflows/ci.yml`): Executa em todos os pushes
  - ✅ Valida projeto Maven
  - ✅ Compila código
  - ✅ Roda testes
  - ✅ Verifica package
  - ✅ Quality gate

**Status Badges:**
```markdown
![Tests](https://github.com/seu-usuario/ytMusicSync/actions/workflows/pr-tests.yml/badge.svg)
![CI](https://github.com/seu-usuario/ytMusicSync/actions/workflows/ci.yml/badge.svg)
```

### Boas Práticas Implementadas

- **Arrange-Act-Assert (AAA)**: Estrutura clara dos testes
- **DisplayName**: Descrições em português para melhor legibilidade
- **Mocks com Mockito**: Isolamento de dependências nos testes de serviço
- **Setup/Teardown**: Limpeza de arquivos de teste antes e depois
- **Testes concisos**: Cada teste valida um comportamento específico

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

- [x] Adicionar testes unitários
- [ ] Adicionar testes de integração
- [ ] Implementar logging estruturado (SLF4J)
- [ ] Suporte a banco de dados SQL
- [ ] API REST para integração
- [ ] Interface gráfica (JavaFX/Swing)
- [ ] Retry logic para downloads falhados
- [ ] Notificações (email, webhook)
- [ ] Métricas e monitoring
- [ ] Cobertura de código (JaCoCo)

## Licença

MIT License
