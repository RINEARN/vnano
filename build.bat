: --------------------------------------------------
: compile source files
: --------------------------------------------------

mkdir bin
cd src
javac @sourcelist.txt -d ../bin -encoding UTF-8
cd ..

: --------------------------------------------------
: copy meta files
: --------------------------------------------------

xcopy src\META-INF bin\META-INF /I/S/E/Y

: --------------------------------------------------
: create a JAR file
: --------------------------------------------------

jar cvf Vnano.jar -C bin org -C bin META-INF/services
