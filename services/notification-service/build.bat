@echo off
REM Notification Service Build & Run Script

echo.
echo ============================================
echo Notification Service - Build & Run
echo ============================================
echo.

cd /d "D:\clinic-appointment-system\services\notification-service"

echo [1/3] Running Maven clean compile...
call mvn clean compile -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Compilation failed!
    echo.
    pause
    exit /b 1
)

echo.
echo [2/3] Running Maven package...
call mvn package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Package failed!
    echo.
    pause
    exit /b 1
)

echo.
echo [3/3] Compilation and packaging successful!
echo.
echo To run the service:
echo   mvn spring-boot:run
echo.
pause
