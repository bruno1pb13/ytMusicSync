#!/bin/bash

# Script para baixar dependências necessárias

mkdir -p lib

echo "Baixando Gson..."
curl -L "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar" -o lib/gson-2.10.1.jar

echo ""
echo "✓ Dependências baixadas em lib/"
echo ""
echo "Para compilar:"
echo "  javac -cp \"lib/*:src\" -d out src/**/*.java src/*.java"
echo ""
echo "Para executar:"
echo "  java -cp \"lib/*:out\" Main"
