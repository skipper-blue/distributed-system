package client;

import common.Drink;
import common.RemoteService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;

public class StockChartPanel extends JPanel {
    private static final Color PANEL_BACKGROUND = new Color(19, 38, 64);
    private static final Color BORDER_COLOR = new Color(62, 91, 123);
    private static final Color TITLE_COLOR = new Color(228, 239, 255);
    private static final Color SUBTITLE_COLOR = new Color(151, 177, 209);
    private static final Color BAR_COLOR = new Color(53, 192, 235);
    private static final Color GRID_COLOR = new Color(52, 78, 108);
    private static final String[] BRANCH_ORDER = {
        "HEADQUARTERS (NAIROBI)",
        "NAKURU",
        "MOMBASA",
        "KISUMU"
    };
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private final RemoteService service;
    private final DefaultCategoryDataset dataset;
    private final JLabel titleLabel;
    private final JLabel statusLabel;
    private final Timer refreshTimer;
    private Drink selectedDrink;
    private volatile boolean refreshInFlight;

    public StockChartPanel(RemoteService service) {
        this.service = service;
        this.dataset = new DefaultCategoryDataset();
        this.titleLabel = new JLabel("Live Inventory by Branch", SwingConstants.LEFT);
        this.statusLabel = new JLabel("Choose a drink to begin live tracking.", SwingConstants.RIGHT);
        this.refreshTimer = new Timer(5000, e -> refreshData());

        setLayout(new BorderLayout(0, 10));
        setBackground(PANEL_BACKGROUND);
        setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
            )
        );

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createChartPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TITLE_COLOR);

        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(SUBTITLE_COLOR);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(statusLabel, BorderLayout.EAST);
        return header;
    }

    private ChartPanel createChartPanel() {
        JFreeChart chart = ChartFactory.createBarChart(
            null,
            "Branch",
            "Units in Stock",
            dataset
        );

        chart.setBackgroundPaint(PANEL_BACKGROUND);
        chart.setPadding(new RectangleInsets(8, 10, 4, 10));
        TextTitle chartTitle = new TextTitle("", new Font("Segoe UI", Font.BOLD, 18));
        chartTitle.setPaint(TITLE_COLOR);
        chartTitle.setMargin(0, 0, 0, 0);
        chart.setTitle(chartTitle);
        chart.removeLegend();

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(PANEL_BACKGROUND);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setInsets(new RectangleInsets(10, 8, 10, 8));

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.BOLD, 12));
        domainAxis.setTickLabelPaint(TITLE_COLOR);
        domainAxis.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        domainAxis.setLabelPaint(SUBTITLE_COLOR);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        rangeAxis.setTickLabelPaint(TITLE_COLOR);
        rangeAxis.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        rangeAxis.setLabelPaint(SUBTITLE_COLOR);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, BAR_COLOR);
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.16);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        renderer.setDefaultItemLabelPaint(TITLE_COLOR);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setBackground(PANEL_BACKGROUND);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);
        return chartPanel;
    }

    public void setDrink(Drink drink) {
        selectedDrink = drink;
        if (drink == null) {
            titleLabel.setText("Live Inventory by Branch");
            statusLabel.setText("Choose a drink to begin live tracking.");
            dataset.clear();
            return;
        }

        titleLabel.setText("Live Inventory: " + drink.getName());
        refreshData();
    }

    public void startAutoRefresh() {
        if (!refreshTimer.isRunning()) {
            refreshTimer.start();
        }
    }

    public void stopAutoRefresh() {
        if (refreshTimer.isRunning()) {
            refreshTimer.stop();
        }
    }

    public void refreshData() {
        if (selectedDrink == null || refreshInFlight) {
            return;
        }

        refreshInFlight = true;
        statusLabel.setText("Refreshing " + selectedDrink.getName() + " inventory...");

        new SwingWorker<Map<String, Integer>, Void>() {
            @Override
            protected Map<String, Integer> doInBackground() throws Exception {
                return service.getStockByBranch(selectedDrink.getId());
            }

            @Override
            protected void done() {
                try {
                    Map<String, Integer> latestData = get();
                    populateDataset(latestData);
                    statusLabel.setText("Last synced at " + TIME_FORMAT.format(new Date()));
                } catch (Exception e) {
                    statusLabel.setText("Live refresh unavailable: " + e.getMessage());
                } finally {
                    refreshInFlight = false;
                }
            }
        }.execute();
    }

    private void populateDataset(Map<String, Integer> values) {
        dataset.clear();
        Map<String, Integer> orderedValues = new LinkedHashMap<>();
        for (String branch : BRANCH_ORDER) {
            orderedValues.put(branch, 0);
        }
        if (values != null) {
            orderedValues.putAll(values);
        }

        for (Map.Entry<String, Integer> entry : orderedValues.entrySet()) {
            dataset.addValue(entry.getValue(), "Stock", entry.getKey());
        }
    }
}
