@echo off
setlocal DisableDelayedExpansion
cd /d "%~dp0"
if not exist "server\src\main\resources\application.properties" (
  echo Missing configuration: server\src\main\resources\application.properties
  pause
  exit /b 1
)
if not exist "release\super-ace-online-13.0.0.jar" (
  echo Missing release\super-ace-online-13.0.0.jar
  pause
  exit /b 1
)
echo Using configuration: server\src\main\resources\application.properties
java -jar "release\super-ace-online-13.0.0.jar" "--spring.config.location=file:./server/src/main/resources/application.properties"
set "ACE_EXIT_CODE=%ERRORLEVEL%"
pause
exit /b %ACE_EXIT_CODE%
