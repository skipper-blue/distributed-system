package client;

import common.Drink;
import common.RemoteService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;

public class AdminClient extends JFrame {
    private static final String SERVER_HOST = System.getProperty(
        "server.host",
        System.getenv().getOrDefault("DRINKS_SERVER_HOST", "localhost")
    );
    private static final String MAIN_BRANCH = "NAIROBI";
    private static final Color APP_BACKGROUND = new Color(11, 24, 42);
    private static final Color CARD_BACKGROUND = new Color(19, 38, 64);
    private static final Color HEADER_TEXT = new Color(228, 239, 255);
    private static final Color BODY_TEXT = new Color(215, 229, 249);
    private static final Color STATUS_OK = new Color(73, 203, 145);
    private static final Color STATUS_PENDING = new Color(234, 187, 94);
    private static final Color STATUS_ERROR = new Color(242, 130, 130);
    private static final Color NAV_TEXT = new Color(207, 224, 246);
    private static final Color NAV_ACTIVE_TEXT = new Color(240, 249, 255);
    private static final Color NAV_ACTIVE_BG = new Color(17, 122, 190);
    private static final Color NAV_ACTIVE_BORDER = new Color(84, 202, 245);
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private RemoteService service;
    private JEditorPane displayArea;
    private JLabel statusLabel;
    private JButton customersButton;
    private JButton branchReportButton;
    private JButton revenueBranchButton;
    private JButton totalRevenueButton;
    private JButton lowStockButton;
    private JButton restockButton;
    private JButton restockHistoryButton;
    private JButton restockNairobiButton;
    private JButton distributeStockButton;
    private JButton distributionHistoryButton;
    private JButton refreshButton;
    private JButton exitButton;
    private JButton activeNavButton;
    private javax.swing.Timer autoRefreshTimer;
    private Timer reconnectTimer;
    private volatile boolean isConnected = false;

    public AdminClient() {
        initializeUI();
        connectToServer();
    }

    private void initializeUI() {
        setTitle("Distributed Drinks System - Admin Dashboard (Headquarters)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1220, 780));

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.setBackground(APP_BACKGROUND);
        setContentPane(root);

        root.add(createHeaderPanel(), BorderLayout.NORTH);
        root.add(createSidebarPanel(), BorderLayout.WEST);
        root.add(createContentPanel(), BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (autoRefreshTimer != null) {
                    autoRefreshTimer.stop();
                }
                cancelReconnectTimer();
            }
        });

        setLocationRelativeTo(null);
    }

    private JPanel createHeaderPanel() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(12, 8));

        JPanel textWrap = new JPanel();
        textWrap.setOpaque(false);
        textWrap.setLayout(new BoxLayout(textWrap, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(HEADER_TEXT);

        JLabel subtitleLabel = new JLabel("Headquarters (Nairobi) operations and analytics");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(166, 192, 224));

        textWrap.add(titleLabel);
        textWrap.add(Box.createVerticalStrut(2));
        textWrap.add(subtitleLabel);

        statusLabel = new JLabel("Initializing...", SwingConstants.RIGHT);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLabel.setForeground(STATUS_PENDING);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(54, 68, 89));
        statusLabel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(107, 124, 148), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
            )
        );

        JLabel mainSiteLabel = new JLabel("Headquarters: Nairobi");
        mainSiteLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        mainSiteLabel.setForeground(new Color(229, 246, 255));
        mainSiteLabel.setOpaque(true);
        mainSiteLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainSiteLabel.setBackground(new Color(19, 122, 164));
        mainSiteLabel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(92, 206, 238), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
            )
        );

        card.add(textWrap, BorderLayout.WEST);
        card.add(mainSiteLabel, BorderLayout.CENTER);
        card.add(statusLabel, BorderLayout.EAST);
        return card;
    }

    private JPanel createSidebarPanel() {
        JPanel card = createCard();
        card.setPreferredSize(new Dimension(270, 470));
        card.setLayout(new BorderLayout(0, 12));

        JLabel menuTitle = new JLabel("Headquarters (Nairobi) - Reports");
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        menuTitle.setForeground(new Color(174, 198, 230));
        menuTitle.setBorder(new EmptyBorder(0, 4, 2, 0));
        card.add(menuTitle, BorderLayout.NORTH);

        JPanel reportButtons = new JPanel();
        reportButtons.setOpaque(false);
        reportButtons.setLayout(new BoxLayout(reportButtons, BoxLayout.Y_AXIS));

        customersButton = createNavButton("Customers", new Color(35, 63, 95), new Color(77, 116, 160), this::showCustomers);
        branchReportButton = createNavButton("Orders by Location", new Color(37, 68, 98), new Color(77, 116, 160), this::showBranchReport);
        revenueBranchButton = createNavButton("Revenue by Location", new Color(37, 72, 92), new Color(75, 120, 149), this::showRevenuePerBranch);
        totalRevenueButton = createNavButton("Total Revenue", new Color(33, 74, 82), new Color(75, 130, 140), this::showTotalRevenue);
        lowStockButton = createNavButton("Low Stock Alerts", new Color(79, 58, 68), new Color(148, 107, 122), this::showLowStockAlerts);
        restockButton = createNavButton("Restock Branch", new Color(60, 80, 100), new Color(120, 140, 160), this::showRestockDialog);
        restockHistoryButton = createNavButton("Restock History", new Color(60, 90, 95), new Color(110, 155, 160), this::showRestockHistory);
        restockNairobiButton = createNavButton("Restock Nairobi", new Color(85, 65, 45), new Color(168, 123, 85), this::showRestockNairobi);
        distributeStockButton = createNavButton("Distribute Stock", new Color(50, 85, 75), new Color(95, 165, 145), this::showDistributeStock);
        distributionHistoryButton = createNavButton("Distribution History", new Color(55, 80, 90), new Color(110, 150, 170), this::showDistributionHistory);

        addNavButton(reportButtons, customersButton);
        addNavButton(reportButtons, branchReportButton);
        addNavButton(reportButtons, revenueBranchButton);
        addNavButton(reportButtons, totalRevenueButton);
        addNavButton(reportButtons, lowStockButton);
        addNavButton(reportButtons, restockButton);
        addNavButton(reportButtons, restockHistoryButton);
        addNavButton(reportButtons, restockNairobiButton);
        addNavButton(reportButtons, distributeStockButton);
        addNavButton(reportButtons, distributionHistoryButton);
        reportButtons.add(Box.createVerticalGlue());

        JPanel utilityButtons = new JPanel();
        utilityButtons.setOpaque(false);
        utilityButtons.setLayout(new BoxLayout(utilityButtons, BoxLayout.Y_AXIS));

        refreshButton = createNavButton("Refresh Dashboard", new Color(40, 59, 86), new Color(96, 125, 163), this::refreshConnectionOrDashboard);
        exitButton = createNavButton("Exit", new Color(58, 60, 83), new Color(128, 131, 161), () -> System.exit(0));
        addNavButton(utilityButtons, refreshButton);
        addNavButton(utilityButtons, exitButton);

        card.add(reportButtons, BorderLayout.CENTER);
        card.add(utilityButtons, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createContentPanel() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 10));

        displayArea = new JEditorPane();
        displayArea.setEditable(false);
        displayArea.setContentType("text/html");
        displayArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        displayArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        displayArea.setForeground(BODY_TEXT);
        displayArea.setBackground(CARD_BACKGROUND);
        displayArea.setMargin(new Insets(0, 0, 0, 0));
        displayArea.setText(buildWelcomeHtml());

        JScrollPane scrollPane = new JScrollPane(
            displayArea,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(214, 223, 238), 1));
        scrollPane.getViewport().setBackground(CARD_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scrollPane, BorderLayout.CENTER);

        JLabel hint = new JLabel("Headquarters pinned first | Auto refresh every 30 seconds while connected.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(new Color(151, 177, 209));
        card.add(hint, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createCard() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(62, 91, 123), 1),
                new EmptyBorder(14, 14, 14, 14)
            )
        );
        return panel;
    }

    private JButton createNavButton(String text, Color background, Color borderColor, Runnable action) {
        JButton button = new JButton(text);
        button.setUI(new BasicButtonUI());
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(NAV_TEXT);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setOpaque(true);
        button.putClientProperty("defaultBg", background);
        button.putClientProperty("defaultBorder", borderColor);
        button.setBorder(createButtonBorder(borderColor));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(0.0f);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        button.setPreferredSize(new Dimension(220, 48));
        button.setMinimumSize(new Dimension(180, 48));
        button.addActionListener(e -> action.run());
        return button;
    }

    private void addNavButton(JPanel panel, JButton button) {
        panel.add(button);
        panel.add(Box.createVerticalStrut(8));
    }

    private javax.swing.border.Border createButtonBorder(Color borderColor) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 1),
            BorderFactory.createEmptyBorder(11, 12, 11, 12)
        );
    }

    private void setActiveNavButton(JButton button) {
        if (activeNavButton != null) {
            applyNavButtonState(activeNavButton, false);
        }
        activeNavButton = button;
        if (activeNavButton != null) {
            applyNavButtonState(activeNavButton, true);
        }
    }

    private void applyNavButtonState(JButton button, boolean active) {
        if (active) {
            button.setBackground(NAV_ACTIVE_BG);
            button.setForeground(NAV_ACTIVE_TEXT);
            button.setBorder(createButtonBorder(NAV_ACTIVE_BORDER));
            return;
        }

        Color defaultBg = (Color) button.getClientProperty("defaultBg");
        Color defaultBorder = (Color) button.getClientProperty("defaultBorder");
        button.setBackground(defaultBg);
        button.setForeground(NAV_TEXT);
        button.setBorder(createButtonBorder(defaultBorder));
    }

    private void connectToServer() {
        updateStatus("Connecting to server...", STATUS_PENDING);
        try {
            Registry registry = LocateRegistry.getRegistry(SERVER_HOST, 1099);
            service = (RemoteService) registry.lookup("DrinkService");

            isConnected = true;
            enableButtons(true);
            cancelReconnectTimer();
            startAutoRefresh();
            updateStatus("Connected", STATUS_OK);
            loadDashboard();
        } catch (Exception e) {
            isConnected = false;
            enableButtons(false);
            updateStatus("Connection failed - retrying...", STATUS_ERROR);
            displayArea.setText(buildConnectionHelpHtml(e.getMessage()));
            startReconnectTimer();
        }
    }

    private void startAutoRefresh() {
        if (autoRefreshTimer == null) {
            autoRefreshTimer = new javax.swing.Timer(30000, e -> {
                if (isConnected) {
                    loadDashboard();
                }
            });
        }
        if (!autoRefreshTimer.isRunning()) {
            autoRefreshTimer.start();
        }
    }

    private synchronized void startReconnectTimer() {
        if (reconnectTimer != null) {
            return;
        }
        reconnectTimer = new Timer("admin-reconnect", true);
        reconnectTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isConnected) {
                    cancelReconnectTimer();
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    if (!isConnected) {
                        connectToServer();
                    }
                });
            }
        }, 5000, 5000);
    }

    private synchronized void cancelReconnectTimer() {
        if (reconnectTimer != null) {
            reconnectTimer.cancel();
            reconnectTimer = null;
        }
    }

    private void refreshConnectionOrDashboard() {
        if (isConnected) {
            loadDashboard();
        } else {
            connectToServer();
        }
    }

    private void updateStatus(String status, Color color) {
        statusLabel.setText(status + " | Last refresh: " + now());
        statusLabel.setForeground(color);
        if (STATUS_OK.equals(color)) {
            statusLabel.setBackground(new Color(24, 84, 63));
            statusLabel.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(78, 171, 133), 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                )
            );
        } else if (STATUS_ERROR.equals(color)) {
            statusLabel.setBackground(new Color(90, 47, 56));
            statusLabel.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(206, 117, 135), 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                )
            );
        } else {
            statusLabel.setBackground(new Color(79, 63, 35));
            statusLabel.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(197, 155, 87), 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                )
            );
        }
    }

    private void enableButtons(boolean enabled) {
        customersButton.setEnabled(enabled);
        branchReportButton.setEnabled(enabled);
        revenueBranchButton.setEnabled(enabled);
        totalRevenueButton.setEnabled(enabled);
        lowStockButton.setEnabled(enabled);
        restockButton.setEnabled(enabled);
        restockHistoryButton.setEnabled(enabled);
        restockNairobiButton.setEnabled(enabled);
        distributeStockButton.setEnabled(enabled);
        distributionHistoryButton.setEnabled(enabled);
        refreshButton.setEnabled(true);
        exitButton.setEnabled(true);
    }

    private void loadDashboard() {
        setActiveNavButton(null);
        runReport("Refreshing dashboard...", this::buildDashboardReportHtml);
    }

    private void showCustomers() {
        setActiveNavButton(customersButton);
        runReport("Loading customers...", () -> {
            List<ReportSection> sections = new ArrayList<>();
            sections.add(new ReportSection(
                "Customers",
                normalizeCurrencyList(service.getCustomers()),
                "No customers registered yet."
            ));
            return buildReportHtml(
                "Customer Registry",
                "Registered customer records across all branches",
                sections,
                null,
                null
            );
        });
    }

    private void showBranchReport() {
        setActiveNavButton(branchReportButton);
        runReport("Loading location report...", () -> {
            List<ReportSection> sections = new ArrayList<>();
            sections.add(new ReportSection(
                "Orders by Location",
                prioritizeNairobi(normalizeCurrencyList(service.getBranchReport())),
                "No orders placed yet."
            ));
            return buildReportHtml(
                "Orders by Location",
                "Order count and activity by location (Headquarters pinned first)",
                sections,
                "Headquarters",
                "Nairobi"
            );
        });
    }

    private void showRevenuePerBranch() {
        setActiveNavButton(revenueBranchButton);
        runReport("Loading location revenue...", () -> {
            List<ReportSection> sections = new ArrayList<>();
            sections.add(new ReportSection(
                "Revenue by Location (KSh)",
                prioritizeNairobi(normalizeCurrencyList(service.getRevenuePerBranch())),
                "No revenue data available."
            ));
            return buildReportHtml(
                "Revenue by Location (KSh)",
                "Revenue performance by location (Headquarters pinned first)",
                sections,
                "Headquarters",
                "Nairobi"
            );
        });
    }

    private void showTotalRevenue() {
        setActiveNavButton(totalRevenueButton);
        runReport("Loading total revenue...", () -> {
            double total = service.getTotalRevenue();
            return buildReportHtml(
                "Total Revenue (KSh)",
                "Overall income generated from all branches",
                new ArrayList<>(),
                "Total Revenue (KSh)",
                "KSh " + MONEY_FORMAT.format(total)
            );
        });
    }

    private void showLowStockAlerts() {
        setActiveNavButton(lowStockButton);
        runReport("Loading stock alerts...", () -> {
            List<ReportSection> sections = new ArrayList<>();
            sections.add(new ReportSection(
                "Low Stock Alerts",
                normalizeCurrencyList(service.getLowStockAlerts()),
                "All products are above threshold."
            ));
            return buildReportHtml(
                "Low Stock Alerts",
                "Inventory items below threshold",
                sections,
                null,
                null
            );
        });
    }

    private void showRestockHistory() {
        setActiveNavButton(restockHistoryButton);
        runReport("Loading restock history...", () -> {
            List<ReportSection> sections = new ArrayList<>();
            sections.add(new ReportSection(
                "Restock History",
                formatRestockHistoryItems(service.getRestockHistory()),
                "No restock records found."
            ));
            return buildReportHtml(
                "Restock History",
                "Audit trail for branch restock operations",
                sections,
                null,
                null
            );
        });
    }

    private void showRestockNairobi() {
        setActiveNavButton(restockNairobiButton);
        if (!isConnected) {
            displayArea.setText(buildConnectionHelpHtml("Not connected to server."));
            return;
        }

        JFrame nairobiFrame = new JFrame("Restock Nairobi (Main Warehouse)");
        nairobiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        nairobiFrame.setSize(600, 380);
        nairobiFrame.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        mainPanel.setBackground(APP_BACKGROUND);

        JLabel titleLabel = new JLabel("Add Stock to Nairobi Warehouse");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(255, 235, 205));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel drinkLabel = new JLabel("Select Drink:");
        drinkLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        drinkLabel.setForeground(BODY_TEXT);
        formPanel.add(drinkLabel);

        JComboBox<Drink> drinkCombo = new JComboBox<>();
        drinkCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        drinkCombo.setBackground(new Color(30, 45, 70));
        drinkCombo.setForeground(BODY_TEXT);
        try {
            for (Drink drink : service.getAvailableDrinks()) {
                drinkCombo.addItem(drink);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        formPanel.add(drinkCombo);
        formPanel.add(Box.createVerticalStrut(12));

        JLabel quantityLabel = new JLabel("Quantity to Add:");
        quantityLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        quantityLabel.setForeground(BODY_TEXT);
        formPanel.add(quantityLabel);

        JTextField quantityField = new JTextField();
        quantityField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        quantityField.setBackground(new Color(30, 45, 70));
        quantityField.setForeground(BODY_TEXT);
        quantityField.setPreferredSize(new Dimension(400, 32));
        formPanel.add(quantityField);
        formPanel.add(Box.createVerticalStrut(20));

        JLabel statusLabel = new JLabel();
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(STATUS_PENDING);
        formPanel.add(statusLabel);
        formPanel.add(Box.createVerticalStrut(10));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        JButton addButton = new JButton("ADD STOCK");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addButton.setBackground(new Color(46, 125, 50));
        addButton.setForeground(Color.WHITE);
        addButton.setPreferredSize(new Dimension(150, 36));
        addButton.addActionListener(e -> {
            try {
                Drink drink = (Drink) drinkCombo.getSelectedItem();
                int qty = Integer.parseInt(quantityField.getText().trim());

                if (drink == null || qty <= 0) {
                    statusLabel.setText("❌ Invalid input");
                    statusLabel.setForeground(STATUS_ERROR);
                    return;
                }

                boolean success = service.restockMainBranch(drink.getId(), qty);
                if (success) {
                    statusLabel.setText("✓ Nairobi warehouse updated successfully");
                    statusLabel.setForeground(STATUS_OK);
                    quantityField.setText("");
                } else {
                    statusLabel.setText("❌ Update failed");
                    statusLabel.setForeground(STATUS_ERROR);
                }
            } catch (Exception ex) {
                statusLabel.setText("❌ " + ex.getMessage());
                statusLabel.setForeground(STATUS_ERROR);
            }
        });

        JButton closeButton = new JButton("CLOSE");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeButton.setBackground(new Color(58, 60, 83));
        closeButton.setForeground(NAV_TEXT);
        closeButton.setPreferredSize(new Dimension(120, 36));
        closeButton.addActionListener(e -> nairobiFrame.dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(Box.createHorizontalStrut(12));
        buttonPanel.add(closeButton);
        formPanel.add(buttonPanel);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(62, 91, 123), 1));
        scrollPane.getViewport().setBackground(CARD_BACKGROUND);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        nairobiFrame.add(mainPanel);
        nairobiFrame.setVisible(true);
    }

    private void showDistributeStock() {
        setActiveNavButton(distributeStockButton);
        if (!isConnected) {
            displayArea.setText(buildConnectionHelpHtml("Not connected to server."));
            return;
        }

        JFrame distributeFrame = new JFrame("Distribute Stock from Nairobi");
        distributeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        distributeFrame.setSize(600, 450);
        distributeFrame.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        mainPanel.setBackground(APP_BACKGROUND);

        JLabel titleLabel = new JLabel("Transfer Stock to Branch");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(144, 202, 249));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel branchLabel = new JLabel("Transfer To:");
        branchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        branchLabel.setForeground(BODY_TEXT);
        formPanel.add(branchLabel);

        JComboBox<String> branchCombo = new JComboBox<>(new String[]{"NAKURU", "MOMBASA", "KISUMU"});
        branchCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        branchCombo.setBackground(new Color(30, 45, 70));
        branchCombo.setForeground(BODY_TEXT);
        formPanel.add(branchCombo);
        formPanel.add(Box.createVerticalStrut(12));

        JLabel drinkLabel = new JLabel("Select Drink:");
        drinkLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        drinkLabel.setForeground(BODY_TEXT);
        formPanel.add(drinkLabel);

        JComboBox<Drink> drinkCombo = new JComboBox<>();
        drinkCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        drinkCombo.setBackground(new Color(30, 45, 70));
        drinkCombo.setForeground(BODY_TEXT);
        try {
            for (Drink drink : service.getAvailableDrinks()) {
                drinkCombo.addItem(drink);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        formPanel.add(drinkCombo);
        formPanel.add(Box.createVerticalStrut(12));

        JLabel quantityLabel = new JLabel("Quantity to Transfer:");
        quantityLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        quantityLabel.setForeground(BODY_TEXT);
        formPanel.add(quantityLabel);

        JTextField quantityField = new JTextField();
        quantityField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        quantityField.setBackground(new Color(30, 45, 70));
        quantityField.setForeground(BODY_TEXT);
        quantityField.setPreferredSize(new Dimension(400, 32));
        formPanel.add(quantityField);
        formPanel.add(Box.createVerticalStrut(20));

        JLabel statusLabel = new JLabel();
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(STATUS_PENDING);
        formPanel.add(statusLabel);
        formPanel.add(Box.createVerticalStrut(10));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        JButton transferButton = new JButton("TRANSFER STOCK");
        transferButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        transferButton.setBackground(new Color(33, 150, 243));
        transferButton.setForeground(Color.WHITE);
        transferButton.setPreferredSize(new Dimension(180, 36));
        transferButton.addActionListener(e -> {
            try {
                String branch = branchCombo.getSelectedItem().toString();
                Drink drink = (Drink) drinkCombo.getSelectedItem();
                int qty = Integer.parseInt(quantityField.getText().trim());

                if (drink == null || qty <= 0) {
                    statusLabel.setText("❌ Invalid input");
                    statusLabel.setForeground(STATUS_ERROR);
                    return;
                }

                boolean success = service.distributeStock(branch, drink.getId(), qty);
                if (success) {
                    statusLabel.setText("✓ Stock transferred to " + branch + " successfully");
                    statusLabel.setForeground(STATUS_OK);
                    quantityField.setText("");
                } else {
                    statusLabel.setText("❌ Transfer failed (insufficient Nairobi stock?)");
                    statusLabel.setForeground(STATUS_ERROR);
                }
            } catch (Exception ex) {
                statusLabel.setText("❌ " + ex.getMessage());
                statusLabel.setForeground(STATUS_ERROR);
            }
        });

        JButton closeButton = new JButton("CLOSE");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeButton.setBackground(new Color(58, 60, 83));
        closeButton.setForeground(NAV_TEXT);
        closeButton.setPreferredSize(new Dimension(120, 36));
        closeButton.addActionListener(e -> distributeFrame.dispose());

        buttonPanel.add(transferButton);
        buttonPanel.add(Box.createHorizontalStrut(12));
        buttonPanel.add(closeButton);
        formPanel.add(buttonPanel);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(62, 91, 123), 1));
        scrollPane.getViewport().setBackground(CARD_BACKGROUND);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        distributeFrame.add(mainPanel);
        distributeFrame.setVisible(true);
    }

    private void showDistributionHistory() {
        setActiveNavButton(distributionHistoryButton);
        runReport("Loading distribution history...", () -> {
            List<ReportSection> sections = new ArrayList<>();
            sections.add(new ReportSection(
                "Distribution Log",
                normalizeCurrencyList(service.getDistributionHistory()),
                "No distribution records found."
            ));
            return buildReportHtml(
                "Distribution History",
                "Complete audit trail of Nairobi stock transfers to branches",
                sections,
                null,
                null
            );
        });
    }

    private void showRestockDialog() {
        setActiveNavButton(restockButton);
        if (!isConnected) {
            displayArea.setText(buildConnectionHelpHtml("Not connected to server."));
            return;
        }

        JDialog restockDialog = new JDialog(this, "Inventory Operations Center - Headquarters (Nairobi)", false);
        restockDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        restockDialog.setSize(1240, 780);
        restockDialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        mainPanel.setBackground(APP_BACKGROUND);

        JPanel headerPanel = createCard();
        headerPanel.setLayout(new BorderLayout(8, 6));
        JLabel titleLabel = new JLabel("Restock Inventory Workspace");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(HEADER_TEXT);
        JLabel subtitleLabel = new JLabel("Transfer stock from Headquarters to branches with live visibility and audit history.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(151, 177, 209));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        DefaultComboBoxModel<String> branchModel = new DefaultComboBoxModel<>(new String[]{"NAKURU", "MOMBASA", "KISUMU"});
        JComboBox<String> branchCombo = new JComboBox<>(branchModel);
        styleInventoryInput(branchCombo);

        DefaultComboBoxModel<Drink> drinkModel = new DefaultComboBoxModel<>();
        JComboBox<Drink> drinkCombo = new JComboBox<>(drinkModel);
        styleInventoryInput(drinkCombo);

        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(12, 1, 5000, 1));
        styleInventorySpinner(quantitySpinner);

        JLabel inlineStatusLabel = createInlineStatusLabel("Choose a destination branch, drink, and quantity to transfer.");

        JPanel formPanel = createCard();
        formPanel.setPreferredSize(new Dimension(330, 0));
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.add(createInventorySectionTitle("Restock Form", "Headquarters inventory is the source of every transfer."));
        formPanel.add(Box.createVerticalStrut(16));
        formPanel.add(createInventoryField("Destination Branch", branchCombo));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createInventoryField("Drink", drinkCombo));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createInventoryField("Quantity to Transfer", quantitySpinner));
        formPanel.add(Box.createVerticalStrut(16));
        formPanel.add(inlineStatusLabel);
        formPanel.add(Box.createVerticalStrut(16));

        DefaultTableModel stockTableModel = createReadOnlyTableModel("Branch", "Drink", "Current Stock", "Threshold", "Status");
        JTable stockTable = createInventoryTable(stockTableModel);

        DefaultTableModel historyTableModel = createReadOnlyTableModel("Restock Date", "Branch", "Drink", "Qty Added");
        JTable historyTable = createInventoryTable(historyTableModel);

        StockChartPanel stockChartPanel = new StockChartPanel(service);
        stockChartPanel.setPreferredSize(new Dimension(760, 320));

        JButton submitButton = createWorkspaceButton("Restock Now", new Color(38, 133, 93));
        submitButton.addActionListener(e -> {
            Drink selectedDrink = (Drink) drinkCombo.getSelectedItem();
            String selectedBranch = String.valueOf(branchCombo.getSelectedItem());
            int quantity = ((Number) quantitySpinner.getValue()).intValue();
            performRestock(selectedBranch, selectedDrink, quantity, quantitySpinner, inlineStatusLabel, stockTableModel, historyTableModel, stockChartPanel);
        });

        JButton refreshButton = createWorkspaceButton("Refresh Live Data", new Color(40, 59, 86));
        refreshButton.addActionListener(e -> {
            updateInlineStatus(inlineStatusLabel, "Refreshing inventory workspace...", STATUS_PENDING);
            loadStockData(stockTableModel);
            loadRestockHistoryTable(historyTableModel);
            stockChartPanel.refreshData();
        });

        JButton closeButton = createWorkspaceButton("Close Workspace", new Color(58, 60, 83));
        closeButton.addActionListener(e -> restockDialog.dispose());

        JPanel actionPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(submitButton);
        actionPanel.add(refreshButton);
        actionPanel.add(closeButton);
        formPanel.add(actionPanel);
        formPanel.add(Box.createVerticalStrut(16));

        JLabel guidanceLabel = new JLabel(
            "<html><body style='width:270px;color:#9fb8d8;font-family:Segoe UI,sans-serif;font-size:12px;'>"
                + "Tip: click any stock row to sync its branch and drink into the form. "
                + "The chart refreshes automatically every 5 seconds while this workspace is open."
                + "</body></html>"
        );
        guidanceLabel.setForeground(new Color(159, 184, 216));
        formPanel.add(guidanceLabel);

        drinkCombo.addActionListener(e -> stockChartPanel.setDrink((Drink) drinkCombo.getSelectedItem()));

        stockTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || stockTable.getSelectedRow() < 0) {
                return;
            }

            int modelRow = stockTable.convertRowIndexToModel(stockTable.getSelectedRow());
            String branch = String.valueOf(stockTableModel.getValueAt(modelRow, 0));
            String drinkName = String.valueOf(stockTableModel.getValueAt(modelRow, 1));
            if (!"HEADQUARTERS (NAIROBI)".equals(branch)) {
                branchCombo.setSelectedItem(toBranchCode(branch));
            }
            selectDrinkByName(drinkCombo, drinkName);
            updateInlineStatus(inlineStatusLabel, "Form synced from the selected stock row.", STATUS_PENDING);
        });

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.addTab("Stock Levels", createTableTab(stockTable));
        tabs.addTab("Restock Activity", createTableTab(historyTable));

        JPanel insightsPanel = new JPanel(new BorderLayout(0, 12));
        insightsPanel.setOpaque(false);
        insightsPanel.add(stockChartPanel, BorderLayout.NORTH);
        insightsPanel.add(tabs, BorderLayout.CENTER);

        mainPanel.add(formPanel, BorderLayout.WEST);
        mainPanel.add(insightsPanel, BorderLayout.CENTER);

        javax.swing.Timer workspaceRefreshTimer = new javax.swing.Timer(8000, e -> {
            loadStockData(stockTableModel);
            loadRestockHistoryTable(historyTableModel);
        });

        restockDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                workspaceRefreshTimer.start();
                stockChartPanel.startAutoRefresh();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                workspaceRefreshTimer.stop();
                stockChartPanel.stopAutoRefresh();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                workspaceRefreshTimer.stop();
                stockChartPanel.stopAutoRefresh();
            }
        });

        restockDialog.add(mainPanel);

        loadAvailableDrinks(drinkModel, drinkCombo, stockChartPanel, inlineStatusLabel);
        loadStockData(stockTableModel);
        loadRestockHistoryTable(historyTableModel);
        restockDialog.setVisible(true);
    }

    private void loadStockData(DefaultTableModel tableModel) {
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return service.getAllStockLevels();
            }

            @Override
            protected void done() {
                try {
                    List<String> stockData = get();
                    tableModel.setRowCount(0); // Clear table

                    for (String line : stockData) {
                        String[] parts = line.split("\t");
                        if (parts.length >= 5) {
                            tableModel.addRow(new Object[]{parts[0], parts[1], parts[2], parts[3], parts[4]});
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error loading stock data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void loadRestockHistoryTable(DefaultTableModel historyTableModel) {
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return service.getRestockHistory();
            }

            @Override
            protected void done() {
                try {
                    List<String> historyRows = get();
                    historyTableModel.setRowCount(0);

                    for (String row : historyRows) {
                        String[] parts = row.split("\t");
                        if (parts.length >= 4) {
                            historyTableModel.addRow(new Object[]{parts[0], parts[1], parts[2], parts[3]});
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error loading restock history: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void loadAvailableDrinks(
        DefaultComboBoxModel<Drink> drinkModel,
        JComboBox<Drink> drinkCombo,
        StockChartPanel stockChartPanel,
        JLabel inlineStatusLabel
    ) {
        new SwingWorker<List<Drink>, Void>() {
            @Override
            protected List<Drink> doInBackground() throws Exception {
                return service.getAvailableDrinks();
            }

            @Override
            protected void done() {
                try {
                    List<Drink> drinks = get();
                    drinkModel.removeAllElements();
                    for (Drink drink : drinks) {
                        drinkModel.addElement(drink);
                    }

                    if (drinkModel.getSize() > 0) {
                        drinkCombo.setSelectedIndex(0);
                        stockChartPanel.setDrink((Drink) drinkModel.getSelectedItem());
                        updateInlineStatus(inlineStatusLabel, "Live inventory is connected and refreshing automatically.", STATUS_OK);
                    } else {
                        updateInlineStatus(inlineStatusLabel, "No drink catalog entries were returned by the server.", STATUS_ERROR);
                    }
                } catch (Exception e) {
                    updateInlineStatus(inlineStatusLabel, "Unable to load drinks: " + e.getMessage(), STATUS_ERROR);
                }
            }
        }.execute();
    }

    private void performRestock(
        String branch,
        Drink drink,
        int quantity,
        JSpinner quantitySpinner,
        JLabel inlineStatusLabel,
        DefaultTableModel stockTableModel,
        DefaultTableModel historyTableModel,
        StockChartPanel stockChartPanel
    ) {
        if (drink == null) {
            updateInlineStatus(inlineStatusLabel, "Select a drink before submitting the transfer.", STATUS_ERROR);
            return;
        }
        if (branch == null || branch.trim().isEmpty()) {
            updateInlineStatus(inlineStatusLabel, "Select a destination branch before submitting the transfer.", STATUS_ERROR);
            return;
        }
        if (quantity <= 0) {
            updateInlineStatus(inlineStatusLabel, "Quantity must be greater than zero.", STATUS_ERROR);
            return;
        }

        updateInlineStatus(
            inlineStatusLabel,
            "Submitting " + quantity + " units of " + drink.getName() + " to " + branch + "...",
            STATUS_PENDING
        );

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return service.restockDrink(branch, drink.getId(), quantity);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        quantitySpinner.setValue(12);
                        updateInlineStatus(
                            inlineStatusLabel,
                            "Restock successful: " + drink.getName() + " transferred to " + branch + ".",
                            STATUS_OK
                        );
                        loadStockData(stockTableModel);
                        loadRestockHistoryTable(historyTableModel);
                        stockChartPanel.refreshData();
                    } else {
                        updateInlineStatus(
                            inlineStatusLabel,
                            "Restock failed. Check Headquarters stock and branch configuration, then try again.",
                            STATUS_ERROR
                        );
                    }
                } catch (Exception e) {
                    updateInlineStatus(inlineStatusLabel, "Restock error: " + e.getMessage(), STATUS_ERROR);
                }
            }
        }.execute();
    }

    private DefaultTableModel createReadOnlyTableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable createInventoryTable(DefaultTableModel tableModel) {
        JTable table = new JTable(tableModel);
        table.setBackground(CARD_BACKGROUND);
        table.setForeground(BODY_TEXT);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.setGridColor(new Color(65, 92, 122));
        table.setSelectionBackground(new Color(27, 76, 118));
        table.setSelectionForeground(HEADER_TEXT);
        table.getTableHeader().setBackground(new Color(62, 91, 123));
        table.getTableHeader().setForeground(HEADER_TEXT);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        return table;
    }

    private JScrollPane createTableTab(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(62, 91, 123), 1));
        scrollPane.getViewport().setBackground(CARD_BACKGROUND);
        return scrollPane;
    }

    private JPanel createInventorySectionTitle(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(HEADER_TEXT);

        JLabel subtitleLabel = new JLabel("<html><body style='width:270px;color:#97b1d1;font-family:Segoe UI,sans-serif;font-size:12px;'>" + subtitle + "</body></html>");
        subtitleLabel.setForeground(new Color(151, 177, 209));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(subtitleLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createInventoryField(String labelText, java.awt.Component inputComponent) {
        JPanel fieldPanel = new JPanel(new BorderLayout(0, 6));
        fieldPanel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(BODY_TEXT);

        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(inputComponent, BorderLayout.CENTER);
        return fieldPanel;
    }

    private void styleInventoryInput(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setBackground(new Color(28, 49, 78));
        comboBox.setForeground(BODY_TEXT);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 113, 149), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }

    private void styleInventorySpinner(JSpinner spinner) {
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spinner.getEditor().setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private JButton createWorkspaceButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setUI(new BasicButtonUI());
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.brighter(), 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
            )
        );
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JLabel createInlineStatusLabel(String message) {
        JLabel label = new JLabel(message);
        label.setOpaque(true);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        updateInlineStatus(label, message, STATUS_PENDING);
        return label;
    }

    private void updateInlineStatus(JLabel label, String message, Color statusColor) {
        label.setText(message);
        label.setForeground(HEADER_TEXT);
        if (STATUS_OK.equals(statusColor)) {
            label.setBackground(new Color(24, 84, 63));
            label.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(78, 171, 133), 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                )
            );
        } else if (STATUS_ERROR.equals(statusColor)) {
            label.setBackground(new Color(90, 47, 56));
            label.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(206, 117, 135), 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                )
            );
        } else {
            label.setBackground(new Color(79, 63, 35));
            label.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(197, 155, 87), 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                )
            );
        }
    }

    private void selectDrinkByName(JComboBox<Drink> drinkCombo, String drinkName) {
        if (drinkName == null) {
            return;
        }
        for (int i = 0; i < drinkCombo.getItemCount(); i++) {
            Drink drink = drinkCombo.getItemAt(i);
            if (drink != null && drink.getName().equalsIgnoreCase(drinkName)) {
                drinkCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private String toBranchCode(String branchLabel) {
        if (branchLabel == null) {
            return "";
        }
        if (branchLabel.toUpperCase().contains("NAIROBI")) {
            return "NAIROBI";
        }
        return branchLabel.trim().toUpperCase();
    }

    private void runReport(String loadingStatus, ReportBuilder reportBuilder) {
        if (!isConnected) {
            displayArea.setText(buildConnectionHelpHtml("Not connected to server."));
            return;
        }

        updateStatus(loadingStatus, STATUS_PENDING);
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return reportBuilder.build();
            }

            @Override
            protected void done() {
                try {
                    displayArea.setText(get());
                    displayArea.setCaretPosition(0);
                    updateStatus("Connected", STATUS_OK);
                } catch (Exception e) {
                    displayArea.setText(buildErrorHtml("Unable to load report", e.getMessage()));
                    updateStatus("Error loading data", STATUS_ERROR);
                }
            }
        }.execute();
    }

    private String buildDashboardReportHtml() throws Exception {
        List<ReportSection> sections = new ArrayList<>();
        sections.add(new ReportSection(
            "Customers",
            normalizeCurrencyList(service.getCustomers()),
            "No customers registered yet."
        ));
        sections.add(new ReportSection(
            "Orders by Location",
            prioritizeNairobi(normalizeCurrencyList(service.getBranchReport())),
            "No orders placed yet."
        ));
        sections.add(new ReportSection(
            "Revenue by Location (KSh)",
            prioritizeNairobi(normalizeCurrencyList(service.getRevenuePerBranch())),
            "No revenue data available."
        ));
        sections.add(new ReportSection(
            "Low Stock Alerts",
            normalizeCurrencyList(service.getLowStockAlerts()),
            "All products are above threshold."
        ));
        sections.add(new ReportSection(
            "Recent Restocks",
            limitItems(formatRestockHistoryItems(service.getRestockHistory()), 6),
            "No restock activity recorded yet."
        ));

        double totalRevenue = service.getTotalRevenue();
        return buildReportHtml(
            "System Dashboard",
            "Live overview of customers, orders, stock, and revenue (Headquarters pinned first)",
            sections,
            "Total Revenue (KSh)",
            "KSh " + MONEY_FORMAT.format(totalRevenue)
        );
    }

    private String buildWelcomeHtml() {
        List<ReportSection> sections = new ArrayList<>();
        List<String> introItems = new ArrayList<>();
        introItems.add("Use the left menu to open each report.");
        introItems.add("Revenue values are displayed in KSh.");
        introItems.add("Headquarters (Nairobi) is pinned first in location reports.");
        introItems.add("Dashboard refreshes every 30 seconds while connected.");
        sections.add(new ReportSection("Welcome", introItems, ""));

        return buildReportHtml(
            "Admin Dashboard",
            "Modern reporting workspace with Nairobi as headquarters",
            sections,
            null,
            null
        );
    }

    private String buildConnectionHelpHtml(String errorMessage) {
        List<ReportSection> sections = new ArrayList<>();
        List<String> troubleshooting = new ArrayList<>();
        troubleshooting.add("Confirm the server process is running.");
        troubleshooting.add("Confirm MySQL is running.");
        troubleshooting.add("Confirm port 1099 is available.");
        troubleshooting.add("Use 'Refresh Dashboard' to retry.");
        sections.add(new ReportSection("Troubleshooting", troubleshooting, ""));

        return buildReportHtml(
            "Connection Error",
            normalizeCurrencyText(errorMessage),
            sections,
            null,
            null
        );
    }

    private String buildErrorHtml(String title, String detail) {
        List<ReportSection> sections = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        errors.add(normalizeCurrencyText(detail));
        sections.add(new ReportSection("Error Details", errors, ""));
        return buildReportHtml(title, "The report could not be loaded.", sections, null, null);
    }

    private String buildReportHtml(
        String title,
        String subtitle,
        List<ReportSection> sections,
        String metricLabel,
        String metricValue
    ) {
        StringBuilder sb = new StringBuilder(4096);
        sb.append("<html><body style='margin:0;background:#0f213c;font-family:Segoe UI,Arial,sans-serif;color:#e6f1ff;'>");
        sb.append("<div style='padding:18px 20px 16px 20px;'>");
        sb.append("<div style='border:1px solid #436891;background:#173256;padding:14px 16px;margin-bottom:14px;'>");
        sb.append("<div style='font-size:21px;font-weight:700;color:#e8f3ff;'>").append(escapeHtml(title)).append("</div>");
        sb.append("<div style='font-size:13px;color:#9cc1e8;margin-top:4px;'>").append(escapeHtml(subtitle)).append("</div>");
        sb.append("</div>");

        if (metricLabel != null && metricValue != null) {
            sb.append("<table width='100%' cellspacing='0' cellpadding='0' style='border:1px solid #2f8f8f;margin-bottom:14px;background:#113a43;'>");
            sb.append("<tr>");
            sb.append("<td style='padding:14px 16px;'>");
            sb.append("<div style='font-size:13px;color:#8fcfd8;font-weight:700;'>").append(escapeHtml(metricLabel)).append("</div>");
            sb.append("<div style='font-size:26px;color:#63e5c4;font-weight:700;margin-top:4px;'>").append(escapeHtml(metricValue)).append("</div>");
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("</table>");
        }

        if (sections != null) {
            for (ReportSection section : sections) {
                appendSectionHtml(sb, section.title, section.items, section.emptyMessage);
            }
        }

        sb.append("</div>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private void appendSectionHtml(StringBuilder sb, String sectionTitle, List<String> items, String emptyMessage) {
        sb.append("<table width='100%' cellspacing='0' cellpadding='0' style='border:1px solid #3a648d;margin:0 0 14px 0;background:#122a48;'>");
        sb.append("<tr><td style='padding:10px 14px;background:#1e3f68;font-size:13px;font-weight:700;color:#e8f3ff;'>");
        sb.append(escapeHtml(sectionTitle));
        sb.append("</td></tr>");
        sb.append("<tr><td style='padding:10px 14px;'>");

        if (items == null || items.isEmpty()) {
            sb.append("<div style='font-size:13px;color:#9ab9de;padding:4px 0 6px 0;'>").append(escapeHtml(emptyMessage)).append("</div>");
        } else {
            sb.append("<table width='100%' cellspacing='0' cellpadding='0'>");
            int width = Math.max(2, String.valueOf(items.size()).length());
            for (int i = 0; i < items.size(); i++) {
                String number = String.format("%" + width + "d.", i + 1);
                sb.append("<tr>");
                sb.append("<td style='width:42px;padding:7px 0;border-bottom:1px solid #28466d;color:#8fb3dd;font-weight:700;vertical-align:top;'>");
                sb.append(escapeHtml(number));
                sb.append("</td>");
                sb.append("<td style='padding:7px 0;border-bottom:1px solid #28466d;color:#e5f2ff;font-size:14px;'>");
                sb.append(escapeHtml(normalizeCurrencyText(items.get(i))));
                sb.append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
        }

        sb.append("</td></tr>");
        sb.append("</table>");
    }

    private List<String> normalizeCurrencyList(List<String> items) {
        if (items == null) {
            return new ArrayList<>();
        }
        List<String> normalized = new ArrayList<>(items.size());
        for (String item : items) {
            normalized.add(normalizeCurrencyText(item));
        }
        return normalized;
    }

    private List<String> formatRestockHistoryItems(List<String> historyRows) {
        List<String> formatted = new ArrayList<>();
        if (historyRows == null) {
            return formatted;
        }

        for (String row : historyRows) {
            String[] parts = row.split("\t");
            if (parts.length >= 4) {
                formatted.add(parts[0] + " | " + parts[1] + " | " + parts[2] + " | +" + parts[3] + " units");
            } else {
                formatted.add(row);
            }
        }
        return formatted;
    }

    private List<String> limitItems(List<String> items, int maxItems) {
        List<String> limited = new ArrayList<>();
        if (items == null || maxItems <= 0) {
            return limited;
        }

        for (int i = 0; i < items.size() && i < maxItems; i++) {
            limited.add(items.get(i));
        }
        return limited;
    }

    private int findDrinkId(String drinkName) {
        try {
            for (Drink drink : service.getAvailableDrinks()) {
                if (drink.getName().equalsIgnoreCase(drinkName)) {
                    return drink.getId();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private List<String> prioritizeNairobi(List<String> items) {
        if (items == null) {
            return new ArrayList<>();
        }
        List<String> sorted = new ArrayList<>(items);
        Collections.sort(sorted, (left, right) -> {
            boolean leftIsMain = startsWithBranch(left, MAIN_BRANCH);
            boolean rightIsMain = startsWithBranch(right, MAIN_BRANCH);

            if (leftIsMain && !rightIsMain) {
                return -1;
            }
            if (!leftIsMain && rightIsMain) {
                return 1;
            }
            return left.compareToIgnoreCase(right);
        });
        return sorted;
    }

    private boolean startsWithBranch(String row, String branch) {
        if (row == null || branch == null) {
            return false;
        }
        String normalized = row.trim().toUpperCase();
        return normalized.startsWith(branch.toUpperCase())
            || normalized.startsWith("HEADQUARTERS")
            || normalized.contains("(NAIROBI)");
    }

    private String normalizeCurrencyText(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.replace("$", "KSh ");
        normalized = normalized.replaceAll("(?i)\\b(ksh|kes)\\b", "KSh");
        normalized = normalized.replaceAll("KSh\\s+", "KSh ");
        return normalized.trim();
    }

    private String escapeHtml(String raw) {
        if (raw == null) {
            return "";
        }
        String escaped = raw
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
        return escaped.replace("\n", "<br/>");
    }

    private String now() {
        return TIME_FORMAT.format(new Date());
    }

    private static class ReportSection {
        private final String title;
        private final List<String> items;
        private final String emptyMessage;

        private ReportSection(String title, List<String> items, String emptyMessage) {
            this.title = title;
            this.items = items;
            this.emptyMessage = emptyMessage;
        }
    }

    @FunctionalInterface
    private interface ReportBuilder {
        String build() throws Exception;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Look and feel setup failed: " + e.getMessage());
            }
            new AdminClient().setVisible(true);
        });
    }
}
