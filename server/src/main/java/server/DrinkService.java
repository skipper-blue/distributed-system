package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class DrinkService {

    private static final ReentrantLock lock = new ReentrantLock();
    private static final List<String> BRANCH_DISPLAY_ORDER = List.of("NAIROBI", "NAKURU", "MOMBASA", "KISUMU");

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

    public static boolean restockDrink(String branch, int drinkId, int quantity) {
        lock.lock();
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            String result = executeRestockTransfer(conn, branch, drinkId, quantity);
            if (result.startsWith("Error:")) {
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            lock.unlock();
        }
    }

    public static List<Map<String, Object>> getRestockHistory() {
        List<Map<String, Object>> history = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT r.restock_date, d.name, r.branch, r.quantity_added " +
                 "FROM restocks r JOIN drinks d ON r.drink_id = d.id " +
                 "ORDER BY r.restock_date DESC, r.id DESC LIMIT 100")) {

            while (rs.next()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("date", rs.getTimestamp("restock_date"));
                entry.put("branch", formatLocation(rs.getString("branch")));
                entry.put("drink", rs.getString("name"));
                entry.put("quantity", rs.getInt("quantity_added"));
                history.add(entry);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    public static boolean restockMainBranch(int drinkId, int quantity) {
        if (quantity <= 0) {
            return false;
        }
        lock.lock();
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            boolean success = updateBranchStockAndLog(conn, "NAIROBI", drinkId, quantity);
            if (success) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            lock.unlock();
        }
    }

    private static String executeRestockTransfer(Connection conn, String branch, int drinkId, int quantity) throws SQLException {
        String normalizedBranch = normalizeBranch(branch);
        if (quantity <= 0) {
            return "Error: Quantity must be positive.";
        }
        if ("NAIROBI".equals(normalizedBranch)) {
            return "Error: Cannot restock Headquarters.";
        }

        String drinkName = findDrinkNameById(conn, drinkId);
        if (drinkName == null) {
            return "Error: Drink with ID " + drinkId + " not found.";
        }

        int hqStock = getStockQuantity(conn, "NAIROBI", drinkId);
        if (hqStock < quantity) {
            return "Error: Insufficient stock at Headquarters. Available: " + hqStock;
        }

        int hqUpdateCount = updateStock(conn, "NAIROBI", drinkId, -quantity);
        if (hqUpdateCount == 0) {
            return "Error: Failed to decrement Headquarters stock for " + drinkName;
        }

        boolean branchUpdated = updateBranchStockAndLog(conn, normalizedBranch, drinkId, quantity);
        if (!branchUpdated) {
            return "Error: Branch " + normalizedBranch + " does not have stock entry for " + drinkName;
        }

        return "Restocked " + quantity + " units of " + drinkName + " to " + formatLocation(normalizedBranch) + " successfully.";
    }

    private static String normalizeBranch(String branch) {
        if (branch == null) return null;
        return branch.toUpperCase().replace(" ", "");
    }

    private static String formatLocation(String locationCode) {
        if (locationCode == null) return "";
        switch (locationCode) {
            case "NAIROBI": return "Nairobi (HQ)";
            case "NAKURU": return "Nakuru";
            case "MOMBASA": return "Mombasa";
            case "KISUMU": return "Kisumu";
            default: return locationCode;
        }
    }

    private static String findDrinkNameById(Connection conn, int drinkId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT name FROM drinks WHERE id = ?")) {
            ps.setInt(1, drinkId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        }
        return null;
    }

    private static int getStockQuantity(Connection conn, String branch, int drinkId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT quantity FROM stock WHERE branch = ? AND drink_id = ?")) {
            ps.setString(1, branch);
            ps.setInt(2, drinkId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                }
            }
        }
        return 0;
    }

    private static int updateStock(Connection conn, String branch, int drinkId, int quantityChange) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE stock SET quantity = quantity + ? WHERE branch = ? AND drink_id = ?")) {
            ps.setInt(1, quantityChange);
            ps.setString(2, branch);
            ps.setInt(3, drinkId);
            return ps.executeUpdate();
        }
    }

    private static boolean updateBranchStockAndLog(Connection conn, String branch, int drinkId, int quantity) throws SQLException {
        int updateCount = updateStock(conn, branch, drinkId, quantity);
        if (updateCount > 0) {
            insertRestockRecord(conn, drinkId, branch, quantity);
            return true;
        }
        return false;
    }

    private static void insertRestockRecord(Connection conn, int drinkId, String branch, int quantity) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO restocks (drink_id, branch, quantity_added) VALUES (?, ?, ?)")) {
            ps.setInt(1, drinkId);
            ps.setString(2, branch);
            ps.setInt(3, quantity);
            ps.executeUpdate();
        }
    }
}
