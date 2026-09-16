@echo off
rem Use the same external application.properties as start.bat.
call "%~dp0start.bat"
exit /b %ERRORLEVEL%
