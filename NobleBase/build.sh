#!/bin/bash

# Compile from project root - no cd into src
javac src/NobleBase.java
mv src/NobleBase.class build/NobleBase.class

cd build

# Change the last argument to the path to your database
java NobleBase 5477 ../db/db.txt
cd ..
