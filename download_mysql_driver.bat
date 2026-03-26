@echo off
REM ════════════════════════════════════════════════════════════
REM  DOWNLOAD MYSQL JDBC DRIVER
REM ════════════════════════════════════════════════════════════

setlocal enabledelayedexpansion

echo.
echo ═══════════════════════════════════════════════════════════
echo     MYSQL JDBC DRIVER DOWNLOADER
echo ═══════════════════════════════════════════════════════════
echo.

REM Create lib directory if it doesn't exist
if not exist lib (
    echo [INFO] Creating lib directory...
    mkdir lib
)

REM Check if driver already exists
if exist lib\mysql-connector-java-8.0.33.jar (
    echo [✓] MySQL JDBC driver already exists in lib folder
    echo.
    goto success
)

echo [INFO] Downloading MySQL JDBC driver (mysql-connector-java-8.0.33.jar)...
echo [INFO] This may take a minute...
echo.

REM Download using PowerShell (more reliable than bitsadmin on modern Windows)
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ProgressPreference = 'SilentlyContinue'; ^
    try { ^
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; ^
        $url = 'https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar'; ^
        $outFile = 'lib\mysql-connector-java-8.0.33.jar'; ^
        Write-Host '[INFO] Downloading from Maven Central Repository...'; ^
        (New-Object Net.WebClient).DownloadFile($url, $outFile); ^
        Write-Host '[✓] Download complete!'; ^
        exit 0; ^
    } catch { ^
        Write-Host '[✗] Download failed: ' + $_.Exception.Message; ^
        exit 1; ^
    }"

if errorlevel 1 (
    echo.
    echo [✗] Download failed. Trying alternative source...
    echo.
    
    REM Alternative: Try downloading from MySQL's mirror
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
        "$ProgressPreference = 'SilentlyContinue'; ^
        try { ^
            [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; ^
            $url = 'https://downloads.mysql.com/archives/get/p/3/file/mysql-connector-java-8.0.33.jar'; ^
            $outFile = 'lib\mysql-connector-java-8.0.33.jar'; ^
            Write-Host '[INFO] Downloading from MySQL Direct...'; ^
            (New-Object Net.WebClient).DownloadFile($url, $outFile); ^
            Write-Host '[✓] Download complete!'; ^
            exit 0; ^
        } catch { ^
            Write-Host '[✗] Download failed: ' + $_.Exception.Message; ^
            exit 1; ^
        }"
    
    if errorlevel 1 (
        echo.
        echo [✗] Both download attempts failed.
        echo.
        echo [INFO] Manual Download Instructions:
        echo ────────────────────────────────────
        echo 1. Visit: https://dev.mysql.com/downloads/connector/j/
        echo 2. Download: mysql-connector-java-8.0.33.jar
        echo 3. Place in: lib\ folder of this project
        echo.
        echo Or download from Maven Central:
        echo https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.33/mysql-connector-java-8.0.33.jar
        echo.
        pause
        exit /b 1
    )
)

:success
echo.
echo [✓] MySQL JDBC Driver Setup Complete!
echo [INFO] Driver location: lib\mysql-connector-java-8.0.33.jar
echo.
echo You can now run the application with:
echo   run.bat
echo.
pause
