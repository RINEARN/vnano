#!/bin/sh

# --------------------------------------------------
# compile source files
# --------------------------------------------------

mkdir bin
cd src
javac @sourcelist.txt -d ../bin -encoding UTF-8
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
