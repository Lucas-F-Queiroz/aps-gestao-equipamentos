@echo off
setlocal
cd /d "%~dp0"
where java >nul 2>nul || (echo Instale Java 17 e coloque no PATH. & exit /b 1)
where mvn >nul 2>nul || (echo Instale Apache Maven e coloque no PATH. & exit /b 1)
where docker >nul 2>nul || (echo Instale e inicie Docker Desktop. & exit /b 1)
docker compose up -d --wait mysql || (echo Falha ao iniciar MySQL no Docker. & exit /b 1)
mvn -q -DskipTests compile exec:java
if errorlevel 1 (echo Nao foi possivel iniciar a aplicacao. & exit /b 1)
