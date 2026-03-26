package server;

import common.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class RemoteServiceImpl extends UnicastRemoteObject implements RemoteService {

    private final ReentrantLock lock = new ReentrantLock();

    protected RemoteServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public String placeOrder(Order order) throws RemoteException {
        lock.lock();
        try {
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
                "SELECT branch, COUNT(*) as order_count FROM orders GROUP BY branch ORDER BY branch");

            while (rs.next()) {
                report.add(rs.getString("branch") + ": " + rs.getInt("order_count") + " orders");
            }

            rs.close();
            stmt.close();
            conn.close();

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
                "SELECT branch, SUM(total_cost) as revenue FROM orders GROUP BY branch ORDER BY branch");

            while (rs.next()) {
                revenues.add(rs.getString("branch") + ": KSH" + rs.getDouble("revenue"));
            }

            rs.close();
            stmt.close();
            conn.close();

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
                alerts.add("LOW STOCK: " + rs.getString("branch") + " - " + rs.getString("name") +
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
}
