# Setup e Instalação - YT Music Sync

## Instalação do Java

### Linux (Steam Deck / Arch)
```bash
sudo pacman -S jdk-openjdk
```

### Ubuntu/Debian
```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

### Fedora/RHEL
```bash
sudo dnf install java-21-openjdk-devel
```

### macOS (Homebrew)
```bash
brew install openjdk@21
```

### Windows
Baixe e instale do site oficial: https://adoptium.net/

## Verificação

Após instalar, verifique:
```bash
java -version   # Deve mostrar versão 21 ou superior
javac -version  # Deve mostrar versão 21 ou superior
```

## Instalação do yt-dlp

### Via pip (recomendado)
```bash
pip install yt-dlp
```

### Via package manager (Linux)
```bash
# Arch
sudo pacman -S yt-dlp

# Ubuntu/Debian
sudo apt install yt-dlp

# Fedora
sudo dnf install yt-dlp
```

### macOS
```bash
brew install yt-dlp
```

### Windows
```bash
pip install yt-dlp
# ou baixe o executável: https://github.com/yt-dlp/yt-dlp/releases
```

## Instalação do FFmpeg

O FFmpeg é necessário para converter áudio.

### Linux
```bash
# Arch
sudo pacman -S ffmpeg

# Ubuntu/Debian
sudo apt install ffmpeg

# Fedora
sudo dnf install ffmpeg
```

### macOS
```bash
brew install ffmpeg
```

### Windows
Baixe de: https://ffmpeg.org/download.html

## Build do Projeto

1. Clone o repositório:
```bash
git clone <seu-repo>
cd ytMusicSync
```

2. Baixe dependências:
```bash
chmod +x *.sh
./download-gson.sh
```

3. Compile:
```bash
./build.sh
```

4. Execute:
```bash
./run.sh
```

## Build via IntelliJ IDEA

Se estiver usando IntelliJ IDEA:

1. Abra o projeto
2. Vá em `File > Project Structure`
3. Configure o JDK 21+
4. Adicione `lib/gson-2.10.1.jar` às bibliotecas
5. Execute `Main.java`

## Troubleshooting

### "javac: command not found"
- Java JDK não está instalado ou não está no PATH
- Instale o JDK conforme instruções acima
- Verifique com `javac -version`

### "yt-dlp not found"
- yt-dlp não está instalado
- Instale com `pip install yt-dlp`
- Se instalado, ajuste o caminho em Configurações no app

### "ERROR: FFmpeg not found"
- FFmpeg não está instalado
- Instale conforme instruções acima
- Verifique com `ffmpeg -version`

### Erro ao baixar vídeos privados
- Vídeos privados ou removidos não podem ser baixados
- A sincronização registrará o erro e continuará

### Performance
- Downloads podem ser lentos dependendo da qualidade e conexão
- O intervalo padrão de sincronização é 60 minutos
- Ajuste nas configurações se necessário
