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

## Testes

```bash
mvn test
```

### CI/CD

- **PR Tests**: Executa testes unitários e build em pull requests
- **Continuous Integration**: Valida compilação e testes em todos os pushes

## Uso

### Configurações

O arquivo `config.properties` permite personalizar:

- `download.directory`: Diretório de destino dos downloads
- `check.interval.minutes`: Intervalo entre verificações automáticas (padrão: 60 minutos)
- `yt-dlp.path`: Caminho para o executável yt-dlp (padrão: yt-dlp)
- `audio.format`: Formato de áudio - mp3, m4a ou opus (padrão: mp3)
- `audio.quality`: Qualidade do áudio em kbps (padrão: 192)

## Roadmap

- [ ] Adicionar estatísticas detalhadas de downloads
- [ ] Suporte para exportação de relatórios

## Licença

MIT License
