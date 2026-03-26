package client;

import common.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class AdminClient extends JFrame {
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
    private boolean isConnected = false;

    public AdminClient() {
        initializeUI();
        connectToServer();
    }

    private void connectToServer() {
        try {
            updateStatus("Connecting to server...", Color.ORANGE);
            System.out.println("[CLIENT] Attempting to connect to RMI Registry on localhost:1099");
            
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            service = (RemoteService) registry.lookup("DrinkService");
            
            System.out.println("[CLIENT] ✓ Connected to server");
            updateStatus("✓ Connected to Server", Color.GREEN);
            isConnected = true;
            enableButtons(true);
            
            loadDashboard();
            
            // Start auto-refresh
            if (autoRefreshTimer == null) {
                autoRefreshTimer = new javax.swing.Timer(30000, e -> {
                    if (isConnected) loadDashboard();
                });
                autoRefreshTimer.start();
            }
            
        } catch (Exception e) {
            System.err.println("[CLIENT] ✗ Connection failed: " + e.getMessage());
            updateStatus("✗ Connection Failed - Retrying...", Color.RED);
            isConnected = false;
            enableButtons(false);
            
            displayArea.setText("CONNECTION ERROR\n\n" +
                "Could not connect to server on localhost:1099\n\n" +
                "Error: " + e.getMessage() + "\n\n" +
                "TROUBLESHOOTING:\n" +
                "1. Ensure server is running (check terminal for 'Server running')\n" +
                "2. Check if port 1099 is available\n" +
                "3. Verify MySQL is running (XAMPP Control Panel)\n" +
                "4. Click 'Refresh All' button to retry connection\n");
            
            // Auto-retry every 5 seconds
            new java.util.Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (!isConnected) {
                        SwingUtilities.invokeLater(() -> connectToServer());
                    }
                }
            }, 5000, 5000);
        }
    }

    private void initializeUI() {
        setTitle("Distributed Drinks System - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(new Color(245, 247, 250));
        
        // ===== TOP HEADER PANEL =====
        JPanel topPanel = new JPanel(new BorderLayout(15, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(25, 50, 120), 0, getHeight(), new Color(35, 75, 150));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("🏢 DISTRIBUTED DRINKS SYSTEM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("↳ Admin Dashboard & Analytics");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(220, 230, 255));
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 3));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        statusLabel = new JLabel("● Initializing...");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(Color.ORANGE);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        statusLabel.setOpaque(false);
        
        topPanel.add(titlePanel, BorderLayout.WEST);
        topPanel.add(statusLabel, BorderLayout.EAST);
        
        // ===== SIDEBAR WITH BUTTONS =====
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(250, 250, 252));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, new Color(200, 205, 215)));
        sidebar.setPreferredSize(new Dimension(200, 400));
        
        sidebar.add(Box.createVerticalStrut(10));
        
        Font buttonFont = new Font("Segoe UI", Font.PLAIN, 12);
        
        customersButton = createStyledButton("👥 Customers", buttonFont, new Color(70, 130, 200), e -> showCustomers());
        branchReportButton = createStyledButton("📊 Branch Orders", buttonFont, new Color(85, 145, 200), e -> showBranchReport());
        revenueBranchButton = createStyledButton("💰 Revenue/Branch", buttonFont, new Color(100, 160, 200), e -> showRevenuePerBranch());
        totalRevenueButton = createStyledButton("💵 Total Revenue", buttonFont, new Color(115, 175, 200), e -> showTotalRevenue());
        lowStockButton = createStyledButton("⚠️ Low Stock", buttonFont, new Color(210, 80, 80), e -> showLowStockAlerts());
        refreshButton = createStyledButton("🔄 Refresh Data", buttonFont, new Color(60, 150, 100), e -> loadDashboard());
        exitButton = createStyledButton("❌ Exit", buttonFont, new Color(100, 100, 100), e -> System.exit(0));

        sidebar.add(customersButton);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(branchReportButton);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(revenueBranchButton);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(totalRevenueButton);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(lowStockButton);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(refreshButton);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(exitButton);
        sidebar.add(Box.createVerticalGlue());

        // Add padding to sidebar buttons
        JPanel sidebarContainer = new JPanel(new BorderLayout());
        sidebarContainer.add(sidebar, BorderLayout.CENTER);
        sidebarContainer.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
        sidebarContainer.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
        sidebarContainer.setBackground(new Color(250, 250, 252));
        
        // ===== MAIN CONTENT AREA =====
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(255, 255, 255));
        mainPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(200, 205, 215)));

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        displayArea.setLineWrap(true);
        displayArea.setWrapStyleWord(true);
        displayArea.setForeground(new Color(40, 50, 70));
        displayArea.setBackground(new Color(255, 255, 255));
        displayArea.setMargin(new Insets(15, 15, 15, 15));
        displayArea.setText("📊 WELCOME TO ADMIN DASHBOARD\n" +
                           "═══════════════════════════════════════════════════════════════\n\n" +
                           "Click any button on the left sidebar to view reports:\n\n" +
                           "  👥 Customers    - View all registered customers\n" +
                           "  📊 Branch Orders - See order count per branch\n" +
                           "  💰 Revenue/Branch - Financial performance by location\n" +
                           "  💵 Total Revenue - Overall business metrics\n" +
                           "  ⚠️ Low Stock    - Watch products running low\n" +
                           "  🔄 Refresh     - Manually refresh data anytime\n\n" +
                           "→ Dashboard auto-updates every 30 seconds\n");
        
        JScrollPane scrollPane = new JScrollPane(displayArea,
                                                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(new Color(255, 255, 255));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(sidebarContainer, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(new ImageIcon().getImage());

        // Stop timer when closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (autoRefreshTimer != null) {
                    autoRefreshTimer.stop();
                }
                System.exit(0);
            }
        });
    }

    private JButton createStyledButton(String text, Font font, Color baseColor, ActionListener action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color fillColor = getModel().isPressed() ? new Color(
                    Math.max(0, baseColor.getRed() - 40),
                    Math.max(0, baseColor.getGreen() - 40),
                    Math.max(0, baseColor.getBlue() - 40)
                ) : getModel().isArmed() ? new Color(
                    Math.min(255, baseColor.getRed() + 30),
                    Math.min(255, baseColor.getGreen() + 30),
                    Math.min(255, baseColor.getBlue() + 30)
                ) : baseColor;
                
                g2.setColor(fillColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                g2.setColor(new Color(255, 255, 255, 80));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                
                super.paintComponent(g);
            }
        };
        
        btn.setFont(font);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.addActionListener(action);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 45));
        btn.setMargin(new Insets(8, 12, 8, 12));
        
        return btn;
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
        refreshButton.setEnabled(enabled);
    }

    private void loadDashboard() {
        if (!isConnected) return;
        
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                StringBuilder sb = new StringBuilder();
                sb.append("╔════════════════════════════════════════════════════════════════════════════════════════╗\n");
                sb.append("║                    📊 SYSTEM DASHBOARD - REAL TIME ANALYTICS                          ║\n");
                sb.append("╚════════════════════════════════════════════════════════════════════════════════════════╝\n\n");

                try {
                    // CUSTOMERS SECTION
                    List<String> customers = service.getCustomers();
                    sb.append("┌── 👥 CUSTOMERS ─────────────────────────────────────────────────────────────────────┐\n");
                    if (customers.isEmpty()) {
                        sb.append("│   No customers yet                                                                 │\n");
                    } else {
                        sb.append(String.format("│   Total: %d customer(s)                                                             │\n", customers.size()));
                        for (String customer : customers) {
                            sb.append(String.format("│   ✓ %s\n", customer));
                        }
                    }
                    sb.append("└──────────────────────────────────────────────────────────────────────────────────────┘\n\n");

                    // BRANCH ORDERS SECTION
                    List<String> branchReports = service.getBranchReport();
                    sb.append("┌── 📊 ORDERS PER BRANCH ─────────────────────────────────────────────────────────────┐\n");
                    if (branchReports.isEmpty()) {
                        sb.append("│   No orders yet                                                                    │\n");
                    } else {
                        for (String report : branchReports) {
                            sb.append(String.format("│   📍 %s\n", report));
                        }
                    }
                    sb.append("└──────────────────────────────────────────────────────────────────────────────────────┘\n\n");

                    // REVENUE BY BRANCH SECTION
                    List<String> revenues = service.getRevenuePerBranch();
                    sb.append("┌── 💰 REVENUE BY BRANCH (KES) ───────────────────────────────────────────────────────┐\n");
                    if (revenues.isEmpty()) {
                        sb.append("│   No revenue yet                                                                   │\n");
                    } else {
                        for (String revenue : revenues) {
                            sb.append(String.format("│   💳 %s\n", revenue));
                        }
                    }
                    sb.append("└──────────────────────────────────────────────────────────────────────────────────────┘\n\n");

                    // TOTAL REVENUE SECTION
                    double totalRevenue = service.getTotalRevenue();
                    sb.append("┌────────────────────────────────────────────────────────────────────────────────────┐\n");
                    sb.append(String.format("│  💵 TOTAL BUSINESS REVENUE:                    %-20s KES │\n", String.format("%.2f", totalRevenue)));
                    sb.append("└────────────────────────────────────────────────────────────────────────────────────┘\n\n");

                    // LOW STOCK ALERTS SECTION
                    List<String> alerts = service.getLowStockAlerts();
                    sb.append("┌── ⚠️ LOW STOCK ALERTS ──────────────────────────────────────────────────────────────┐\n");
                    if (alerts.isEmpty()) {
                        sb.append("│   ✅ All stock levels are healthy - No issues detected                          │\n");
                    } else {
                        sb.append("│   🚨 WARNING: The following items are below threshold (10 units):                │\n");
                        for (String alert : alerts) {
                            sb.append(String.format("│   ⚠️  %s\n", alert));
                        }
                    }
                    sb.append("└──────────────────────────────────────────────────────────────────────────────────────┘\n");

                } catch (Exception e) {
                    sb.append("❌ ERROR LOADING DATA: ").append(e.getMessage()).append("\n");
                    updateStatus("✗ Error loading data", Color.RED);
                }

                return sb.toString();
            }

            @Override
            protected void done() {
                try {
                    displayArea.setText(get());
                    displayArea.setCaretPosition(0);
                    updateStatus("✅ Connected  |  Last refresh: " + new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()), Color.GREEN);
                } catch (Exception e) {
                    displayArea.setText("❌ Error: " + e.getMessage());
                    updateStatus("✗ Error", Color.RED);
                }
            }
        }.execute();
    }

    private void showCustomers() {
        if (!isConnected) return;
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                StringBuilder sb = new StringBuilder();
                sb.append("╔═══════════════════════════════════════════════════════════════════════════════════╗\n");
                sb.append("║                         👥 CUSTOMER REGISTRY REPORT                               ║\n");
                sb.append("╚═══════════════════════════════════════════════════════════════════════════════════╝\n\n");
                sb.append("📋 ALL REGISTERED CUSTOMERS (Entire Network)\n\n");
                try {
                    List<String> customers = service.getCustomers();
                    if (customers.isEmpty()) {
                        sb.append("   No customers recorded yet.\n");
                    } else {
                        sb.append(String.format("   Total Customers: %d  │  Active  │  Status: ✓\n\n", customers.size()));
                        int count = 1;
                        for (String customer : customers) {
                            sb.append(String.format("   %-3d │ ✓ %s\n", count++, customer));
                        }
                    }
                } catch (Exception e) {
                    sb.append("❌ Error: ").append(e.getMessage());
                }
                return sb.toString();
            }
            @Override
            protected void done() {
                try {
                    displayArea.setText(get());
                    displayArea.setCaretPosition(0);
                } catch (Exception e) {}
            }
        }.execute();
    }

    private void showBranchReport() {
        if (!isConnected) return;
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                StringBuilder sb = new StringBuilder();
                sb.append("╔═══════════════════════════════════════════════════════════════════════════════════╗\n");
                sb.append("║                         📊 BRANCH ORDER PERFORMANCE REPORT                       ║\n");
                sb.append("╚═══════════════════════════════════════════════════════════════════════════════════╝\n\n");
                sb.append("📍 ORDERS BY LOCATION (All Branches)\n\n");
                try {
                    List<String> reports = service.getBranchReport();
                    if (reports.isEmpty()) {
                        sb.append("   No orders placed yet.\n");
                    } else {
                        sb.append("   Branch          │  Order Count  │  Status\n");
                        sb.append("   ────────────────┼───────────────┼──────────\n");
                        for (String report : reports) {
                            sb.append(String.format("   📌 %s\n", report));
                        }
                    }
                } catch (Exception e) {
                    sb.append("❌ Error: ").append(e.getMessage());
                }
                return sb.toString();
            }
            @Override
            protected void done() {
                try {
                    displayArea.setText(get());
                    displayArea.setCaretPosition(0);
                } catch (Exception e) {}
            }
        }.execute();
    }

    private void showRevenuePerBranch() {
        if (!isConnected) return;
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                StringBuilder sb = new StringBuilder();
                sb.append("╔═══════════════════════════════════════════════════════════════════════════════════╗\n");
                sb.append("║                      💰 REVENUE BY BRANCH ANALYSIS (KES)                          ║\n");
                sb.append("╚═══════════════════════════════════════════════════════════════════════════════════╝\n\n");
                sb.append("💳 FINANCIAL PERFORMANCE BY LOCATION\n\n");
                try {
                    List<String> revenues = service.getRevenuePerBranch();
                    if (revenues.isEmpty()) {
                        sb.append("   No revenue data available yet.\n");
                    } else {
                        sb.append("   Location              │  Revenue (KES)  │  Performance Status\n");
                        sb.append("   ─────────────────────┼─────────────────┼──────────────────────\n");
                        for (String revenue : revenues) {
                            sb.append(String.format("   💳 %s\n", revenue));
                        }
                    }
                } catch (Exception e) {
                    sb.append("❌ Error: ").append(e.getMessage());
                }
                return sb.toString();
            }
            @Override
            protected void done() {
                try {
                    displayArea.setText(get());
                    displayArea.setCaretPosition(0);
                } catch (Exception e) {}
            }
        }.execute();
    }

    private void showTotalRevenue() {
        if (!isConnected) return;
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                StringBuilder sb = new StringBuilder();
                sb.append("╔═══════════════════════════════════════════════════════════════════════════════════╗\n");
                sb.append("║                      💵 TOTAL BUSINESS REVENUE OVERVIEW                          ║\n");
                sb.append("╚═══════════════════════════════════════════════════════════════════════════════════╝\n\n");
                sb.append("📊 CONSOLIDATED BUSINESS METRICS\n\n");
                try {
                    double total = service.getTotalRevenue();
                    sb.append("   ╔════════════════════════════════════════════╗\n");
                    sb.append(String.format("   ║  💵 TOTAL REVENUE:   %20s KES  ║\n", String.format("%.2f", total)));
                    sb.append("   ║                                            ║\n");
                    sb.append("   ║  Status: ✅ Active Business                ║\n");
                    sb.append("   ║  Currency: Kenyan Shilling (KES)          ║\n");
                    sb.append("   ╚════════════════════════════════════════════╝\n\n");
                    sb.append(String.format("   💹 Grand Total Revenue: KES %.2f\n\n", total));
                    if (total > 0) {
                        sb.append("   ✅ Positive business performance detected\n");
                        sb.append("   📈 Revenue trend: ACTIVE\n");
                    } else {                     sb.append("   ⓘ Awaiting first order...\n");
                    }
                } catch (Exception e) {
                    sb.append("❌ Error: ").append(e.getMessage());
                }
                return sb.toString();
            }
            @Override
            protected void done() {
                try {
                    displayArea.setText(get());
                    displayArea.setCaretPosition(0);
                } catch (Exception e) {}
            }
        }.execute();
    }

    private void showLowStockAlerts() {
        if (!isConnected) return;
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                StringBuilder sb = new StringBuilder();
                sb.append("╔═══════════════════════════════════════════════════════════════════════════════════╗\n");
                sb.append("║                           ⚠️ LOW STOCK ALERTS DASHBOARD                           ║\n");
                sb.append("╚═══════════════════════════════════════════════════════════════════════════════════╝\n\n");
                sb.append("🚨 INVENTORY ALERT SYSTEM (Threshold: 10 Units)\n\n");
                try {
                    List<String> alerts = service.getLowStockAlerts();
                    if (alerts.isEmpty()) {
                        sb.append("   ╔════════════════════════════════════════════╗\n");
                        sb.append("   ║  ✅ ALL STOCK LEVELS HEALTHY               ║\n");
                        sb.append("   ║                                            ║\n");
                        sb.append("   ║  ✓ No items below threshold (10 units)    ║\n");
                        sb.append("   ║  ✓ All products adequately stocked        ║\n");
                        sb.append("   ║  ✓ No reordering needed at this time      ║\n");
                        sb.append("   ╚════════════════════════════════════════════╝\n");
                    } else {
                        sb.append("   ⚠️ CRITICAL ALERTS - ITEMS BELOW THRESHOLD\n\n");
                        sb.append("   Item             │  Branch         │  Stock  │  Action\n");
                        sb.append("   ─────────────────┼─────────────────┼─────────┼──────────────\n");
                        for (String alert : alerts) {
                            sb.append(String.format("   ⚠️  %s\n", alert));
                        }
                        sb.append("\n   🔔 ACTION REQUIRED: Please reorder items marked with ⚠️ immediately\n");
                    }
                } catch (Exception e) {
                    sb.append("❌ Error: ").append(e.getMessage());
                }
                return sb.toString();
            }
            @Override
            protected void done() {
                try {
                    displayArea.setText(get());
                    displayArea.setCaretPosition(0);
                } catch (Exception e) {}
            }
        }.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new AdminClient().setVisible(true);
        });
    }
}
