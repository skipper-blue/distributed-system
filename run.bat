@echo off
REM Distributed Drinks System - Complete Startup Script
REM This script compiles and runs the entire system

cls
setlocal enabledelayedexpansion

echo.
echo ╔═════════════════════════════════════════════════════════════════════╗
echo ║  DISTRIBUTED DRINKS SYSTEM - AUTOMATED SETUP & STARTUP              ║
echo ╚═════════════════════════════════════════════════════════════════════╝
echo.

set PROJECT_DIR=%~dp0
cd /d "%PROJECT_DIR%"

echo [STEP 1] Checking Prerequisites...
echo.

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo  ✗ Java not found in PATH
    echo  Please install Java 11+ and add to PATH
    pause
    exit /b 1
)
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| find "version"') do set JAVA_VERSION=%%i
echo  ✓ Java found: %JAVA_VERSION%

REM Check if compiled
if exist "out\common\*.class" (
    echo  ✓ Project already compiled
    goto SKIP_COMPILE
) else (
    echo  ⓘ Project not yet compiled
)

:COMPILE
echo.
echo [STEP 2] Compiling Project...
echo.

if not exist "out" mkdir out

echo  Compiling common module...
javac -d out common\src\main\java\common\*.java 2>compile_errors.log
if errorlevel 1 (
    echo  ✗ Compilation failed. Check compile_errors.log
    type compile_errors.log
    pause
    exit /b 1
)
echo  ✓ Common module compiled

echo  Compiling server module...
javac -cp out -d out server\src\main\java\server\*.java 2>compile_errors.log
if errorlevel 1 (
    echo  ✗ Compilation failed. Check compile_errors.log
    type compile_errors.log
    pause
    exit /b 1
)
echo  ✓ Server module compiled

echo  Compiling client module...
javac -cp out -d out client\src\main\java\client\*.java 2>compile_errors.log
if errorlevel 1 (
    echo  ✗ Compilation failed. Check compile_errors.log
    type compile_errors.log
    pause
    exit /b 1
)
echo  ✓ Client module compiled

echo  ✓ All modules compiled successfully!

:SKIP_COMPILE
echo.
echo [STEP 3] Checking MySQL JDBC Driver...
echo.

if exist "lib\mysql-connector-j-8.3.0.jar" (
    echo  ✓ MySQL JDBC driver found (mysql-connector-j-8.3.0.jar)
    set MYSQL_DRIVER=lib\mysql-connector-j-8.3.0.jar
) else if exist "lib\mysql-connector-java-8.0.33.jar" (
    echo  ✓ MySQL JDBC driver found (mysql-connector-java-8.0.33.jar)
    set MYSQL_DRIVER=lib\mysql-connector-java-8.0.33.jar
) else (
    echo  ✗ MySQL JDBC driver not found in lib\ folder
    echo.
    echo  Run: powershell -ExecutionPolicy Bypass
    echo  Then paste: (New-Object Net.WebClient).DownloadFile('https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar', 'lib\mysql-connector-j-8.3.0.jar')
    echo.
    pause
    exit /b 1
)

echo.
echo [STEP 4] Checking MySQL Connection...
echo.

:MENU
echo.
echo ╔═════════════════════════════════════════════════════════════════════╗
echo ║                         STARTUP MENU                                ║
echo ╠═════════════════════════════════════════════════════════════════════╣
echo ║  [1] Start Server Only                                              ║
echo ║  [2] Start Customer Client (requires server running)                ║
echo ║  [3] Start Admin Dashboard (requires server running)                ║
echo ║  [4] Start All in Separate Windows                                  ║
echo ║  [5] View Compilation Log                                           ║
echo ║  [6] Exit                                                           ║
echo ╚═════════════════════════════════════════════════════════════════════╝
echo.

set /p CHOICE="Enter your choice (1-6): "

if "%CHOICE%"=="1" goto START_SERVER
if "%CHOICE%"=="2" goto START_CUSTOMER
if "%CHOICE%"=="3" goto START_ADMIN
if "%CHOICE%"=="4" goto START_ALL
if "%CHOICE%"=="5" goto VIEW_LOG
if "%CHOICE%"=="6" goto END
echo Invalid choice. Please try again.
goto MENU

:START_SERVER
echo.
echo [SERVER] Starting RMI Server...
echo.
cd /d "%PROJECT_DIR%"
java -cp "out;%MYSQL_DRIVER%" server.ServerMain
pause
goto MENU

:START_CUSTOMER
echo.
echo [CUSTOMER] Starting Customer Client...
echo.
cd /d "%PROJECT_DIR%"
java -cp "out;%MYSQL_DRIVER%" client.CustomerClient
pause
goto MENU

:START_ADMIN
echo.
echo [ADMIN] Starting Admin Dashboard...
echo.
cd /d "%PROJECT_DIR%"
java -cp "out;%MYSQL_DRIVER%" client.AdminClient
pause
goto MENU

:START_ALL
echo.
echo [STARTUP] Launching all components...
echo  • Server will start in Window 1
echo  • Customer Client will start in Window 2
echo  • Admin Dashboard will start in Window 3
echo.
pause

start cmd /k "cd /d "%PROJECT_DIR%" && echo [SERVER] Starting RMI Server... && echo. && java -cp "out;%MYSQL_DRIVER%" server.ServerMain"
timeout /t 2

start cmd /k "cd /d "%PROJECT_DIR%" && echo [CUSTOMER] Starting Customer Client... && echo. && java -cp "out;%MYSQL_DRIVER%" client.CustomerClient"
timeout /t 1

start cmd /k "cd /d "%PROJECT_DIR%" && echo [ADMIN] Starting Admin Dashboard... && echo. && java -cp "out;%MYSQL_DRIVER%" client.AdminClient"

echo.
echo ✓ All components launched in separate windows
echo.
echo Next steps:
echo  1. Admin Dashboard and Customer Client will show connection status
echo  2. If they can't connect, ensure Server window shows "✓ SERVER RUNNING SUCCESSFULLY"
echo  3. Check MySQL is running if Server fails to start
echo.
pause
goto MENU

:VIEW_LOG
echo.
if exist "compile_errors.log" (
    echo ╔═════════════════════════════════════════════════════════════════════╗
    echo ║                     COMPILATION LOG                                 ║
    echo ╚═════════════════════════════════════════════════════════════════════╝
    echo.
    type compile_errors.log
) else (
    echo No compilation errors found
)
echo.
pause
goto MENU

:END
echo.
echo Thank you for using Distributed Drinks System!
echo.
exit /b 0
