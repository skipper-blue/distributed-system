package server;

import static spark.Spark.*;

import com.google.gson.Gson;
import java.util.Map;

public class DrinkController {
    private static final Gson gson = new Gson();

    public static void registerRoutes() {
        get("/health", (req, res) -> {
            res.type("application/json");
            return gson.toJson(Map.of("status", "ok"));
        });

        get("/stock", (req, res) -> {
            res.type("application/json");
            return gson.toJson(DrinkService.getAllStock());
        });
    }
}
