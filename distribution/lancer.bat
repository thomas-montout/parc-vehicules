@echo off
rem Lance ISO Stock sans fenetre de console.
rem Pas d'accents dans ce fichier : l'invite de commandes Windows les affiche mal.

cd /d "%~dp0"

where javaw >nul 2>nul
if errorlevel 1 (
    echo Java n'est pas installe sur ce poste.
    echo Telechargez-le gratuitement sur https://adoptium.net puis relancez.
    echo.
    pause
    exit /b 1
)

start "" javaw -jar ISO-Stock.jar
