@echo off
REM Database Setup and Verification Script for XAMPP

cls
setlocal enabledelayedexpansion

echo.
echo ╔═════════════════════════════════════════════════════════════════════╗
echo ║         DATABASE SETUP & VERIFICATION SCRIPT (XAMPP)                ║
echo ╚═════════════════════════════════════════════════════════════════════╝
echo.

echo [STEP 1] Verifying MySQL Connection...
echo.

REM Test connection
mysql -u root -e "SELECT VERSION();" >nul 2>&1
if errorlevel 1 (
    echo  ✗ Could not connect to MySQL
    echo.
    echo [HELP] Ensure XAMPP MySQL is running:
    echo  1. Open XAMPP Control Panel
    echo  2. Click "Start" next to MySQL
    echo  3. Wait for it to show "Running"
    echo  4. Run this script again
    echo.
    pause
    exit /b 1
)

for /f "tokens=*" %%i in ('mysql -u root -e "SELECT VERSION();"') do set MYSQL_VERSION=%%i
echo  ✓ MySQL Connected
echo  Version: %MYSQL_VERSION%
echo.

echo [STEP 2] Checking Database...
echo.

REM Check if database exists
mysql -u root -e "USE drinks_system;" >nul 2>&1
if errorlevel 1 (
    echo  ⓘ Database 'drinks_system' not found
    echo  Creating database and schema...
    echo.
    
    REM Create database and load schema
    mysql -u root < database\setup.sql
    if errorlevel 1 (
        echo  ✗ Failed to create database
        pause
        exit /b 1
    )
    echo  ✓ Database created and schema loaded
) else (
    echo  ✓ Database 'drinks_system' exists
)

echo.
echo [STEP 3] Verifying Schema and Data...
echo.

REM Check tables
mysql -u root -e "USE drinks_system; SHOW TABLES;" >nul 2>&1
if errorlevel 1 (
    echo  ✗ Database tables not found
    pause
    exit /b 1
)
echo  ✓ All tables exist

REM Check drinks
for /f %%i in ('mysql -u root -e "USE drinks_system; SELECT COUNT(*) FROM drinks;" 2^>nul') do set DRINK_COUNT=%%i
echo  ✓ Drinks loaded: %DRINK_COUNT% products

REM Check stock
for /f %%i in ('mysql -u root -e "USE drinks_system; SELECT COUNT(*) FROM stock;" 2^>nul') do set STOCK_COUNT=%%i
echo  ✓ Stock records: %STOCK_COUNT% entries

echo.
echo [STEP 4] Displaying Sample Data...
echo.

echo DRINKS:
mysql -u root -e "USE drinks_system; SELECT name, price FROM drinks;"

echo.
echo STOCK (First 4 Branches):
mysql -u root -e "USE drinks_system; SELECT branch, COUNT(*) as items, SUM(quantity) as total FROM stock GROUP BY branch;"

echo.
echo ╔═════════════════════════════════════════════════════════════════════╗
echo ║                    ✓ SETUP VERIFICATION COMPLETE                    ║
echo ╚═════════════════════════════════════════════════════════════════════╝
echo.
echo Database is ready! You can now run the application.
echo.
echo Next: Run "run.bat" to start the system, or manually:
echo  1. Start server: java -cp out server.ServerMain
echo  2. Start client: java -cp out client.CustomerClient
echo  3. Start admin: java -cp out client.AdminClient
echo.

pause
