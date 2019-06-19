@echo off

cd /D %~dp0

:: Define configuration
set SPRING_CONFIG_LOCATION=classpath:/config/base.yml,config.yml

:: launch webapp
FOR /F "tokens=* USEBACKQ" %%F IN (`dir /b kafka-webview-ui-*.jar`) DO SET "jar=%%F"
java -jar %jar%
