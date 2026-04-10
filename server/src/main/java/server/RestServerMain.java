package server;

import static spark.Spark.port;

public class RestServerMain {
    public static void main(String[] args) {
        int appPort = getIntEnv("PORT", 4567);
        port(appPort);

        DrinkController.registerRoutes();

        System.out.println("[REST] Render-ready server started on port " + appPort);
    }

    private static int getIntEnv(String key, int defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
