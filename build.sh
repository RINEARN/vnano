#!/bin/sh

# ==================================================
# A shell script for building Vnano Engine
# ==================================================

# --------------------------------------------------
# compile source files
# --------------------------------------------------

mkdir bin
cd src
javac @org/vcssl/nano/sourcelist.txt -d ../bin -encoding UTF-8
cd ..

# --------------------------------------------------
# copy meta files
# --------------------------------------------------

mkdir bin/META-INF
cp -r src/META-INF/. bin/META-INF/.

# --------------------------------------------------
# create a JAR file
# --------------------------------------------------

jar cvfm Vnano.jar src/main.mf -C bin org -C bin META-INF/services
