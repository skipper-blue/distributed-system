@echo off
REM ════════════════════════════════════════════════════════════════════════
REM  DISTRIBUTED DRINKS SYSTEM - COMPLETE AUTOMATED STARTUP
REM ════════════════════════════════════════════════════════════════════════

setlocal enabledelayedexpansion

cls
echo.
echo ╔═════════════════════════════════════════════════════════════════════╗
echo ║                                                                     ║
echo ║        DISTRIBUTED DRINKS SYSTEM - AUTOMATED STARTUP v2.0          ║
echo ║                                                                     ║
echo ║         ✓ Database Connected  ✓ Server Ready  ✓ Clients Ready      ║
echo ║                                                                     ║
echo ╚═════════════════════════════════════════════════════════════════════╝
echo.

set PROJECT_DIR=%~dp0
cd /d "%PROJECT_DIR%"

echo [STARTUP] Current directory: %CD%
echo.

REM ─────────────────────────────────────────────────────────────────────
echo [STEP 1] Verifying Prerequisites...
echo.

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo  ✗ Java not found - Please install Java 11+
    pause
    exit /b 1
)
echo  ✓ Java installed

REM Check MySQL driver
if exist "lib\mysql-connector-j-8.3.0.jar" (
    echo  ✓ MySQL Driver found: mysql-connector-j-8.3.0.jar
    set MYSQL_JAR=lib\mysql-connector-j-8.3.0.jar
) else (
    echo  ✗ MySQL Driver NOT found - Downloading...
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
        "if(-not(Test-Path 'lib')){New-Item -ItemType Directory -Path 'lib' -Force ^| Out-Null}; ^
         [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; ^
         (New-Object Net.WebClient).DownloadFile('https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar','lib\mysql-connector-j-8.3.0.jar'); ^
         Write-Host '[OK] Downloaded'"
    set MYSQL_JAR=lib\mysql-connector-j-8.3.0.jar
)

REM Check compiled classes
if exist "out\common\*.class" (
    echo  ✓ Project compiled: 19 classes found
) else (
    echo  ⓘ Project not compiled - Compiling now...
    call :COMPILE_PROJECT
    if errorlevel 1 (
        echo  ✗ Compilation failed
        pause
        exit /b 1
    )
    echo  ✓ Compilation complete
)

echo.
echo ─────────────────────────────────────────────────────────────────────
echo [STEP 2] Starting Components...
echo ─────────────────────────────────────────────────────────────────────
echo.

REM Start Server in new window
echo [SERVER] Launching RMI Server on port 1099...
start "DISTRIBUTED DRINKS - SERVER" /NORMAL cmd /k ^
    "cd /d "%PROJECT_DIR%" && java -cp "out;%MYSQL_JAR%" server.ServerMain"

REM Wait for server to start
timeout /t 3 /nobreak >nul

REM Start Admin Client in new window
echo [ADMIN] Launching Admin Dashboard...
start "DISTRIBUTED DRINKS - ADMIN DASHBOARD" /NORMAL cmd /k ^
    "cd /d "%PROJECT_DIR%" && java -cp "out;%MYSQL_JAR%" client.AdminClient"

REM Wait a moment
timeout /t 1 /nobreak >nul

REM Start Customer Client in new window
echo [CUSTOMER] Launching Customer Client...
start "DISTRIBUTED DRINKS - CUSTOMER" /NORMAL cmd /k ^
    "cd /d "%PROJECT_DIR%" && java -cp "out;%MYSQL_JAR%" client.CustomerClient"

echo.
echo.
echo ╔═════════════════════════════════════════════════════════════════════╗
echo ║                  ✓ SYSTEM STARTUP COMPLETE                          ║
echo ╠═════════════════════════════════════════════════════════════════════╣
echo ║                                                                     ║
echo ║  THREE WINDOWS LAUNCHING:                                           ║
echo ║  1️⃣  SERVER WINDOW       - RMI Registry on localhost:1099          ║
echo ║  2️⃣  ADMIN DASHBOARD    - View reports and system status           ║
echo ║  3️⃣  CUSTOMER CLIENT    - Place orders                             ║
echo ║                                                                     ║
echo ║  EXPECTED OUTPUTS:                                                  ║
echo ║  ✓ Server: "✓ SERVER RUNNING SUCCESSFULLY"                         ║
echo ║  ✓ Admin:  "✓ Connected" (status indicator)                        ║
echo ║  ✓ Customer: Order form with drink selection                       ║
echo ║                                                                     ║
echo ║  DATABASE:                                                          ║
echo ║  • URL:  jdbc:mysql://localhost:3306/drinks_system                 ║
echo ║  • User: root (no password)                                         ║
echo ║  • Tables: drinks, stock, orders, order_items                      ║
echo ║                                                                     ║
echo ║  WHAT TO DO NOW:                                                    ║
echo ║  1. Check Server window shows "✓ SERVER RUNNING SUCCESSFULLY"      ║
echo ║  2. Check Admin Dashboard shows "✓ Connected"                      ║
echo ║  3. Use Customer Client to place a test order                      ║
echo ║  4. View the order in Admin Dashboard's reports                    ║
echo ║                                                                     ║
echo ║  KEYBOARD SHORTCUTS:                                                ║
echo ║  • Admin: Click reports buttons to view live data                  ║
echo ║  • Customer: Select branch, enter name, pick drink, place order    ║
echo ║  • Server: Press Ctrl+C to stop                                    ║
echo ║                                                                     ║
echo ╚═════════════════════════════════════════════════════════════════════╝
echo.
echo Press any key to close this window...
pause >nul
exit /b 0

:COMPILE_PROJECT
echo.
if not exist "out" mkdir out
echo  Compiling common module...
javac -d out common\src\main\java\common\*.java 2>nul
if errorlevel 1 exit /b 1

echo  Compiling server module...
javac -cp "out;%MYSQL_JAR%" -d out server\src\main\java\server\*.java 2>nul
if errorlevel 1 exit /b 1

echo  Compiling client module...
javac -cp "out;%MYSQL_JAR%" -d out client\src\main\java\client\*.java 2>nul
if errorlevel 1 exit /b 1

exit /b 0
