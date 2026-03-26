@echo off
REM Commands to run the Distributed Drinks System without Maven
REM This compiles and runs the Java code directly using javac and java

setlocal enabledelayedexpansion

set PROJECT_DIR=%~dp0
cd /d "%PROJECT_DIR%"

echo [INFO] Running without Maven - Manual compilation and execution
echo.

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ✗ Java not found - Please install Java 11+
    pause
    exit /b 1
)
echo ✓ Java installed

REM Ensure MySQL driver exists
if not exist "lib\mysql-connector-j-8.3.0.jar" (
    echo ✗ MySQL Driver not found in lib\ - Please download it manually or use Maven
    pause
    exit /b 1
)
echo ✓ MySQL Driver found

REM Create output directory
if not exist "out" mkdir out

echo.
echo [STEP 1] Compiling common module...
javac -cp "lib\mysql-connector-j-8.3.0.jar" -d out common\src\main\java\common\*.java
if errorlevel 1 (
    echo ✗ Compilation of common failed
    pause
    exit /b 1
)
echo ✓ Common compiled

echo.
echo [STEP 2] Compiling server module...
javac -cp "out;lib\mysql-connector-j-8.3.0.jar" -d out server\src\main\java\server\*.java
if errorlevel 1 (
    echo ✗ Compilation of server failed
    pause
    exit /b 1
)
echo ✓ Server compiled

echo.
echo [STEP 3] Compiling client module...
javac -cp "out;lib\mysql-connector-j-8.3.0.jar" -d out client\src\main\java\client\*.java
if errorlevel 1 (
    echo ✗ Compilation of client failed
    pause
    exit /b 1
)
echo ✓ Client compiled

echo.
echo [STEP 4] Starting components...
echo.

REM Start Server
echo [SERVER] Starting RMI Server...
start "SERVER" cmd /k "cd /d "%PROJECT_DIR%" && java -cp "out;lib\mysql-connector-j-8.3.0.jar" server.ServerMain"

REM Wait
timeout /t 3 /nobreak >nul

REM Start Admin Client
echo [ADMIN] Starting Admin Client...
start "ADMIN" cmd /k "cd /d "%PROJECT_DIR%" && java -cp "out;lib\mysql-connector-j-8.3.0.jar" client.AdminClient"

REM Wait
timeout /t 1 /nobreak >nul

REM Start Customer Client
echo [CUSTOMER] Starting Customer Client...
start "CUSTOMER" cmd /k "cd /d "%PROJECT_DIR%" && java -cp "out;lib\mysql-connector-j-8.3.0.jar" client.CustomerClient"

echo.
echo ✓ All components started. Close the windows to stop.
pause