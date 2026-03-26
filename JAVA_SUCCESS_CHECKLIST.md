# Java Success Checklist (Distributed Drinks System)

This file lists everything needed to run Java successfully for this project.

## 1) Required Software

- JDK `11+` installed (project works with newer LTS too).
- MySQL running on `localhost:3306`.
- MySQL JDBC driver file present:
  - `lib/mysql-connector-j-8.3.0.jar`

## 2) Environment Checks

Run these in PowerShell from project root:

```powershell
java -version
javac -version
```

Expected:

- Both commands should return a version (not "command not found").

## 3) Database Requirements

- Create/import schema from:
  - `database/setup.sql`
- Confirm database name exists:
  - `drinks_system`
- Confirm MySQL credentials in server code match your local setup.

## 4) Compile (Manual, No Maven)

From project root:

```powershell
if (!(Test-Path out)) { New-Item -ItemType Directory out | Out-Null }

# 1. Shared models/interfaces
javac -cp "lib\mysql-connector-j-8.3.0.jar" -d out common\src\main\java\common\*.java

# 2. Server
javac -cp "out;lib\mysql-connector-j-8.3.0.jar" -d out server\src\main\java\server\*.java

# 3. Client
javac -cp "out;lib\mysql-connector-j-8.3.0.jar" -d out client\src\main\java\client\*.java
```

Important:

- On Windows, classpath separator is `;`.
- On Linux/macOS, classpath separator is `:`.

## 5) Run Order (Use Separate Terminals)

Run in this order:

```powershell
# Terminal 1: Server
java -cp "out;lib\mysql-connector-j-8.3.0.jar" server.ServerMain
```

```powershell
# Terminal 2: Admin Client
java -cp "out;lib\mysql-connector-j-8.3.0.jar" client.AdminClient
```

```powershell
# Terminal 3: Customer Client
java -cp "out;lib\mysql-connector-j-8.3.0.jar" client.CustomerClient
```

## 6) Fast Start Option

You can use the provided script:

- `run_without_mvn.bat`

This compiles and starts server + clients automatically.

## 7) Common Errors and Fixes

### `java.io.InvalidClassException` (serialVersionUID mismatch)

Cause:

- Server and client are running with different compiled versions of shared classes.

Fix:

1. Stop all Java windows (server + clients).
2. Recompile `common`, then `server`, then `client` (Section 4).
3. Start server first, then clients again.

### `Cannot connect to server` / `Connection refused`

Fix:

1. Ensure server is running.
2. Ensure port `1099` is not blocked.
3. Confirm RMI registry/service started correctly.

### `javac` or `java` not recognized

Fix:

1. Install JDK.
2. Add JDK `bin` folder to `PATH`.
3. Reopen terminal and retry.

### `ClassNotFoundException` at startup

Fix:

1. Ensure `out` contains freshly compiled classes.
2. Ensure classpath includes both `out` and MySQL jar exactly as shown above.

## 8) Final Pre-Run Checklist

- `java -version` works.
- `javac -version` works.
- MySQL is running.
- `database/setup.sql` has been applied.
- `lib/mysql-connector-j-8.3.0.jar` exists.
- Compile completed without errors.
- Start order is: `Server -> Admin -> Customer`.
