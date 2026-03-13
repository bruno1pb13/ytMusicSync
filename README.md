# YT Music Sync

![Tests](https://github.com/bruno1pb13/ytMusicSync/actions/workflows/pr-tests.yml/badge.svg)
![CI](https://github.com/bruno1pb13/ytMusicSync/actions/workflows/ci.yml/badge.svg)

Baixar playlists do YouTube automaticamente usando yt-dlp.

## Pré-requisitos

- Java 21+
- Maven 3.6+ (ou use o Maven integrado da sua IDE)
- yt-dlp instalado: `pip install yt-dlp`
- FFmpeg (para conversão de áudio)

## Instalação

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

### Modos de Execução

A aplicação suporta dois modos de execução:

**Interface Gráfica (padrão)**
```bash
java -jar target/ytMusicSync-1.0.0.jar
```
- Inicia automaticamente a interface gráfica
- Fallback automático para CLI se GUI não estiver disponível (ambiente headless)

**Modo CLI**
```bash
java -jar target/ytMusicSync-1.0.0.jar --cli
```
- Força o modo linha de comando
- Útil para servidores ou ambientes sem interface gráfica

## Arquivos e Diretórios

A aplicação armazena todos os seus dados nos diretórios padrão do usuário, seguindo as convenções de cada sistema operacional.

### Linux / macOS

| Finalidade         | Caminho                                         |
|--------------------|-------------------------------------------------|
| Configuração       | `~/.config/ytmusicsync/config.properties`       |
| Dados (playlists)  | `~/.local/share/ytmusicsync/data/playlists.json` |
| Dados (vídeos)     | `~/.local/share/ytmusicsync/data/videos.json`   |
| Downloads          | `~/Music/ytMusicSync/`                          |

As variáveis XDG são respeitadas quando definidas (`XDG_CONFIG_HOME`, `XDG_DATA_HOME`, `XDG_MUSIC_DIR`).

### Windows

| Finalidade         | Caminho                                              |
|--------------------|------------------------------------------------------|
| Configuração       | `%APPDATA%\ytmusicsync\config.properties`            |
| Dados (playlists)  | `%APPDATA%\ytmusicsync\data\playlists.json`          |
| Dados (vídeos)     | `%APPDATA%\ytmusicsync\data\videos.json`             |
| Downloads          | `%USERPROFILE%\Music\ytMusicSync\`                   |

Todos os diretórios são criados automaticamente na primeira execução.

## Configuração

O arquivo `config.properties` é criado automaticamente com valores padrão. As configurações disponíveis estão documentadas em [`config.properties.example`](config.properties.example).

### Variáveis de ambiente opcionais (Linux/macOS)

| Variável          | Efeito                                        |
|-------------------|-----------------------------------------------|
| `XDG_CONFIG_HOME` | Sobrescreve o diretório de configuração       |
| `XDG_DATA_HOME`   | Sobrescreve o diretório de dados              |
| `XDG_MUSIC_DIR`   | Sobrescreve o diretório padrão de downloads   |

Exemplo:
```bash
XDG_MUSIC_DIR=/mnt/nas/music java -jar ytMusicSync-1.0.0.jar
```

## Testes

```bash
mvn test
```

### CI/CD

- **PR Tests**: Executa testes unitários e build em pull requests
- **Continuous Integration**: Valida compilação e testes em todos os pushes

## Roadmap

- [ ] Adicionar estatísticas detalhadas de downloads
- [ ] Suporte para exportação de relatórios

## Licença

MIT License
