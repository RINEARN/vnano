:: ==================================================
:: A batch file for building Vnano Engine
:: ==================================================

:: --------------------------------------------------
:: compile source files
:: --------------------------------------------------

mkdir bin
cd src
javac @org/vcssl/nano/sourcelist.txt -d ../bin -encoding UTF-8
cd ..

:: --------------------------------------------------
:: copy meta files
:: --------------------------------------------------

xcopy src\META-INF bin\META-INF /I/S/E/Y

:: --------------------------------------------------
:: create a JAR file
:: --------------------------------------------------

jar cvfm Vnano.jar src/main.mf -C bin org -C bin META-INF/services
