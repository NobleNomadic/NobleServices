#!/bin/bash

# Exit immediately on error
set -e

# Directory definitions
SRC_DIR="src"
BUILD_DIR="build"
JAR_NAME="NobleBase.jar"
MAIN_CLASS="NobleBase"

# Create build directory
mkdir -p "$BUILD_DIR"

# Compile all Java source files
javac "$SRC_DIR"/*.java -d "$BUILD_DIR"

# Package compiled code into JAR
cd "$BUILD_DIR"
jar cfe "../$JAR_NAME" "$MAIN_CLASS" *.class

# Change to build directory and run the JAR
cd ..
mv "$JAR_NAME" "$BUILD_DIR/"
cd "$BUILD_DIR"
# Change arguments to fit your server
java -jar "$JAR_NAME" 5477 ../db/db.db
