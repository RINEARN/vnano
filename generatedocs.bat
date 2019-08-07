:: ----------------------------------------------------------------------------------------------------
:: API reference documents will be generated in ./api directory by running this script.
:: After run this script, please open ./api/index.html by a web browser.
:: 
:: If you want to read only English contents, please append the following line in api/stylesheet.css:
:: 
::    .lang-ja { display: none; }
:: 
:: If you want to read only Japanese contents, please append the following line in api/stylesheet.css:
:: 
::    .lang-en { display: none; }
:: 
:: ----------------------------------------------------------------------------------------------------

mkdir api
mkdir api-all

cd src

javadoc -encoding UTF-8 -charset UTF-8 -windowtitle "Vnano API Reference (Public Only)" -d ../api @sourcelist.txt
javadoc -private -encoding UTF-8 -charset UTF-8 -windowtitle "Vnano API Reference" -d ../api-all @sourcelist.txt

cd ..









echo off
echo ====================================================================================================
echo. 
echo. 
echo. 
echo. 
echo. 
echo. 
echo. 
echo. 
echo. 
echo. 
echo Documents have been generated. See: api/org/vcssl/nano/package-summary.html
echo.
echo By default, both of English and Japanese contents will be displayed in each page.
echo If you want to read only English contents, please append the following line 
echo in "api/stylesheet.css" and "api-all/stylesheet.css" :
echo.
echo 	.lang-ja { display: none; }
echo.
echo If you want to read only Japanese contents, please append the following line 
echo in "api/stylesheet.css" and "api-all/stylesheet.css":
echo.
echo 	.lang-en { display: none; }
echo.
echo. 
echo. 
echo. 
echo. 

pause
