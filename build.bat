@echo off
setlocal
echo ====================================
echo  Building NullSteve mod
echo ====================================
cd /d "%~dp0"
if not exist gradlew.bat (
echo gradlew.bat not found. Please run from project root.
exit /b 1
)
call gradlew.bat clean build --no-daemon
if errorlevel 1 (
echo Build FAILED.
exit /b %errorlevel%
)
echo.
echo Build SUCCESS. JAR is in build\libs\
echo.
endlocal
pause
