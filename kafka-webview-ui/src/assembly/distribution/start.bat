@echo off

cd /D %~dp0

:: launch webapp
FOR /F "tokens=* USEBACKQ" %%F IN (`dir /b kafka-webview-ui-*.jar`) DO SET "jar=%%F"
java -jar %jar%
