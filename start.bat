@echo off
setlocal DisableDelayedExpansion
cd /d "%~dp0"
if exist .env for /f "usebackq eol=# tokens=1,* delims==" %%A in (".env") do set "%%A=%%B"
java -jar release\super-ace-online-13.0.0.jar
pause
