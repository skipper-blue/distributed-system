# Running the Distributed Drinks System Without Maven

This guide provides direct commands to compile and run the system manually using Java tools only.

## Prerequisites
- Java 11+ installed (`java -version`)
- MySQL driver JAR in `lib/mysql-connector-j-8.3.0.jar` (download if missing)
- Chart libraries in `lib/jfreechart-1.5.4.jar` and `lib/jcommon-1.0.24.jar`

## Step 1: Compile the Code
Run these commands in order from the project root directory:

```bash
# Create output directory
mkdir out

# Compile common module
javac -cp "lib/mysql-connector-j-8.3.0.jar;lib/jfreechart-1.5.4.jar;lib/jcommon-1.0.24.jar" -d out common/src/main/java/common/*.java

# Compile server module
javac -cp "out;lib/mysql-connector-j-8.3.0.jar;lib/jfreechart-1.5.4.jar;lib/jcommon-1.0.24.jar" -d out server/src/main/java/server/*.java

# Compile client module
javac -cp "out;lib/mysql-connector-j-8.3.0.jar;lib/jfreechart-1.5.4.jar;lib/jcommon-1.0.24.jar" -d out client/src/main/java/client/*.java
```

## Step 2: Run the Components
Open separate terminal windows for each command:

### Server (Window 1)
```bash
java -cp "out;lib/mysql-connector-j-8.3.0.jar;lib/jfreechart-1.5.4.jar;lib/jcommon-1.0.24.jar" server.ServerMain
```

### Admin Client (Window 2)
```bash
java -cp "out;lib/mysql-connector-j-8.3.0.jar;lib/jfreechart-1.5.4.jar;lib/jcommon-1.0.24.jar" client.AdminClient
```

### Customer Client (Window 3)
```bash
java -cp "out;lib/mysql-connector-j-8.3.0.jar;lib/jfreechart-1.5.4.jar;lib/jcommon-1.0.24.jar" client.CustomerClient
```

## Admin Client Features
- **Dashboard**: Real-time overview of customers, orders, revenue, and low stock alerts.
- **Restock Management**: Live inventory workspace with a restock form, branch chart, stock table, and audit history.
- **Reports**: Detailed analytics by location and overall system performance.

## Alternative: Using the Launcher
For convenience, you can use the Launcher class to start all components automatically:

```bash
# Compile as above, then run:
java -cp "out;lib/mysql-connector-j-8.3.0.jar;lib/jfreechart-1.5.4.jar;lib/jcommon-1.0.24.jar" client.Launcher
```

This will start the server, wait for it to initialize, then launch the Admin and Customer clients. All output will be in the same terminal.

## Notes
- Run the server first, then the clients.
- Use Ctrl+C to stop each process.
- Ensure MySQL is running and database is set up (see `database/setup.sql`).
