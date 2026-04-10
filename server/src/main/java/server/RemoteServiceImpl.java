package server;

import common.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class RemoteServiceImpl extends UnicastRemoteObject implements RemoteService {

    private static final List<String> BRANCH_DISPLAY_ORDER = Arrays.asList(
        "NAIROBI",
        "NAKURU",
        "MOMBASA",
        "KISUMU"
    );

    private final ReentrantLock lock = new ReentrantLock();

    protected RemoteServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public String placeOrder(Order order) throws RemoteException {
        lock.lock();
        try {
            if ("NAIROBI".equalsIgnoreCase(order.branch)) {
                return "Error: Headquarters (Nairobi) orders are admin-only.";
            }

            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Validate stock and calculate total
            double totalCost = 0.0;
            Map<String, Integer> drinkQuantities = new HashMap<>();

            for (OrderItem item : order.items) {
                // Get drink price and check stock
                PreparedStatement ps = conn.prepareStatement(
                    "SELECT d.price, s.quantity FROM drinks d JOIN stock s ON d.id = s.drink_id " +
                    "WHERE d.name = ? AND s.branch = ?");
                ps.setString(1, item.drinkName);
                ps.setString(2, order.branch);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    conn.rollback();
                    return "Error: Drink " + item.drinkName + " not available at " + order.branch;
                }

                double price = rs.getDouble("price");
                int availableStock = rs.getInt("quantity");

                if (availableStock < item.quantity) {
                    conn.rollback();
                    return "Error: Insufficient stock for " + item.drinkName + " at " + order.branch +
                           ". Available: " + availableStock;
                }

                totalCost += price * item.quantity;
                drinkQuantities.put(item.drinkName, item.quantity);

                rs.close();
                ps.close();
            }

            // Insert order
            PreparedStatement psOrder = conn.prepareStatement(
                "INSERT INTO orders (customer_name, branch, total_cost) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            psOrder.setString(1, order.customerName);
            psOrder.setString(2, order.branch);
            psOrder.setDouble(3, totalCost);
            psOrder.executeUpdate();

            ResultSet rsOrder = psOrder.getGeneratedKeys();
            int orderId = 0;
            if (rsOrder.next()) {
                orderId = rsOrder.getInt(1);
            }
            rsOrder.close();
            psOrder.close();

            // Insert order items and update stock
            for (OrderItem item : order.items) {
                // Insert order item
                PreparedStatement psItem = conn.prepareStatement(
                    "INSERT INTO order_items (order_id, drink_name, quantity, price) " +
                    "SELECT ?, d.name, ?, d.price FROM drinks d WHERE d.name = ?");
                psItem.setInt(1, orderId);
                psItem.setInt(2, item.quantity);
                psItem.setString(3, item.drinkName);
                psItem.executeUpdate();
                psItem.close();

                // Update stock
                PreparedStatement psStock = conn.prepareStatement(
                    "UPDATE stock SET quantity = quantity - ? " +
                    "WHERE branch = ? AND drink_id = (SELECT id FROM drinks WHERE name = ?)");
                psStock.setInt(1, item.quantity);
                psStock.setString(2, order.branch);
                psStock.setString(3, item.drinkName);
                psStock.executeUpdate();
                psStock.close();
            }

            conn.commit();
            conn.close();

            return "Order placed successfully for " + order.customerName + " at " + order.branch +
                   ". Total: KSH" + totalCost;

        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<String> getCustomers() throws RemoteException {
        List<String> customers = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT DISTINCT customer_name FROM orders ORDER BY customer_name");

            while (rs.next()) {
                customers.add(rs.getString("customer_name"));
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    @Override
    public List<String> getBranchReport() throws RemoteException {
        List<String> report = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT branch, COUNT(*) as order_count FROM orders GROUP BY branch");

            Map<String, Integer> ordersByBranch = new LinkedHashMap<>();
            for (String branch : BRANCH_DISPLAY_ORDER) {
                ordersByBranch.put(branch, 0);
            }

            while (rs.next()) {
                String branch = rs.getString("branch");
                int count = rs.getInt("order_count");
                if (!ordersByBranch.containsKey(branch)) {
                    ordersByBranch.put(branch, 0);
                }
                ordersByBranch.put(branch, count);
            }

            rs.close();
            stmt.close();
            conn.close();

            for (Map.Entry<String, Integer> entry : ordersByBranch.entrySet()) {
                report.add(formatLocation(entry.getKey()) + ": " + entry.getValue() + " orders");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    @Override
    public List<String> getRevenuePerBranch() throws RemoteException {
        List<String> revenues = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT branch, SUM(total_cost) as revenue FROM orders GROUP BY branch");

            Map<String, Double> revenueByBranch = new LinkedHashMap<>();
            for (String branch : BRANCH_DISPLAY_ORDER) {
                revenueByBranch.put(branch, 0.0);
            }

            while (rs.next()) {
                String branch = rs.getString("branch");
                double revenue = rs.getDouble("revenue");
                if (!revenueByBranch.containsKey(branch)) {
                    revenueByBranch.put(branch, 0.0);
                }
                revenueByBranch.put(branch, revenue);
            }

            rs.close();
            stmt.close();
            conn.close();

            for (Map.Entry<String, Double> entry : revenueByBranch.entrySet()) {
                revenues.add(formatLocation(entry.getKey()) + ": KSH" + entry.getValue());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return revenues;
    }

    @Override
    public double getTotalRevenue() throws RemoteException {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT SUM(total_cost) as total FROM orders");

            if (rs.next()) {
                double total = rs.getDouble("total");
                rs.close();
                stmt.close();
                conn.close();
                return total;
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public List<String> getLowStockAlerts() throws RemoteException {
        List<String> alerts = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT s.branch, d.name, s.quantity, s.threshold " +
                "FROM stock s JOIN drinks d ON s.drink_id = d.id " +
                "WHERE s.quantity < s.threshold ORDER BY s.branch, d.name");

            while (rs.next()) {
                alerts.add("LOW STOCK: " + formatLocation(rs.getString("branch")) + " - " + rs.getString("name") +
                          " (Quantity: " + rs.getInt("quantity") + ", Threshold: " + rs.getInt("threshold") + ")");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alerts;
    }

    @Override
    public List<Drink> getAvailableDrinks() throws RemoteException {
        List<Drink> drinks = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, name, price FROM drinks ORDER BY name");

            while (rs.next()) {
                drinks.add(new Drink(rs.getInt("id"), rs.getString("name"), rs.getDouble("price")));
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return drinks;
    }

    @Override
    public String restockBranch(String branch, String drinkName, int quantity) throws RemoteException {
        lock.lock();
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            int drinkId = findDrinkIdByName(conn, drinkName);
            if (drinkId == -1) {
                conn.rollback();
                return "Error: Drink " + drinkName + " not found.";
            }

            String result = executeRestockTransfer(conn, branch, drinkId, quantity);
            if (result.startsWith("Error:")) {
                conn.rollback();
                return result;
            }

            conn.commit();
            return result;

        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error: " + e.getMessage();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean restockDrink(String branch, int drinkId, int quantity) throws RemoteException {
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

    @Override
    public List<String> getRestockHistory() throws RemoteException {
        List<String> history = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT r.restock_date, d.name, r.branch, r.quantity_added " +
                 "FROM restocks r JOIN drinks d ON r.drink_id = d.id " +
                 "ORDER BY r.restock_date DESC, r.id DESC LIMIT 100")) {

            while (rs.next()) {
                history.add(
                    rs.getTimestamp("restock_date") + "\t"
                    + formatLocation(rs.getString("branch")) + "\t"
                    + rs.getString("name") + "\t"
                    + rs.getInt("quantity_added")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    @Override
    public Map<String, Integer> getStockByBranch(int drinkId) throws RemoteException {
        Map<String, Integer> stockByBranch = new LinkedHashMap<>();
        for (String branch : BRANCH_DISPLAY_ORDER) {
            stockByBranch.put(formatLocation(branch), 0);
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT branch, quantity FROM stock WHERE drink_id = ?")) {
            ps.setInt(1, drinkId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stockByBranch.put(formatLocation(rs.getString("branch")), rs.getInt("quantity"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stockByBranch;
    }

    @Override
    public boolean restockMainBranch(int drinkId, int quantity) throws RemoteException {
        if (quantity <= 0) {
            return false;
        }

        lock.lock();
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            String sql = "UPDATE stock SET quantity = quantity + ? WHERE drink_id = ? AND branch = 'NAIROBI'";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, quantity);
                ps.setInt(2, drinkId);
                int updated = ps.executeUpdate();

                if (updated == 0) {
                    conn.rollback();
                    return false;
                }
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

    @Override
    public boolean distributeStock(String toBranch, int drinkId, int quantity) throws RemoteException {
        if (quantity <= 0) {
            return false;
        }
        if (toBranch == null || toBranch.equalsIgnoreCase("NAIROBI")) {
            return false;
        }

        lock.lock();
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Check Nairobi stock
            String checkSql = "SELECT quantity FROM stock WHERE drink_id = ? AND branch = 'NAIROBI'";
            int nairobiStock;
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, drinkId);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    nairobiStock = rs.getInt("quantity");
                }
            }

            if (nairobiStock < quantity) {
                conn.rollback();
                return false;
            }

            // Deduct from Nairobi
            String deductSql = "UPDATE stock SET quantity = quantity - ? WHERE drink_id = ? AND branch = 'NAIROBI'";
            try (PreparedStatement deductPs = conn.prepareStatement(deductSql)) {
                deductPs.setInt(1, quantity);
                deductPs.setInt(2, drinkId);
                deductPs.executeUpdate();
            }

            // Add to target branch
            String addSql = "UPDATE stock SET quantity = quantity + ? WHERE drink_id = ? AND branch = ?";
            try (PreparedStatement addPs = conn.prepareStatement(addSql)) {
                addPs.setInt(1, quantity);
                addPs.setInt(2, drinkId);
                addPs.setString(3, toBranch.toUpperCase());
                addPs.executeUpdate();
            }

            // Log transfer
            String logSql = "INSERT INTO stock_transfers (drink_id, from_branch, to_branch, quantity) VALUES (?, 'NAIROBI', ?, ?)";
            try (PreparedStatement logPs = conn.prepareStatement(logSql)) {
                logPs.setInt(1, drinkId);
                logPs.setString(2, toBranch.toUpperCase());
                logPs.setInt(3, quantity);
                logPs.executeUpdate();
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

    @Override
    public List<String> getDistributionHistory() throws RemoteException {
        List<String> history = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT st.transfer_date, d.name, st.from_branch, st.to_branch, st.quantity " +
                 "FROM stock_transfers st JOIN drinks d ON st.drink_id = d.id " +
                 "ORDER BY st.transfer_date DESC")) {

            while (rs.next()) {
                history.add(rs.getTimestamp("transfer_date") + " - " + rs.getString("name") + " - " +
                            formatLocation(rs.getString("from_branch")) + " → " +
                            formatLocation(rs.getString("to_branch")) + " - " + rs.getInt("quantity") + " units");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    private int findDrinkIdByName(Connection conn, String drinkName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM drinks WHERE name = ?")) {
            ps.setString(1, drinkName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }

    private String findDrinkNameById(Connection conn, int drinkId) throws SQLException {
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

    private int getStockQuantity(Connection conn, String branch, int drinkId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
            "SELECT quantity FROM stock WHERE branch = ? AND drink_id = ?")) {
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

    private int updateStock(Connection conn, String branch, int drinkId, int delta) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
            "UPDATE stock SET quantity = quantity + ? WHERE branch = ? AND drink_id = ?")) {
            ps.setInt(1, delta);
            ps.setString(2, branch);
            ps.setInt(3, drinkId);
            return ps.executeUpdate();
        }
    }

    private void insertRestockRecord(Connection conn, int drinkId, String branch, int quantity) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO restocks (drink_id, branch, quantity_added) VALUES (?, ?, ?)") ) {
            ps.setInt(1, drinkId);
            ps.setString(2, branch);
            ps.setInt(3, quantity);
            ps.executeUpdate();
        }
    }

    private boolean updateBranchStockAndLog(Connection conn, String branch, int drinkId, int quantity) throws SQLException {
        int rowsAffected = updateStock(conn, branch, drinkId, quantity);
        if (rowsAffected == 0) {
            return false;
        }
        insertRestockRecord(conn, drinkId, branch, quantity);
        return true;
    }

    private String executeRestockTransfer(Connection conn, String branch, int drinkId, int quantity) throws SQLException {
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

    @Override
    public List<String> getAllStockLevels() throws RemoteException {
        List<String> stockLevels = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT s.branch, d.name as drink_name, s.quantity, s.threshold " +
                "FROM stock s JOIN drinks d ON s.drink_id = d.id " +
                "ORDER BY FIELD(s.branch, 'NAIROBI', 'NAKURU', 'MOMBASA', 'KISUMU'), d.name");

            while (rs.next()) {
                String branch = rs.getString("branch");
                String drinkName = rs.getString("drink_name");
                int quantity = rs.getInt("quantity");
                int threshold = rs.getInt("threshold");
                String status = quantity < threshold ? "LOW" : "OK";
                stockLevels.add(formatLocation(branch) + "\t" + drinkName + "\t" + quantity + "\t" + threshold + "\t" + status);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stockLevels;
    }

    private String formatLocation(String locationCode) {
        if (locationCode == null) {
            return "";
        }
        if ("NAIROBI".equalsIgnoreCase(locationCode)) {
            return "HEADQUARTERS (NAIROBI)";
        }
        return locationCode.toUpperCase();
    }

    private String normalizeBranch(String branch) {
        if (branch == null) {
            return "";
        }
        if (branch.toUpperCase().contains("NAIROBI")) {
            return "NAIROBI";
        }
        return branch.trim().toUpperCase();
    }
}
