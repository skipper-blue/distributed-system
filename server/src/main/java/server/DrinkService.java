package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DrinkService {

    public static List<Map<String, Object>> getAllStock() {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql = "SELECT drink_id, branch, quantity FROM stock ORDER BY branch, drink_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("drink_id", rs.getInt("drink_id"));
                row.put("branch", rs.getString("branch"));
                row.put("quantity", rs.getInt("quantity"));
                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}
