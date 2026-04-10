package client;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Launcher {
    private static final String CLIENT_CLASSPATH =
        "out;lib/mysql-connector-j-8.3.0.jar;lib/jfreechart-1.5.4.jar;lib/jcommon-1.0.24.jar";

    public static void main(String[] args) {
        System.out.println("Starting Distributed Drinks System...");

        // Start server
        ProcessBuilder serverPb = new ProcessBuilder(
            "java", "-cp", CLIENT_CLASSPATH, "server.ServerMain"
        );
        serverPb.inheritIO();
        try {
            Process serverProcess = serverPb.start();
            System.out.println("Server started. Waiting for initialization...");

            // Wait for server to start
            TimeUnit.SECONDS.sleep(3);

            // Start Admin Client
            ProcessBuilder adminPb = new ProcessBuilder(
                "java", "-cp", CLIENT_CLASSPATH, "client.AdminClient"
            );
            adminPb.inheritIO();
            Process adminProcess = adminPb.start();
            System.out.println("Admin Client started.");

            // Start Customer Client
            ProcessBuilder customerPb = new ProcessBuilder(
                "java", "-cp", CLIENT_CLASSPATH, "client.CustomerClient"
            );
            customerPb.inheritIO();
            Process customerProcess = customerPb.start();
            System.out.println("Customer Client started.");

            System.out.println("All components started. Press Ctrl+C to stop.");

            // Wait for processes
            serverProcess.waitFor();
            adminProcess.waitFor();
            customerProcess.waitFor();

        } catch (IOException | InterruptedException e) {
            System.err.println("Error starting system: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
