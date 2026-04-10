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

        post("/restock", (req, res) -> {
            res.type("application/json");
            try {
                Map<String, Object> body = gson.fromJson(req.body(), Map.class);
                String branch = (String) body.get("branch");
                int drinkId = ((Double) body.get("drinkId")).intValue();
                int quantity = ((Double) body.get("quantity")).intValue();
                boolean success = DrinkService.restockDrink(branch, drinkId, quantity);
                return gson.toJson(Map.of("success", success));
            } catch (Exception e) {
                res.status(400);
                return gson.toJson(Map.of("error", e.getMessage()));
            }
        });

        get("/restock/history", (req, res) -> {
            res.type("application/json");
            return gson.toJson(DrinkService.getRestockHistory());
        });

        post("/restock/main", (req, res) -> {
            res.type("application/json");
            try {
                Map<String, Object> body = gson.fromJson(req.body(), Map.class);
                int drinkId = ((Double) body.get("drinkId")).intValue();
                int quantity = ((Double) body.get("quantity")).intValue();
                boolean success = DrinkService.restockMainBranch(drinkId, quantity);
                return gson.toJson(Map.of("success", success));
            } catch (Exception e) {
                res.status(400);
                return gson.toJson(Map.of("error", e.getMessage()));
            }
        });
    }
}
