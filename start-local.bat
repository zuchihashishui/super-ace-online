@echo off
setlocal DisableDelayedExpansion
cd /d "%~dp0"
if not exist .env.local (
  powershell -NoProfile -Command "$secret=[guid]::NewGuid().ToString('N')+[guid]::NewGuid().ToString('N'); @('DB_URL=jdbc:mysql://localhost:3306/ace?createDatabaseIfNotExist=true&connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true','DB_USER=root','DB_PASSWORD=123456','CREATOR_USER=zuchiha','CREATOR_PASSWORD=112357',('JWT_SECRET='+$secret),'COOKIE_SECURE=false') | Set-Content -Encoding ASCII .env.local"
  if errorlevel 1 exit /b 1
)
for /f "usebackq eol=# tokens=1,* delims==" %%A in (".env.local") do set "%%A=%%B"
java -jar release\super-ace-online-13.0.0.jar
pause
