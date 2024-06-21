#!/bin/sh

# ==================================================
# A shell script for building Vnano Engine
# ==================================================

# --------------------------------------------------
# compile source files
# --------------------------------------------------

mkdir bin
cd src
javac -Xlint:all -d ../bin -encoding UTF-8 @org/vcssl/connect/sourcelist.txt
javac -Xlint:all -d ../bin -encoding UTF-8 @org/vcssl/nano/sourcelist.txt
cd ..

# --------------------------------------------------
# create a JAR file
# --------------------------------------------------

jar cvfm Vnano.jar src/org/vcssl/nano/meta/main.mf -C bin org -C src/org/vcssl/nano/meta META-INF
