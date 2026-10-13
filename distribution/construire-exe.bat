@echo off
rem Fabrique la version autonome d'ISO Stock : un dossier contenant
rem "ISO Stock.exe" et un Java embarque. Le client n'a RIEN a installer.
rem
rem Avant de lancer ce script, construire le JAR dans IntelliJ :
rem panneau Maven > Lifecycle > package. Il doit exister dans target\.
rem
rem Pas d'accents dans ce fichier : l'invite de commandes les affiche mal.

setlocal
cd /d "%~dp0\.."

if not exist "target\ISO-Stock.jar" (
    echo Le fichier target\ISO-Stock.jar est introuvable.
    echo Construisez-le d'abord : panneau Maven ^> Lifecycle ^> package.
    echo.
    pause
    exit /b 1
)

where jpackage >nul 2>nul
if errorlevel 1 (
    echo jpackage est introuvable : le dossier bin du JDK n'est pas dans le PATH.
    echo.
    pause
    exit /b 1
)

echo Preparation...
if exist "target\paquet" rmdir /s /q "target\paquet"
if exist "target\livraison" rmdir /s /q "target\livraison"
mkdir "target\paquet"
copy /y "target\ISO-Stock.jar" "target\paquet\" >nul

echo Fabrication de ISO Stock.exe, une a deux minutes...
jpackage --type app-image ^
  --name "ISO Stock" ^
  --input "target\paquet" ^
  --main-jar ISO-Stock.jar ^
  --main-class fr.taevie.parc.Main ^
  --icon "distribution\icone.ico" ^
  --app-version 1.0.0 ^
  --vendor TaeVie ^
  --description "Suivi du materiel embarque" ^
  --add-modules java.base,java.desktop,java.sql,jdk.localedata ^
  --jlink-options "--strip-debug --no-man-pages --no-header-files --include-locales=fr,en --compress=zip-6" ^
  --java-options "--enable-native-access=ALL-UNNAMED" ^
  --dest "target\livraison"

if errorlevel 1 (
    echo.
    echo La fabrication a echoue. Lisez le message ci-dessus.
    pause
    exit /b 1
)

copy /y "distribution\LISEZMOI-exe.txt" "target\livraison\ISO Stock\LISEZMOI.txt" >nul

echo.
echo Termine. Le dossier a livrer est :
echo   %CD%\target\livraison\ISO Stock
echo.
explorer "target\livraison\ISO Stock"
pause
