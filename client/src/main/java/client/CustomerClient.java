package client;

import common.Drink;
import common.Order;
import common.OrderItem;
import common.RemoteService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

public class CustomerClient extends JFrame {
    private static final Color APP_BACKGROUND = new Color(244, 247, 252);
    private static final Color CARD_BACKGROUND = new Color(255, 255, 255);
    private static final Color PRIMARY_BLUE = new Color(30, 97, 180);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    private final String[] branches = {"NAIROBI", "NAKURU", "MOMBASA", "KISUMU"};
    private final List<OrderItem> currentItems = new ArrayList<>();

    private RemoteService service;
    private List<Drink> availableDrinks = new ArrayList<>();

    private JComboBox<String> branchCombo;
    private JTextField customerNameField;
    private JComboBox<Drink> drinkCombo;
    private JSpinner quantitySpinner;
    private DefaultTableModel orderTableModel;
    private JLabel totalItemsLabel;

    public CustomerClient() {
        connectToServer();
        initializeUI();
    }

    private void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            service = (RemoteService) registry.lookup("DrinkService");
            availableDrinks = service.getAvailableDrinks();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Cannot connect to server: " + e.getMessage(),
                "Connection Error",
                JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }
    }

    private void initializeUI() {
        setTitle("Distributed Drinks System - Customer Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 580));

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        root.setBackground(APP_BACKGROUND);
        setContentPane(root);

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createCenterPanel(), BorderLayout.CENTER);
        root.add(createFooterPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel titleLabel = new JLabel("Customer Ordering");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(new Color(24, 45, 78));

        JLabel subtitleLabel = new JLabel("Create and submit branch drink orders");
        subtitleLabel.setFont(SUBTITLE_FONT);
        subtitleLabel.setForeground(new Color(90, 102, 121));

        header.add(titleLabel);
        header.add(subtitleLabel);
        return header;
    }

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(createOrderDetailsCard(), BorderLayout.NORTH);
        center.add(createCurrentOrderCard(), BorderLayout.CENTER);
        return center;
    }

    private JPanel createOrderDetailsCard() {
        JPanel card = createCard("Order Details");
        card.setLayout(new BorderLayout());

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        branchCombo = new JComboBox<>(branches);
        customerNameField = new JTextField();
        drinkCombo = new JComboBox<>(availableDrinks.toArray(new Drink[0]));
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));

        styleInput(branchCombo);
        styleInput(customerNameField);
        styleInput(drinkCombo);
        styleInput(quantitySpinner);
        customerNameField.setPreferredSize(new Dimension(260, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;

        addFormRow(grid, gbc, 0, "Branch", branchCombo);
        addFormRow(grid, gbc, 1, "Customer Name", customerNameField);
        addFormRow(grid, gbc, 2, "Drink", drinkCombo);
        addFormRow(grid, gbc, 3, "Quantity", quantitySpinner);

        JButton addItemButton = createButton("Add Item", PRIMARY_BLUE);
        JButton clearButton = createButton("Clear Order", new Color(123, 131, 144));
        addItemButton.addActionListener(e -> addItem());
        clearButton.addActionListener(e -> clearOrder());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        buttonRow.setOpaque(false);
        buttonRow.add(addItemButton);
        buttonRow.add(clearButton);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        grid.add(buttonRow, gbc);

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createCurrentOrderCard() {
        JPanel card = createCard("Current Order");
        card.setLayout(new BorderLayout(0, 10));

        orderTableModel = new DefaultTableModel(new Object[]{"Drink", "Quantity"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 1 ? Integer.class : String.class;
            }
        };

        JTable orderTable = new JTable(orderTableModel);
        orderTable.setFont(BODY_FONT);
        orderTable.setRowHeight(26);
        orderTable.setFillsViewportHeight(true);
        orderTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        orderTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(217, 222, 232), 1));
        scrollPane.setPreferredSize(new Dimension(500, 220));
        card.add(scrollPane, BorderLayout.CENTER);

        totalItemsLabel = new JLabel("Total Items: 0", SwingConstants.RIGHT);
        totalItemsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalItemsLabel.setForeground(new Color(61, 72, 89));
        card.add(totalItemsLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);

        JButton placeOrderButton = createButton("Place Order", PRIMARY_BLUE);
        placeOrderButton.setPreferredSize(new Dimension(140, 36));
        placeOrderButton.addActionListener(e -> placeOrder());
        footer.add(placeOrderButton);
        return footer;
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel();
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 236), 1),
                BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(),
                        title,
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 13),
                        new Color(53, 72, 107)
                    ),
                    BorderFactory.createEmptyBorder(8, 10, 10, 10)
                )
            )
        );
        return card;
    }

    private void styleInput(java.awt.Component component) {
        component.setFont(BODY_FONT);
        if (component instanceof javax.swing.JComponent) {
            ((javax.swing.JComponent) component).setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(206, 212, 224), 1),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
                )
            );
            ((javax.swing.JComponent) component).setPreferredSize(new Dimension(260, 30));
        }
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text + ":");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(54, 63, 80));
        return label;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(createFormLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
    }

    private JButton createButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return button;
    }

    private void addItem() {
        Drink selectedDrink = (Drink) drinkCombo.getSelectedItem();
        if (selectedDrink == null) {
            JOptionPane.showMessageDialog(
                this,
                "Please select a drink.",
                "Missing Information",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int quantity = (Integer) quantitySpinner.getValue();
        mergeOrderItem(selectedDrink.getName(), quantity);
        updateOrderDisplay();
        quantitySpinner.setValue(1);
    }

    private void mergeOrderItem(String drinkName, int quantity) {
        for (OrderItem item : currentItems) {
            if (item.drinkName.equalsIgnoreCase(drinkName)) {
                item.quantity += quantity;
                return;
            }
        }
        currentItems.add(new OrderItem(drinkName, quantity));
    }

    private void clearOrder() {
        currentItems.clear();
        updateOrderDisplay();
    }

    private void updateOrderDisplay() {
        orderTableModel.setRowCount(0);
        int totalItems = 0;
        for (OrderItem item : currentItems) {
            orderTableModel.addRow(new Object[]{item.drinkName, item.quantity});
            totalItems += item.quantity;
        }
        totalItemsLabel.setText("Total Items: " + totalItems);
    }

    private void placeOrder() {
        String customerName = customerNameField.getText().trim();
        String branch = (String) branchCombo.getSelectedItem();

        if (customerName.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter customer name.",
                "Missing Information",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (currentItems.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please add at least one item before placing the order.",
                "Empty Order",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            Order order = new Order(customerName, branch, new ArrayList<>(currentItems));
            String result = service.placeOrder(order);
            JOptionPane.showMessageDialog(
                this,
                result,
                "Order Result",
                result.startsWith("Order placed") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
            );

            if (result.startsWith("Order placed")) {
                clearOrder();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error placing order: " + e.getMessage(),
                "Order Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new CustomerClient().setVisible(true);
        });
    }
}
