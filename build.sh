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

jar cvfm Vnano.jar src/org/vcssl/nano/meta/main.mf -C bin org -C src/org/vcssl/nano/meta META-INF
