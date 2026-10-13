@echo off
rem Lance ISO Stock en affichant les messages d'erreur.
rem A utiliser si lancer.bat n'ouvre rien : faites une capture de cette fenetre.

cd /d "%~dp0"
echo === Version de Java ===
java -version
echo.
echo === Demarrage d'ISO Stock ===
java -jar ISO-Stock.jar
echo.
pause
