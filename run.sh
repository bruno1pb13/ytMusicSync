#!/bin/bash

if [ ! -d "out" ] || [ ! -f "out/Main.class" ]; then
    echo "Projeto não compilado. Execute ./build.sh primeiro"
    exit 1
fi

java -cp "lib/*:out" Main
