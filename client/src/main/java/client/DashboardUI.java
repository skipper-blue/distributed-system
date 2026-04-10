package client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class DashboardUI extends JFrame {
    private static final String API_URL = System.getenv().getOrDefault("API_URL", "https://your-app.onrender.com");
    private static final String STOCK_PATH = API_URL + "/stock";
    private final DefaultTableModel model;

    public DashboardUI() {
        setTitle("🍹 Drinks Stock Dashboard (Live)");
        setSize(700, 450);
        setLayout(new BorderLayout());

        model = new DefaultTableModel();
        model.addColumn("Drink ID");
        model.addColumn("Branch");
        model.addColumn("Quantity");

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        add(new JScrollPane(table), BorderLayout.CENTER);

        startAutoRefresh();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void startAutoRefresh() {
        Timer timer = new Timer("dashboard-refresh", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchStock();
            }
        }, 0, 3000);
    }

    private void fetchStock() {
        try {
            URL url = new URL(STOCK_PATH);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Accept", "application/json");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                JsonArray array = JsonParser.parseString(responseBuilder.toString()).getAsJsonArray();

                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    for (JsonElement element : array) {
                        JsonObject obj = element.getAsJsonObject();
                        model.addRow(new Object[]{
                            obj.get("drink_id").getAsInt(),
                            obj.get("branch").getAsString(),
                            obj.get("quantity").getAsInt()
                        });
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("[Dashboard] Failed to refresh stock: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(DashboardUI::new);
    }
}
