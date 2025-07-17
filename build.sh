#!/bin/bash
cd src/main
gcc -o nobleserver main.c ../header/httplib.c ../header/socketlib.c
mv nobleserver ../../build/nobleserver
