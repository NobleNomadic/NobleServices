#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# Define source and build directories
SRC_DIR="src"
BUILD_DIR="build"
JAR_NAME="NobleServer.jar"
MAIN_CLASS="NobleServer"

# Create build directory if it doesn't exist
mkdir -p "$BUILD_DIR"

# Compile Java source code
javac "$SRC_DIR/$MAIN_CLASS.java" -d "$BUILD_DIR"

# Package compiled classes into a JAR
cd "$BUILD_DIR"
jar cfe "../$JAR_NAME" "$MAIN_CLASS" "$MAIN_CLASS.class" "$MAIN_CLASS\$ClientConnection.class"
mv ../NobleServer.jar ../build
cd ../build

# Run the JAR file (Change these arguments to fit your server)
java -jar "$JAR_NAME" 8008 ../route/route.txt
