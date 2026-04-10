@echo off
REM Commands to run the Distributed Drinks System without Maven
REM This compiles and runs the Java code directly using javac and java

setlocal enabledelayedexpansion

set PROJECT_DIR=%~dp0
set CLIENT_LIBS=lib\mysql-connector-j-8.3.0.jar;lib\jfreechart-1.5.4.jar;lib\jcommon-1.0.24.jar
cd /d "%PROJECT_DIR%"

echo [INFO] Running without Maven - Manual compilation and execution
echo.

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java not found - Please install Java 11+
    pause
    exit /b 1
)
echo [OK] Java installed

REM Ensure required libraries exist
if not exist "lib\mysql-connector-j-8.3.0.jar" (
    echo [ERROR] MySQL Driver not found in lib\ - Please add mysql-connector-j-8.3.0.jar
    pause
    exit /b 1
)
if not exist "lib\jfreechart-1.5.4.jar" (
    echo [ERROR] JFreeChart not found in lib\ - Please add jfreechart-1.5.4.jar
    pause
    exit /b 1
)
if not exist "lib\jcommon-1.0.24.jar" (
    echo [ERROR] JCommon not found in lib\ - Please add jcommon-1.0.24.jar
    pause
    exit /b 1
)
echo [OK] Required libraries found

REM Create output directory
if not exist "out" mkdir out

echo.
echo [STEP 1] Compiling common module...
javac -cp "%CLIENT_LIBS%" -d out common\src\main\java\common\*.java
if errorlevel 1 (
    echo [ERROR] Compilation of common failed
    pause
    exit /b 1
)
echo [OK] Common compiled

echo.
echo [STEP 2] Compiling server module...
javac -cp "out;%CLIENT_LIBS%" -d out server\src\main\java\server\*.java
if errorlevel 1 (
    echo [ERROR] Compilation of server failed
    pause
    exit /b 1
)
echo [OK] Server compiled

echo.
echo [STEP 3] Compiling client module...
javac -cp "out;%CLIENT_LIBS%" -d out client\src\main\java\client\*.java
if errorlevel 1 (
    echo [ERROR] Compilation of client failed
    pause
    exit /b 1
)
echo [OK] Client compiled

echo.
echo [STEP 4] Starting components...
echo.

REM Start Server
echo [SERVER] Starting RMI Server...
start "SERVER" cmd /k "cd /d "%PROJECT_DIR%" && java -cp "out;%CLIENT_LIBS%" server.ServerMain"

REM Wait
timeout /t 3 /nobreak >nul

REM Start Admin Client
echo [ADMIN] Starting Admin Client...
start "ADMIN" cmd /k "cd /d "%PROJECT_DIR%" && java -cp "out;%CLIENT_LIBS%" client.AdminClient"

REM Wait
timeout /t 1 /nobreak >nul

REM Start Customer Client
echo [CUSTOMER] Starting Customer Client...
start "CUSTOMER" cmd /k "cd /d "%PROJECT_DIR%" && java -cp "out;%CLIENT_LIBS%" client.CustomerClient"

echo.
echo [OK] All components started. Close the windows to stop.
pause
