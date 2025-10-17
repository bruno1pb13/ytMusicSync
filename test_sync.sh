#!/bin/bash

# Script para testar sincronização
cd /home/deck/IdeaProjects/ytMusicSync

# Compila se necessário
if [ ! -f out/Main.class ]; then
    echo "Compilando..."
    ./compile.sh
fi

# Executa teste
java -cp "out:lib/*" Main << EOF
1
https://music.youtube.com/playlist?list=PL2xxwp4J4DM8ODfRNyI6eb4Jqw4_xLdzK
n
4
1
0
EOF
