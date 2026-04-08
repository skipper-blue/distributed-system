package client;

import common.Drink;
import common.Order;
import common.OrderItem;
import common.RemoteService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;

public class CustomerClient extends JFrame {
    private static final String SERVER_HOST = System.getProperty(
        "server.host",
        System.getenv().getOrDefault("DRINKS_SERVER_HOST", "localhost")
    );
    private static final Color APP_BACKGROUND = new Color(12, 21, 38);
    private static final Color SIDEBAR_BACKGROUND = new Color(20, 39, 64);
    private static final Color SURFACE_BACKGROUND = new Color(18, 34, 57);
    private static final Color CARD_BACKGROUND = new Color(25, 48, 76);
    private static final Color BORDER_COLOR = new Color(60, 90, 123);
    private static final Color TEXT_PRIMARY = new Color(236, 244, 255);
    private static final Color TEXT_SECONDARY = new Color(166, 190, 221);
    private static final Color ACTION_PRIMARY = new Color(15, 178, 167);
    private static final Color ACTION_MUTED = new Color(58, 91, 132);
    private static final Color BRANCH_ACTIVE_BG = new Color(17, 122, 190);
    private static final Color BRANCH_ACTIVE_BORDER = new Color(80, 201, 245);
    private static final Color INLINE_LINK_BG = new Color(35, 66, 104);
    private static final Color INLINE_LINK_ACTIVE_BG = new Color(20, 128, 186);
    private static final Color INLINE_LINK_BORDER = new Color(76, 111, 154);
    private static final Color INPUT_BACKGROUND = new Color(238, 245, 253);
    private static final Color INPUT_TEXT = new Color(21, 38, 61);
    private static final Color INPUT_BORDER = new Color(102, 139, 181);
    private static final String PRODUCT_IMAGE_BASE_DIR = "client/assets/images";
    private static final int CARD_IMAGE_WIDTH = 160;
    private static final int CARD_IMAGE_HEIGHT = 108;
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 27);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    private final String[] branches = {"NAKURU", "MOMBASA", "KISUMU"};
    private final List<OrderItem> currentItems = new ArrayList<>();
    private final List<JButton> branchLinkButtons = new ArrayList<>();

    private RemoteService service;
    private List<Drink> availableDrinks = new ArrayList<>();

    private JComboBox<String> branchCombo;
    private JTextField customerNameField;
    private JTextField searchField;
    private JComboBox<Drink> drinkCombo;
    private JSpinner quantitySpinner;
    private JButton addItemButton;
    private JPanel drinkCardsPanel;
    private DefaultTableModel orderTableModel;
    private JLabel totalItemsLabel;
    private JLabel activeBranchLabel;
    private JLabel cartBranchLabel;

    public CustomerClient() {
        connectToServer();
        initializeUI();
    }

    private void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry(SERVER_HOST, 1099);
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
        setTitle("Distributed Drinks System - Modern Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 760));

        JPanel root = new JPanel(new BorderLayout(12, 0));
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        root.setBackground(APP_BACKGROUND);
        setContentPane(root);

        root.add(createSidebarPanel(), BorderLayout.WEST);
        root.add(createMainPanel(), BorderLayout.CENTER);

        updateActiveBranchContext();
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 16));
        sidebar.setBackground(SIDEBAR_BACKGROUND);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
            )
        );

        JPanel brandBlock = new JPanel();
        brandBlock.setOpaque(false);
        brandBlock.setLayout(new BoxLayout(brandBlock, BoxLayout.Y_AXIS));

        JLabel appName = new JLabel("Distributed Drinks System");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        appName.setForeground(TEXT_PRIMARY);

        JLabel appTag = new JLabel("Customer Client");
        appTag.setFont(SUBTITLE_FONT);
        appTag.setForeground(TEXT_SECONDARY);

        brandBlock.add(appName);
        brandBlock.add(Box.createVerticalStrut(4));
        brandBlock.add(appTag);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));

        JLabel menuLabel = new JLabel("Shop Branches");
        menuLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        menuLabel.setForeground(TEXT_SECONDARY);
        menuLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 8, 0));
        nav.add(menuLabel);

        nav.add(createBranchLinkButton("NAKURU"));
        nav.add(Box.createVerticalStrut(8));
        nav.add(createBranchLinkButton("MOMBASA"));
        nav.add(Box.createVerticalStrut(8));
        nav.add(createBranchLinkButton("KISUMU"));
        nav.add(Box.createVerticalGlue());

        JLabel hint = new JLabel("Headquarters (Nairobi) is admin-only");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(TEXT_SECONDARY);
        hint.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 0));

        sidebar.add(brandBlock, BorderLayout.NORTH);
        sidebar.add(nav, BorderLayout.CENTER);
        sidebar.add(hint, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton createBranchLinkButton(String branch) {
        JButton button = new JButton(formatBranchName(branch));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setUI(new BasicButtonUI());
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBackground(SIDEBAR_BACKGROUND);
        button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        button.setOpaque(true);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setPreferredSize(new Dimension(170, 40));
        button.setAlignmentX(0.0f);
        button.putClientProperty("branch", branch);
        button.putClientProperty("linkStyle", "sidebar");
        button.addActionListener(e -> setSelectedBranch(branch));
        branchLinkButtons.add(button);
        return button;
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setOpaque(false);

        main.add(createTopBar(), BorderLayout.NORTH);

        JPanel contentRow = new JPanel(new BorderLayout(12, 0));
        contentRow.setOpaque(false);
        contentRow.add(createShopPanel(), BorderLayout.CENTER);
        contentRow.add(createCartPanel(), BorderLayout.EAST);

        main.add(contentRow, BorderLayout.CENTER);
        return main;
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(SURFACE_BACKGROUND);
        bar.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
            )
        );

        JLabel branchLabel = createTopLabel("Branch:");
        branchCombo = new JComboBox<>(branches);
        branchCombo.setSelectedItem("NAKURU");
        styleInput(branchCombo, new Dimension(180, 34));
        branchCombo.addActionListener(e -> updateActiveBranchContext());

        JLabel customerLabel = createTopLabel("Customer:");
        customerNameField = new JTextField();
        styleInput(customerNameField, new Dimension(200, 34));

        searchField = new JTextField();
        searchField.setToolTipText("Filter drinks by name");
        styleInput(searchField, new Dimension(260, 34));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyDrinkFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyDrinkFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyDrinkFilter();
            }
        });

        bar.add(branchLabel);
        bar.add(branchCombo);
        bar.add(customerLabel);
        bar.add(customerNameField);
        bar.add(createTopLabel("Search:"));
        bar.add(searchField);
        return bar;
    }

    private JLabel createTopLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    private JPanel createShopPanel() {
        JPanel card = createCard("Shop");
        card.setLayout(new BorderLayout(0, 12));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        activeBranchLabel = new JLabel("Shop - Nakuru");
        activeBranchLabel.setFont(TITLE_FONT);
        activeBranchLabel.setForeground(TEXT_PRIMARY);

        JLabel sub = new JLabel("Branch inventory and quick add tools");
        sub.setFont(SUBTITLE_FONT);
        sub.setForeground(TEXT_SECONDARY);

        header.add(activeBranchLabel);
        header.add(Box.createVerticalStrut(2));
        header.add(sub);
        header.add(Box.createVerticalStrut(8));
        header.add(createInlineBranchLinks());

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setOpaque(false);

        drinkCombo = new JComboBox<>(availableDrinks.toArray(new Drink[0]));
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        styleInput(drinkCombo, new Dimension(300, 34));
        styleInput(quantitySpinner, new Dimension(84, 34));

        addItemButton = createActionButton("Add to Cart", ACTION_PRIMARY);
        JButton clearButton = createActionButton("Clear Cart", ACTION_MUTED);
        addItemButton.addActionListener(e -> addItem());
        clearButton.addActionListener(e -> clearOrder());

        controls.add(createTopLabel("Drink:"));
        controls.add(drinkCombo);
        controls.add(createTopLabel("Qty:"));
        controls.add(quantitySpinner);
        controls.add(addItemButton);
        controls.add(clearButton);

        drinkCardsPanel = createDrinkCardsPanel();
        JScrollPane catalogScroll = new JScrollPane(drinkCardsPanel);
        catalogScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        catalogScroll.getViewport().setBackground(SURFACE_BACKGROUND);
        catalogScroll.getVerticalScrollBar().setUnitIncrement(12);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(controls, BorderLayout.NORTH);
        center.add(catalogScroll, BorderLayout.CENTER);

        card.add(header, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JPanel createInlineBranchLinks() {
        JPanel links = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        links.setOpaque(false);

        JLabel label = new JLabel("Order Links:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(189, 211, 238));
        links.add(label);

        links.add(createInlineBranchLinkButton("NAKURU"));
        links.add(createInlineBranchLinkButton("MOMBASA"));
        links.add(createInlineBranchLinkButton("KISUMU"));
        return links;
    }

    private JButton createInlineBranchLinkButton(String branch) {
        JButton button = new JButton(formatBranchName(branch));
        button.setUI(new BasicButtonUI());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setOpaque(true);
        button.setBackground(INLINE_LINK_BG);
        button.setBorder(BorderFactory.createLineBorder(INLINE_LINK_BORDER, 1));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(INLINE_LINK_BORDER, 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        button.putClientProperty("branch", branch);
        button.putClientProperty("linkStyle", "inline");
        button.addActionListener(e -> setSelectedBranch(branch));
        branchLinkButtons.add(button);
        return button;
    }

    private JPanel createDrinkCardsPanel() {
        JPanel wrap = new JPanel(new GridLayout(0, 2, 10, 10));
        wrap.setOpaque(true);
        wrap.setBackground(SURFACE_BACKGROUND);
        wrap.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedBranch = branchCombo == null ? "NAKURU" : (String) branchCombo.getSelectedItem();
        if (selectedBranch == null || selectedBranch.trim().isEmpty()) {
            selectedBranch = "NAKURU";
        }

        List<Drink> filteredDrinks = new ArrayList<>();
        for (Drink drink : availableDrinks) {
            if (query.isEmpty() || drink.getName().toLowerCase().contains(query)) {
                filteredDrinks.add(drink);
            }
        }

        if (filteredDrinks.isEmpty()) {
            JLabel empty = new JLabel("No drinks available.");
            empty.setFont(BODY_FONT);
            empty.setForeground(TEXT_SECONDARY);
            JPanel emptyCard = createMiniCard();
            emptyCard.add(empty, BorderLayout.CENTER);
            wrap.add(emptyCard);
            return wrap;
        }

        for (Drink drink : filteredDrinks) {
            JPanel drinkCard = createMiniCard();
            drinkCard.add(createImageLabel(selectedBranch, drink.getName()), BorderLayout.NORTH);

            JLabel name = new JLabel(drink.getName());
            name.setFont(new Font("Segoe UI", Font.BOLD, 15));
            name.setForeground(TEXT_PRIMARY);

            JLabel price = new JLabel("KSh " + String.format("%,.2f", drink.getPrice()));
            price.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            price.setForeground(TEXT_SECONDARY);

            JLabel hint = new JLabel("Select above and add to cart");
            hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            hint.setForeground(new Color(133, 170, 211));

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(name);
            text.add(Box.createVerticalStrut(2));
            text.add(price);
            text.add(Box.createVerticalStrut(4));
            text.add(hint);

            drinkCard.add(text, BorderLayout.CENTER);
            wrap.add(drinkCard);
        }

        return wrap;
    }

    private JLabel createImageLabel(String branch, String drinkName) {
        JLabel imageLabel = new JLabel("No Image", SwingConstants.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(22, 44, 70));
        imageLabel.setForeground(new Color(176, 203, 233));
        imageLabel.setPreferredSize(new Dimension(CARD_IMAGE_WIDTH, CARD_IMAGE_HEIGHT));
        imageLabel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(73, 117, 166), 1),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
            )
        );

        String imagePath = getProductImagePath(branch, drinkName);
        if (imagePath == null) {
            return imageLabel;
        }

        ImageIcon rawIcon = new ImageIcon(imagePath);
        Image scaled = rawIcon.getImage().getScaledInstance(
            CARD_IMAGE_WIDTH - 8,
            CARD_IMAGE_HEIGHT - 8,
            Image.SCALE_SMOOTH
        );
        imageLabel.setText("");
        imageLabel.setIcon(new ImageIcon(scaled));
        return imageLabel;
    }

    private String getProductImagePath(String branch, String drinkName) {
        String slug = toFileSlug(drinkName);
        String branchDir = branch.toUpperCase();

        String[] candidates = new String[] {
            PRODUCT_IMAGE_BASE_DIR + "/" + branchDir + "/" + slug + ".png",
            PRODUCT_IMAGE_BASE_DIR + "/" + branchDir + "/" + slug + ".jpg",
            PRODUCT_IMAGE_BASE_DIR + "/COMMON/" + slug + ".png",
            PRODUCT_IMAGE_BASE_DIR + "/COMMON/" + slug + ".jpg",
            "assets/images/" + branchDir + "/" + slug + ".png",
            "assets/images/" + branchDir + "/" + slug + ".jpg",
            "assets/images/COMMON/" + slug + ".png",
            "assets/images/COMMON/" + slug + ".jpg"
        };

        for (String candidate : candidates) {
            File file = new File(candidate);
            if (file.exists() && file.isFile()) {
                return file.getPath();
            }
        }
        return null;
    }

    private String toFileSlug(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }

    private JPanel createMiniCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(30, 60, 95));
        card.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(73, 117, 166), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            )
        );
        return card;
    }

    private JPanel createCartPanel() {
        JPanel card = createCard("Current Cart");
        card.setPreferredSize(new Dimension(360, 0));
        card.setLayout(new BorderLayout(0, 10));

        cartBranchLabel = new JLabel("Nakuru Branch Cart");
        cartBranchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cartBranchLabel.setForeground(TEXT_SECONDARY);

        orderTableModel = new DefaultTableModel(new Object[]{"Drink", "Qty"}, 0) {
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
        orderTable.setGridColor(new Color(59, 92, 129));
        orderTable.setBackground(SURFACE_BACKGROUND);
        orderTable.setForeground(TEXT_PRIMARY);
        orderTable.setSelectionBackground(new Color(17, 122, 190));
        orderTable.setSelectionForeground(TEXT_PRIMARY);
        orderTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        orderTable.getTableHeader().setBackground(new Color(38, 70, 108));
        orderTable.getTableHeader().setForeground(TEXT_PRIMARY);
        orderTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(SURFACE_BACKGROUND);

        totalItemsLabel = new JLabel("Total Items: 0", SwingConstants.RIGHT);
        totalItemsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalItemsLabel.setForeground(TEXT_PRIMARY);

        JButton placeOrderButton = createActionButton("Place Order", ACTION_PRIMARY);
        placeOrderButton.setPreferredSize(new Dimension(130, 36));
        placeOrderButton.addActionListener(e -> placeOrder());

        JButton clearButton = createActionButton("Clear Cart", ACTION_MUTED);
        clearButton.setPreferredSize(new Dimension(130, 36));
        clearButton.addActionListener(e -> clearOrder());

        JPanel totals = new JPanel(new BorderLayout(0, 10));
        totals.setOpaque(false);
        totals.add(totalItemsLabel, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(clearButton);
        buttons.add(placeOrderButton);
        totals.add(buttons, BorderLayout.SOUTH);

        card.add(cartBranchLabel, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(totals, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel();
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(),
                        title,
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        TEXT_PRIMARY
                    ),
                    BorderFactory.createEmptyBorder(8, 10, 10, 10)
                )
            )
        );
        return card;
    }

    private void styleInput(java.awt.Component component, Dimension size) {
        component.setFont(BODY_FONT);
        if (component instanceof JComponent) {
            JComponent jComponent = (JComponent) component;
            jComponent.setOpaque(true);
            jComponent.setBackground(INPUT_BACKGROUND);
            jComponent.setForeground(INPUT_TEXT);
            jComponent.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(INPUT_BORDER, 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                )
            );
            jComponent.setPreferredSize(size);
        }

        if (component instanceof JComboBox) {
            JComboBox<?> comboBox = (JComboBox<?>) component;
            comboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
                ) {
                    JLabel renderer = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    renderer.setFont(BODY_FONT);
                    if (isSelected) {
                        renderer.setBackground(new Color(34, 84, 142));
                        renderer.setForeground(Color.WHITE);
                    } else {
                        renderer.setBackground(INPUT_BACKGROUND);
                        renderer.setForeground(INPUT_TEXT);
                    }
                    return renderer;
                }
            });
        }

        if (component instanceof JSpinner) {
            JComponent editor = ((JSpinner) component).getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
                tf.setBackground(INPUT_BACKGROUND);
                tf.setForeground(INPUT_TEXT);
                tf.setCaretColor(INPUT_TEXT);
                tf.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            }
        }
    }

    private JButton createActionButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setUI(new BasicButtonUI());
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return button;
    }

    private void setSelectedBranch(String branch) {
        if (branchCombo != null) {
            branchCombo.setSelectedItem(branch);
        }
        updateActiveBranchContext();
    }

    private void updateActiveBranchContext() {
        if (branchCombo == null) {
            return;
        }

        String selected = (String) branchCombo.getSelectedItem();
        if (selected == null) {
            selected = "NAKURU";
        }

        String displayBranch = formatBranchName(selected);
        if (activeBranchLabel != null) {
            activeBranchLabel.setText("Shop - " + displayBranch);
        }
        if (cartBranchLabel != null) {
            cartBranchLabel.setText(displayBranch + " Branch Cart");
        }
        rebuildDrinkCards();

        for (JButton button : branchLinkButtons) {
            String buttonBranch = (String) button.getClientProperty("branch");
            String linkStyle = (String) button.getClientProperty("linkStyle");
            boolean active = selected.equalsIgnoreCase(buttonBranch);
            if (active) {
                if ("inline".equals(linkStyle)) {
                    button.setBackground(INLINE_LINK_ACTIVE_BG);
                    button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BRANCH_ACTIVE_BORDER, 1),
                        BorderFactory.createEmptyBorder(4, 10, 4, 10)
                    ));
                } else {
                    button.setBackground(BRANCH_ACTIVE_BG);
                    button.setBorder(BorderFactory.createLineBorder(BRANCH_ACTIVE_BORDER, 1));
                }
            } else {
                if ("inline".equals(linkStyle)) {
                    button.setBackground(INLINE_LINK_BG);
                    button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(INLINE_LINK_BORDER, 1),
                        BorderFactory.createEmptyBorder(4, 10, 4, 10)
                    ));
                } else {
                    button.setBackground(SIDEBAR_BACKGROUND);
                    button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
                }
            }
        }
    }

    private String formatBranchName(String branch) {
        if (branch == null || branch.isEmpty()) {
            return "Branch";
        }
        String lower = branch.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private void applyDrinkFilter() {
        if (drinkCombo == null || searchField == null) {
            return;
        }

        String query = searchField.getText().trim().toLowerCase();
        DefaultComboBoxModel<Drink> model = new DefaultComboBoxModel<>();
        for (Drink drink : availableDrinks) {
            if (query.isEmpty() || drink.getName().toLowerCase().contains(query)) {
                model.addElement(drink);
            }
        }

        drinkCombo.setModel(model);
        if (model.getSize() > 0) {
            drinkCombo.setSelectedIndex(0);
            addItemButton.setEnabled(true);
        } else {
            addItemButton.setEnabled(false);
        }

        rebuildDrinkCards();
    }

    private void rebuildDrinkCards() {
        if (drinkCardsPanel == null) {
            return;
        }
        JPanel refreshed = createDrinkCardsPanel();
        drinkCardsPanel.removeAll();
        java.awt.Component[] components = refreshed.getComponents();
        for (java.awt.Component component : components) {
            drinkCardsPanel.add(component);
        }
        drinkCardsPanel.revalidate();
        drinkCardsPanel.repaint();
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
            String normalizedResult = normalizeCurrencyText(result);
            JOptionPane.showMessageDialog(
                this,
                normalizedResult,
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

    private String normalizeCurrencyText(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.replace("$", "KSh ");
        normalized = normalized.replaceAll("(?i)\\b(kes|ksh)\\b", "KSh");
        normalized = normalized.replaceAll("KSh\\s+", "KSh ");
        return normalized.trim();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new CustomerClient().setVisible(true);
        });
    }
}
