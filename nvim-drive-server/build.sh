#!/bin/bash

bold_green='\033[1;32m'
bold_red='\003[1;31m' 
reset='\033[0m'

TARGET_DIR="target"
DEST_DIR="../nvim-drive/"

mvn clean package > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo -e "[${bold_green}DEPLOY${reset}]Proyecto empaquetado por Maven."
else
    exit 1
fi

JAR_FILE=$(find "$TARGET_DIR" -name "*.jar" -type f ! -name "original-*.jar" | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo -e "[${bold_red}ERROR${reset}]No se encontró ningún archivo JAR en $TARGET_DIR."
    exit 1
fi

echo -e "[${bold_green}DEPLOY${reset}]Moviendo el archivo JAR a $DEST_DIR."
mv "$JAR_FILE" "$DEST_DIR"

if [ $? -eq 0 ]; then
    echo -e "[${bold_green}DEPLOY${reset}]El archivo JAR se movió correctamente a $DEST_DIR."
else
    echo -e "[${bold_red}ERROR${reset}]Hubo un error al mover el archivo JAR."
    exit 1
fi

if mvn clean > /dev/null 2>&1; then
    echo -e "[${bold_green}DEPLOY${reset}]Se ha ejecutado mvn clean en el proyecto."
fi
