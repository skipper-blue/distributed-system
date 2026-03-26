package client;

import common.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class CustomerClient extends JFrame {
    private RemoteService service;
    private JComboBox<String> branchCombo;
    private JTextField customerNameField;
    private JComboBox<Drink> drinkCombo;
    private JTextField quantityField;
    private JTextArea orderArea;
    private JButton addItemButton;
    private JButton placeOrderButton;
    private JButton clearButton;

    private List<OrderItem> currentItems = new ArrayList<>();
    private String[] branches = {"NAIROBI", "NAKURU", "MOMBASA", "KISUMU"};
    private List<Drink> availableDrinks = new ArrayList<>();

    public CustomerClient() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            service = (RemoteService) registry.lookup("DrinkService");
            availableDrinks = service.getAvailableDrinks();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Cannot connect to server: " + e.getMessage(),
                                        "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        initializeUI();
    }

    private void initializeUI() {
        setTitle("Distributed Drinks System - Customer Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Branch selection
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Branch:"), gbc);
        gbc.gridx = 1;
        branchCombo = new JComboBox<>(branches);
        mainPanel.add(branchCombo, gbc);

        // Customer name
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Customer Name:"), gbc);
        gbc.gridx = 1;
        customerNameField = new JTextField(20);
        mainPanel.add(customerNameField, gbc);

        // Drink selection
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Drink:"), gbc);
        gbc.gridx = 1;
        drinkCombo = new JComboBox<>(availableDrinks.toArray(new Drink[0]));
        mainPanel.add(drinkCombo, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        quantityField = new JTextField(5);
        mainPanel.add(quantityField, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 4;
        addItemButton = new JButton("Add Item");
        addItemButton.addActionListener(e -> addItem());
        mainPanel.add(addItemButton, gbc);

        gbc.gridx = 1;
        clearButton = new JButton("Clear Order");
        clearButton.addActionListener(e -> clearOrder());
        mainPanel.add(clearButton, gbc);

        // Order area
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        mainPanel.add(new JLabel("Current Order:"), gbc);

        gbc.gridy = 6;
        orderArea = new JTextArea(10, 30);
        orderArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(orderArea);
        mainPanel.add(scrollPane, gbc);

        // Place order button
        gbc.gridy = 7;
        placeOrderButton = new JButton("Place Order");
        placeOrderButton.addActionListener(e -> placeOrder());
        mainPanel.add(placeOrderButton, gbc);

        add(mainPanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    private void addItem() {
        try {
            Drink selectedDrink = (Drink) drinkCombo.getSelectedItem();
            int quantity = Integer.parseInt(quantityField.getText().trim());

            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive",
                                            "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            currentItems.add(new OrderItem(selectedDrink.getName(), quantity));
            updateOrderDisplay();

            quantityField.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity",
                                        "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearOrder() {
        currentItems.clear();
        updateOrderDisplay();
        customerNameField.setText("");
    }

    private void updateOrderDisplay() {
        StringBuilder sb = new StringBuilder();
        for (OrderItem item : currentItems) {
            sb.append(item.drinkName).append(" x ").append(item.quantity).append("\n");
        }
        orderArea.setText(sb.toString());
    }

    private void placeOrder() {
        String customerName = customerNameField.getText().trim();
        String branch = (String) branchCombo.getSelectedItem();

        if (customerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter customer name",
                                        "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add items to order",
                                        "Empty Order", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Order order = new Order(customerName, branch, new ArrayList<>(currentItems));
            String result = service.placeOrder(order);

            JOptionPane.showMessageDialog(this, result, "Order Result",
                                        result.startsWith("Order placed") ?
                                        JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

            if (result.startsWith("Order placed")) {
                clearOrder();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error placing order: " + e.getMessage(),
                                        "Order Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CustomerClient().setVisible(true);
        });
    }
}
