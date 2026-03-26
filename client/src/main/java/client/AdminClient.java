package client;

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
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class AdminClient extends JFrame {
    private static final Color APP_BACKGROUND = new Color(243, 246, 252);
    private static final Color CARD_BACKGROUND = new Color(255, 255, 255);
    private static final Color HEADER_TEXT = new Color(28, 45, 76);
    private static final Color BODY_TEXT = new Color(40, 51, 68);
    private static final Color STATUS_OK = new Color(25, 125, 71);
    private static final Color STATUS_PENDING = new Color(188, 130, 28);
    private static final Color STATUS_ERROR = new Color(186, 53, 53);
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private RemoteService service;
    private JTextArea displayArea;
    private JLabel statusLabel;
    private JButton customersButton;
    private JButton branchReportButton;
    private JButton revenueBranchButton;
    private JButton totalRevenueButton;
    private JButton lowStockButton;
    private JButton refreshButton;
    private JButton exitButton;
    private javax.swing.Timer autoRefreshTimer;
    private Timer reconnectTimer;
    private volatile boolean isConnected = false;

    public AdminClient() {
        initializeUI();
        connectToServer();
    }

    private void initializeUI() {
        setTitle("Distributed Drinks System - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 700));

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
        card.setLayout(new BorderLayout(8, 8));

        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(HEADER_TEXT);

        JLabel subtitleLabel = new JLabel("Operations and branch analytics");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(96, 108, 128));

        JPanel textWrap = new JPanel();
        textWrap.setOpaque(false);
        textWrap.setLayout(new BoxLayout(textWrap, BoxLayout.Y_AXIS));
        textWrap.add(titleLabel);
        textWrap.add(Box.createVerticalStrut(2));
        textWrap.add(subtitleLabel);

        statusLabel = new JLabel("Initializing...", SwingConstants.RIGHT);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(STATUS_PENDING);

        card.add(textWrap, BorderLayout.WEST);
        card.add(statusLabel, BorderLayout.EAST);
        return card;
    }

    private JPanel createSidebarPanel() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(230, 420));

        JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 10));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(6, 6, 6, 6));

        customersButton = createNavButton("Customers", new Color(36, 115, 196), this::showCustomers);
        branchReportButton = createNavButton("Branch Orders", new Color(49, 133, 186), this::showBranchReport);
        revenueBranchButton = createNavButton("Revenue Per Branch", new Color(57, 151, 164), this::showRevenuePerBranch);
        totalRevenueButton = createNavButton("Total Revenue", new Color(60, 168, 135), this::showTotalRevenue);
        lowStockButton = createNavButton("Low Stock Alerts", new Color(202, 94, 75), this::showLowStockAlerts);
        refreshButton = createNavButton("Refresh Dashboard", new Color(99, 108, 121), this::refreshConnectionOrDashboard);
        exitButton = createNavButton("Exit", new Color(77, 86, 98), () -> System.exit(0));

        buttons.add(customersButton);
        buttons.add(branchReportButton);
        buttons.add(revenueBranchButton);
        buttons.add(totalRevenueButton);
        buttons.add(lowStockButton);
        buttons.add(refreshButton);
        buttons.add(exitButton);

        card.add(buttons, BorderLayout.NORTH);
        return card;
    }

    private JPanel createContentPanel() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 10));

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setLineWrap(false);
        displayArea.setWrapStyleWord(false);
        displayArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        displayArea.setForeground(BODY_TEXT);
        displayArea.setMargin(new Insets(14, 14, 14, 14));
        displayArea.setText(buildWelcomeText());

        JScrollPane scrollPane = new JScrollPane(
            displayArea,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(215, 221, 232), 1));
        card.add(scrollPane, BorderLayout.CENTER);

        JLabel hint = new JLabel("Auto refresh: every 30 seconds while connected.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(new Color(108, 118, 136));
        card.add(hint, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createCard() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(217, 223, 234), 1),
                new EmptyBorder(12, 12, 12, 12)
            )
        );
        return panel;
    }

    private JButton createNavButton(String text, Color color, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.addActionListener(e -> action.run());
        return button;
    }

    private void connectToServer() {
        updateStatus("Connecting to server...", STATUS_PENDING);
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
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
            displayArea.setText(buildConnectionHelp(e.getMessage()));
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
        statusLabel.setText(status);
        statusLabel.setForeground(color);
    }

    private void enableButtons(boolean enabled) {
        customersButton.setEnabled(enabled);
        branchReportButton.setEnabled(enabled);
        revenueBranchButton.setEnabled(enabled);
        totalRevenueButton.setEnabled(enabled);
        lowStockButton.setEnabled(enabled);
        refreshButton.setEnabled(true);
        exitButton.setEnabled(true);
    }

    private void loadDashboard() {
        runReport("Refreshing dashboard...", this::buildDashboardReport);
    }

    private void showCustomers() {
        runReport("Loading customers...", () -> {
            List<String> customers = service.getCustomers();
            StringBuilder sb = new StringBuilder();
            appendHeader(sb, "Customer Registry");
            appendNumberedSection(sb, "Customers", customers, "No customers registered yet.");
            return sb.toString();
        });
    }

    private void showBranchReport() {
        runReport("Loading branch report...", () -> {
            List<String> reports = service.getBranchReport();
            StringBuilder sb = new StringBuilder();
            appendHeader(sb, "Orders Per Branch");
            appendNumberedSection(sb, "Branch Orders", reports, "No orders placed yet.");
            return sb.toString();
        });
    }

    private void showRevenuePerBranch() {
        runReport("Loading branch revenue...", () -> {
            List<String> revenues = service.getRevenuePerBranch();
            StringBuilder sb = new StringBuilder();
            appendHeader(sb, "Revenue Per Branch (KES)");
            appendNumberedSection(sb, "Branch Revenue", revenues, "No revenue data available.");
            return sb.toString();
        });
    }

    private void showTotalRevenue() {
        runReport("Loading total revenue...", () -> {
            double total = service.getTotalRevenue();
            StringBuilder sb = new StringBuilder();
            appendHeader(sb, "Total Revenue");
            sb.append("TOTAL REVENUE (KES)\n");
            sb.append("-------------------\n");
            sb.append(MONEY_FORMAT.format(total)).append("\n\n");
            sb.append("Status: ").append(total > 0 ? "Revenue recorded" : "Awaiting first order").append("\n");
            return sb.toString();
        });
    }

    private void showLowStockAlerts() {
        runReport("Loading stock alerts...", () -> {
            List<String> alerts = service.getLowStockAlerts();
            StringBuilder sb = new StringBuilder();
            appendHeader(sb, "Low Stock Alerts");
            appendNumberedSection(sb, "Inventory Alerts", alerts, "All products are above threshold.");
            return sb.toString();
        });
    }

    private void runReport(String loadingStatus, ReportBuilder reportBuilder) {
        if (!isConnected) {
            displayArea.setText(buildConnectionHelp("Not connected to server."));
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
                    updateStatus("Connected | Last refresh: " + now(), STATUS_OK);
                } catch (Exception e) {
                    displayArea.setText("Unable to load report.\n\n" + e.getMessage());
                    updateStatus("Error loading data", STATUS_ERROR);
                }
            }
        }.execute();
    }

    private String buildDashboardReport() throws Exception {
        List<String> customers = service.getCustomers();
        List<String> branchReports = service.getBranchReport();
        List<String> revenues = service.getRevenuePerBranch();
        double totalRevenue = service.getTotalRevenue();
        List<String> lowStockAlerts = service.getLowStockAlerts();

        StringBuilder sb = new StringBuilder();
        appendHeader(sb, "System Dashboard");
        sb.append("Updated: ").append(now()).append("\n\n");
        appendNumberedSection(sb, "Customers", customers, "No customers registered yet.");
        appendNumberedSection(sb, "Orders Per Branch", branchReports, "No orders placed yet.");
        appendNumberedSection(sb, "Revenue Per Branch (KES)", revenues, "No revenue data available.");

        sb.append("TOTAL REVENUE (KES)\n");
        sb.append("-------------------\n");
        sb.append(MONEY_FORMAT.format(totalRevenue)).append("\n\n");

        appendNumberedSection(
            sb,
            "Low Stock Alerts",
            lowStockAlerts,
            "All products are above threshold."
        );
        return sb.toString();
    }

    private void appendHeader(StringBuilder sb, String title) {
        sb.append(title.toUpperCase()).append("\n");
        sb.append("=".repeat(Math.max(28, title.length() + 8))).append("\n\n");
    }

    private void appendNumberedSection(
        StringBuilder sb,
        String sectionTitle,
        List<String> items,
        String emptyMessage
    ) {
        sb.append(sectionTitle.toUpperCase()).append("\n");
        sb.append("-".repeat(Math.max(20, sectionTitle.length() + 4))).append("\n");

        if (items == null || items.isEmpty()) {
            sb.append("  - ").append(emptyMessage).append("\n\n");
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            sb.append(String.format("%2d. %s%n", i + 1, items.get(i)));
        }
        sb.append("\n");
    }

    private String buildWelcomeText() {
        StringBuilder sb = new StringBuilder();
        appendHeader(sb, "Admin Dashboard");
        sb.append("Use the left menu to open reports:\n");
        sb.append("  - Customers\n");
        sb.append("  - Branch Orders\n");
        sb.append("  - Revenue Per Branch\n");
        sb.append("  - Total Revenue\n");
        sb.append("  - Low Stock Alerts\n\n");
        sb.append("The dashboard refreshes every 30 seconds when connected.\n");
        return sb.toString();
    }

    private String buildConnectionHelp(String errorMessage) {
        StringBuilder sb = new StringBuilder();
        appendHeader(sb, "Connection Error");
        sb.append("Could not connect to server at localhost:1099.\n\n");
        sb.append("Error Details:\n");
        sb.append("  ").append(errorMessage).append("\n\n");
        sb.append("Troubleshooting:\n");
        sb.append("  1. Confirm the server process is running.\n");
        sb.append("  2. Confirm MySQL is running.\n");
        sb.append("  3. Confirm port 1099 is available.\n");
        sb.append("  4. Use 'Refresh Dashboard' to retry.\n");
        return sb.toString();
    }

    private String now() {
        return TIME_FORMAT.format(new Date());
    }

    @FunctionalInterface
    private interface ReportBuilder {
        String build() throws Exception;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Look and feel setup failed: " + e.getMessage());
            }
            new AdminClient().setVisible(true);
        });
    }
}
