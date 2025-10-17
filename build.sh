#!/bin/bash

echo "=== Building YT Music Sync ==="

# Cria diretórios
mkdir -p out lib

# Verifica se o Gson existe
if [ ! -f "lib/gson-2.10.1.jar" ]; then
    echo "Gson não encontrado. Executando download..."
    ./download-gson.sh
fi

# Compila
echo ""
echo "Compilando..."
javac -cp "lib/*:src" -d out \
    src/domain/*.java \
    src/repository/*.java \
    src/adapter/*.java \
    src/service/*.java \
    src/util/*.java \
    src/Application.java \
    src/Main.java

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Build concluído com sucesso!"
    echo ""
    echo "Para executar:"
    echo "  ./run.sh"
else
    echo ""
    echo "✗ Erro no build"
    exit 1
fi
