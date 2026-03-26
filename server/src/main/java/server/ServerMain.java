package server;

import common.RemoteService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            System.out.println("\n═══════════════════════════════════════════════════════");
            System.out.println("  DISTRIBUTED DRINKS SYSTEM - SERVER STARTUP");
            System.out.println("═══════════════════════════════════════════════════════\n");
            
            System.out.println("[STARTUP] Testing database connection...");
            // Test database connection before starting server
            DatabaseConnection.getConnection().close();
            System.out.println("[STARTUP] ✓ Database connection verified\n");
            
            System.out.println("[STARTUP] Creating RMI service implementation...");
            RemoteService service = new RemoteServiceImpl();
            System.out.println("[STARTUP] ✓ Service created\n");
            
            System.out.println("[STARTUP] Initializing RMI Registry on port 1099...");
            Registry registry = null;
            try {
                registry = LocateRegistry.createRegistry(1099);
                System.out.println("[STARTUP] ✓ New RMI Registry created");
            } catch (Exception e) {
                System.out.println("[STARTUP] ⚠ Registry already exists, using existing");
                registry = LocateRegistry.getRegistry(1099);
            }
            
            System.out.println("[STARTUP] Binding service as 'DrinkService'...");
            registry.rebind("DrinkService", service);
            System.out.println("[STARTUP] ✓ Service bound successfully\n");
            
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("  ✓ SERVER RUNNING SUCCESSFULLY");
            System.out.println("  RMI Registry: localhost:1099");
            System.out.println("  Service Name: DrinkService");
            System.out.println("  Ready to accept client connections...");
            System.out.println("═══════════════════════════════════════════════════════\n");
            
        } catch (Exception e) {
            System.err.println("\n✗ SERVER STARTUP FAILED\n");
            System.err.println("Error: " + e.getMessage());
            System.err.println("\nTroubleshooting:");
            System.err.println("1. Check if XAMPP MySQL is running");
            System.err.println("2. Verify database 'drinks_system' exists");
            System.err.println("3. Check port 1099 is not in use");
            System.err.println("4. Ensure mysql-connector-java JAR is in classpath\n");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
