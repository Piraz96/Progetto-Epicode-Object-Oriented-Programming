@REM QuizMaster Maven wrapper (Windows variant).
@echo off
where mvn >nul 2>nul
if errorlevel 1 (
    echo Maven is not installed on this machine.
    echo Download from https://maven.apache.org/download.cgi
    exit /b 2
)
mvn %*
