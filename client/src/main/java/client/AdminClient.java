package client;

import common.RemoteService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class AdminClient extends JFrame {
    private static final Color APP_BACKGROUND = new Color(234, 241, 250);
    private static final Color CARD_BACKGROUND = new Color(255, 255, 255);
    private static final Color HEADER_TEXT = new Color(18, 40, 74);
    private static final Color BODY_TEXT = new Color(30, 43, 62);
    private static final Color STATUS_OK = new Color(14, 120, 72);
    private static final Color STATUS_PENDING = new Color(129, 83, 28);
    private static final Color STATUS_ERROR = new Color(179, 46, 46);
    private static final Color NAV_TEXT = new Color(31, 51, 82);
    private static final Color NAV_ACTIVE_TEXT = new Color(13, 54, 111);
    private static final Color NAV_ACTIVE_BG = new Color(216, 232, 252);
    private static final Color NAV_ACTIVE_BORDER = new Color(69, 126, 208);
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
        setTitle("Distributed Drinks System - Admin Dashboard");
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

        JLabel subtitleLabel = new JLabel("Operations and branch analytics");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(71, 89, 118));

        textWrap.add(titleLabel);
        textWrap.add(Box.createVerticalStrut(2));
        textWrap.add(subtitleLabel);

        statusLabel = new JLabel("Initializing...", SwingConstants.RIGHT);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLabel.setForeground(STATUS_PENDING);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(248, 238, 222));
        statusLabel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(228, 196, 140), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
            )
        );

        card.add(textWrap, BorderLayout.WEST);
        card.add(statusLabel, BorderLayout.EAST);
        return card;
    }

    private JPanel createSidebarPanel() {
        JPanel card = createCard();
        card.setPreferredSize(new Dimension(270, 470));
        card.setLayout(new BorderLayout(0, 12));

        JLabel menuTitle = new JLabel("Reports");
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        menuTitle.setForeground(new Color(52, 71, 102));
        menuTitle.setBorder(new EmptyBorder(0, 4, 2, 0));
        card.add(menuTitle, BorderLayout.NORTH);

        JPanel reportButtons = new JPanel();
        reportButtons.setOpaque(false);
        reportButtons.setLayout(new BoxLayout(reportButtons, BoxLayout.Y_AXIS));

        customersButton = createNavButton("Customers", new Color(230, 240, 252), new Color(156, 184, 219), this::showCustomers);
        branchReportButton = createNavButton("Branch Orders", new Color(231, 242, 252), new Color(156, 184, 219), this::showBranchReport);
        revenueBranchButton = createNavButton("Revenue Per Branch", new Color(231, 245, 246), new Color(150, 190, 192), this::showRevenuePerBranch);
        totalRevenueButton = createNavButton("Total Revenue", new Color(231, 246, 238), new Color(151, 194, 168), this::showTotalRevenue);
        lowStockButton = createNavButton("Low Stock Alerts", new Color(251, 237, 234), new Color(212, 160, 150), this::showLowStockAlerts);

        addNavButton(reportButtons, customersButton);
        addNavButton(reportButtons, branchReportButton);
        addNavButton(reportButtons, revenueBranchButton);
        addNavButton(reportButtons, totalRevenueButton);
        addNavButton(reportButtons, lowStockButton);
        reportButtons.add(Box.createVerticalGlue());

        JPanel utilityButtons = new JPanel();
        utilityButtons.setOpaque(false);
        utilityButtons.setLayout(new BoxLayout(utilityButtons, BoxLayout.Y_AXIS));

        refreshButton = createNavButton("Refresh Dashboard", new Color(238, 242, 248), new Color(168, 178, 194), this::refreshConnectionOrDashboard);
        exitButton = createNavButton("Exit", new Color(238, 242, 248), new Color(168, 178, 194), () -> System.exit(0));
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

        JLabel hint = new JLabel("Auto refresh: every 30 seconds while connected.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(new Color(79, 94, 117));
        card.add(hint, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createCard() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 221, 238), 1),
                new EmptyBorder(14, 14, 14, 14)
            )
        );
        return panel;
    }

    private JButton createNavButton(String text, Color background, Color borderColor, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(NAV_TEXT);
        button.setBackground(background);
        button.setFocusPainted(false);
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
            statusLabel.setBackground(new Color(229, 247, 236));
            statusLabel.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(137, 196, 166), 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                )
            );
        } else if (STATUS_ERROR.equals(color)) {
            statusLabel.setBackground(new Color(253, 235, 235));
            statusLabel.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(231, 166, 166), 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                )
            );
        } else {
            statusLabel.setBackground(new Color(248, 238, 222));
            statusLabel.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(228, 196, 140), 1),
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
        runReport("Loading branch report...", () -> {
            List<ReportSection> sections = new ArrayList<>();
            sections.add(new ReportSection(
                "Orders Per Branch",
                normalizeCurrencyList(service.getBranchReport()),
                "No orders placed yet."
            ));
            return buildReportHtml(
                "Branch Orders",
                "Order count and activity by branch",
                sections,
                null,
                null
            );
        });
    }

    private void showRevenuePerBranch() {
        setActiveNavButton(revenueBranchButton);
        runReport("Loading branch revenue...", () -> {
            List<ReportSection> sections = new ArrayList<>();
            sections.add(new ReportSection(
                "Revenue Per Branch (KSh)",
                normalizeCurrencyList(service.getRevenuePerBranch()),
                "No revenue data available."
            ));
            return buildReportHtml(
                "Revenue Per Branch (KSh)",
                "Revenue performance by location",
                sections,
                null,
                null
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
            "Orders Per Branch",
            normalizeCurrencyList(service.getBranchReport()),
            "No orders placed yet."
        ));
        sections.add(new ReportSection(
            "Revenue Per Branch (KSh)",
            normalizeCurrencyList(service.getRevenuePerBranch()),
            "No revenue data available."
        ));
        sections.add(new ReportSection(
            "Low Stock Alerts",
            normalizeCurrencyList(service.getLowStockAlerts()),
            "All products are above threshold."
        ));

        double totalRevenue = service.getTotalRevenue();
        return buildReportHtml(
            "System Dashboard",
            "Live overview of customers, orders, stock, and revenue",
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
        introItems.add("Dashboard refreshes every 30 seconds while connected.");
        sections.add(new ReportSection("Welcome", introItems, ""));

        return buildReportHtml(
            "Admin Dashboard",
            "Professional and aligned reporting workspace",
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
        sb.append("<html><body style='margin:0;background:#ffffff;font-family:Segoe UI,Arial,sans-serif;color:#1f2d3d;'>");
        sb.append("<div style='padding:18px 20px 16px 20px;'>");
        sb.append("<div style='border:1px solid #d4e0f1;background:#f5f9ff;padding:14px 16px;margin-bottom:14px;'>");
        sb.append("<div style='font-size:21px;font-weight:700;color:#163d73;'>").append(escapeHtml(title)).append("</div>");
        sb.append("<div style='font-size:13px;color:#5f7393;margin-top:4px;'>").append(escapeHtml(subtitle)).append("</div>");
        sb.append("</div>");

        if (metricLabel != null && metricValue != null) {
            sb.append("<table width='100%' cellspacing='0' cellpadding='0' style='border:1px solid #d4e8da;margin-bottom:14px;background:#f7fcf8;'>");
            sb.append("<tr>");
            sb.append("<td style='padding:14px 16px;'>");
            sb.append("<div style='font-size:13px;color:#385d6f;font-weight:700;'>").append(escapeHtml(metricLabel)).append("</div>");
            sb.append("<div style='font-size:26px;color:#0e7a4b;font-weight:700;margin-top:4px;'>").append(escapeHtml(metricValue)).append("</div>");
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
        sb.append("<table width='100%' cellspacing='0' cellpadding='0' style='border:1px solid #d7e2f2;margin:0 0 14px 0;'>");
        sb.append("<tr><td style='padding:10px 14px;background:#edf3fd;font-size:13px;font-weight:700;color:#1f4274;'>");
        sb.append(escapeHtml(sectionTitle));
        sb.append("</td></tr>");
        sb.append("<tr><td style='padding:10px 14px;'>");

        if (items == null || items.isEmpty()) {
            sb.append("<div style='font-size:13px;color:#5f7594;padding:4px 0 6px 0;'>").append(escapeHtml(emptyMessage)).append("</div>");
        } else {
            sb.append("<table width='100%' cellspacing='0' cellpadding='0'>");
            int width = Math.max(2, String.valueOf(items.size()).length());
            for (int i = 0; i < items.size(); i++) {
                String number = String.format("%" + width + "d.", i + 1);
                sb.append("<tr>");
                sb.append("<td style='width:42px;padding:7px 0;border-bottom:1px solid #edf2fb;color:#6b7b94;font-weight:700;vertical-align:top;'>");
                sb.append(escapeHtml(number));
                sb.append("</td>");
                sb.append("<td style='padding:7px 0;border-bottom:1px solid #edf2fb;color:#1f2d3d;font-size:14px;'>");
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
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Look and feel setup failed: " + e.getMessage());
            }
            new AdminClient().setVisible(true);
        });
    }
}
