import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Dashboard — home screen after login.
 *
 * Modern responsive layout:
 *  - 6 equal stat cards (Total Income, Total Expenses, Current Budget,
 *    Remaining Budget, Savings, Budget Usage %) in a GridLayout(2,3,15,15)
 *  - Quick Actions row (+ Add Expense, + Add Income, Set Budget, View Reports)
 *  - Recent Transactions table (last 10, styled, search + double-click details)
 *  - Financial Overview panel (monthly income / expense / savings)
 *  - Budget Alert panel (budget / spent / remaining / usage with 4-zone bar)
 *  - Savings Tracker panel (income - expense + goal progress)
 *  - Expense Category Summary table
 *
 * Entire content scrolls so it works on smaller screens and when maximized.
 * No absolute positioning — only BorderLayout, GridLayout and BoxLayout.
 *
 * @author preetam-06
 */
public class DashboardUI extends javax.swing.JFrame {

    private static final Color DARK_RED = new Color(185, 40, 40);

    private JPanel jSidebar;

    private JLabel lblWelcome;
    private JLabel lblWelcomeSub;

    /* Summary cards */
    private JLabel vIncome;
    private JLabel vExpenses;
    private JLabel vBudget;
    private JLabel vRemaining;
    private JLabel vSavings;
    private JLabel vBudgetPct;
    private JProgressBar progressUsageCard;

    /* Financial overview */
    private JLabel finIncome;
    private JLabel finExpense;
    private JLabel finSavings;

    /* Budget alert */
    private JProgressBar progressBudget;
    private JLabel lblBudgetMsg;
    private JLabel lblBudgetHint;
    private JLabel lbBudgetVal;
    private JLabel lbSpentVal;
    private JLabel lbRemainVal;
    private JLabel lbUseVal;

    /* Savings tracker */
    private JProgressBar progressGoal;
    private JLabel lblGoalPct;
    private JLabel lblGoalAmount;
    private JLabel saveIncome;
    private JLabel saveExpense;
    private JLabel saveSavings;
    private UITheme.RoundedButton btnSetGoal;
    private UITheme.RoundedButton btnUpdateGoal;
    private UITheme.RoundedButton btnResetGoal;

    /* Recent transactions */
    private JTable recentTable;
    private DefaultTableModel recentModel;
    private JLabel lblTableTitle;
    private JLabel lblTxCount;
    private JTextField txtSearch;
    private UITheme.RoundedButton btnSearch;

    /* Category summary */
    private JTable categoryTable;
    private DefaultTableModel categoryModel;

    public DashboardUI() {
        buildUI();
        setTitle("Dashboard - " + UserSession.username);
        setMinimumSize(new Dimension(1200, 720));
        setLocationRelativeTo(null);
        loadDashboard();
        loadRecentExpenses();
        loadCategorySummary();
    }

    /* ===================== UI ===================== */

    private void buildUI() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jSidebar = UITheme.createSidebar(this, 0);

        JPanel content = new JPanel();
        content.setBackground(UITheme.CONTENT_BG);
        content.setLayout(new BorderLayout());

        /* ---- Header ---- */
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(22, 22, 2, 22));
        JPanel headerLeft = new JPanel(new GridLayout(2, 1));
        headerLeft.setOpaque(false);
        lblWelcome = new JLabel("Welcome, " + UserSession.username + "!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblWelcome.setForeground(UITheme.TEXT);
        lblWelcomeSub = new JLabel("Expense Tracker — here's your spending overview");
        lblWelcomeSub.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblWelcomeSub.setForeground(UITheme.MUTED);
        headerLeft.add(lblWelcome);
        headerLeft.add(lblWelcomeSub);
        JLabel lblPeriod = new JLabel("\u25cf  " + monthName());
        lblPeriod.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPeriod.setForeground(UITheme.BLUE);
        lblPeriod.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        header.add(headerLeft, BorderLayout.WEST);
        header.add(lblPeriod, BorderLayout.EAST);

        /* ---- Scrollable content stack ---- */
        JPanel stack = new JPanel();
        stack.setBackground(UITheme.CONTENT_BG);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setBorder(BorderFactory.createEmptyBorder(10, 22, 22, 22));

        /* 1) Summary cards — GridLayout(2,3,15,15) */
        JPanel cardsGrid = new JPanel(new GridLayout(2, 3, 15, 15));
        cardsGrid.setOpaque(false);
        cardsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsGrid.add(makeStatCard("\u20b9", "Total Income", UITheme.GREEN));
        cardsGrid.add(makeStatCard("\u2715", "Total Expenses", UITheme.RED));
        cardsGrid.add(makeStatCard("\u25ce", "Current Budget", UITheme.BLUE));
        cardsGrid.add(makeStatCard("\u2713", "Remaining Budget", UITheme.GOLD));
        cardsGrid.add(makeStatCard("\u2605", "Savings", UITheme.PURPLE));
        cardsGrid.add(makeStatCard("%", "Budget Usage", UITheme.TEAL));

        /* 2) Quick Actions */
        JPanel quickCard = buildQuickActions();

        /* 3) Main two-column row */
        JPanel mainRow = new JPanel(new GridLayout(1, 2, 15, 15));
        mainRow.setOpaque(false);
        mainRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel leftCol = new JPanel();
        leftCol.setOpaque(false);
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.add(buildRecentCard());
        leftCol.add(Box.createVerticalStrut(16));
        leftCol.add(buildCategoryCard());

        JPanel rightCol = new JPanel();
        rightCol.setOpaque(false);
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.add(buildFinancialCard());
        rightCol.add(Box.createVerticalStrut(16));
        rightCol.add(buildBudgetCard());
        rightCol.add(Box.createVerticalStrut(16));
        rightCol.add(buildSavingsCard());

        mainRow.add(leftCol);
        mainRow.add(rightCol);

        stack.add(cardsGrid);
        stack.add(Box.createVerticalStrut(18));
        stack.add(quickCard);
        stack.add(Box.createVerticalStrut(18));
        stack.add(mainRow);
        stack.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(stack);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        content.add(header, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(jSidebar, BorderLayout.WEST);
        getContentPane().add(content, BorderLayout.CENTER);
        pack();
    }

    /** White rounded stat card: icon badge + small title + large value. */
    private JPanel makeStatCard(String glyph, String title, Color accent) {
        UITheme.RoundedPanel card = new UITheme.RoundedPanel();
        card.setArc(15);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        UITheme.IconBadge badge = new UITheme.IconBadge(glyph, accent);
        badge.setPreferredSize(new Dimension(44, 44));

        JPanel txt = new JPanel();
        txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(UITheme.MUTED);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel v = new JLabel("\u20b9 0");
        v.setFont(new Font("Segoe UI", Font.BOLD, 24));
        v.setForeground(accent);
        v.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.add(t);
        txt.add(Box.createVerticalStrut(2));
        txt.add(v);

        switch (title) {
            case "Total Income": vIncome = v; break;
            case "Total Expenses": vExpenses = v; break;
            case "Current Budget": vBudget = v; break;
            case "Remaining Budget": vRemaining = v; break;
            case "Savings": vSavings = v; break;
            case "Budget Usage":
                vBudgetPct = v;
                txt.add(Box.createVerticalStrut(8));
                progressUsageCard = new JProgressBar(0, 1000);
                progressUsageCard.setPreferredSize(new Dimension(0, 8));
                progressUsageCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
                progressUsageCard.setBackground(new Color(235, 238, 245));
                txt.add(progressUsageCard);
                break;
            default: break;
        }
        card.add(badge, BorderLayout.WEST);
        card.add(txt, BorderLayout.CENTER);
        return card;
    }

    /** Quick Actions strip with modern coloured buttons. */
    private JPanel buildQuickActions() {
        UITheme.RoundedPanel card = new UITheme.RoundedPanel();
        card.setArc(16);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 16, 16));

        JLabel title = new JLabel("Quick Actions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(UITheme.TEXT);

        JPanel grid = new JPanel(new GridLayout(1, 4, 15, 0));
        grid.setOpaque(false);
        grid.add(quickButton("\uFF0B  Add Expense", UITheme.RED,
                new Color(180, 45, 45), 3));
        grid.add(quickButton("\u2191  Add Income", UITheme.GREEN,
                new Color(28, 140, 76), 2));
        grid.add(quickButton("\u20b9  Set Budget", UITheme.BLUE,
                UITheme.BLUE_DARK, 5));
        grid.add(quickButton("\u2630  View Reports", UITheme.PURPLE,
                new Color(115, 70, 180), 7));

        card.add(title, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private UITheme.RoundedButton quickButton(String text, Color bg, Color hover, int index) {
        UITheme.RoundedButton b = new UITheme.RoundedButton(text);
        b.setBg(bg);
        b.setHoverColor(hover);
        b.setPreferredSize(new Dimension(0, 46));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        b.setArc(14);
        b.addActionListener(e -> UITheme.openScreen(this, index));
        return b;
    }

    /* ================= PANELS ================= */

    /** Recent Transactions card with search and styled table. */
    private JPanel buildRecentCard() {
        UITheme.RoundedPanel card = new UITheme.RoundedPanel();
        card.setArc(16);
        card.setLayout(new BorderLayout());

        JPanel recentHead = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        recentHead.setOpaque(false);
        lblTableTitle = new JLabel("Recent Transactions");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTableTitle.setForeground(UITheme.TEXT);
        recentHead.add(lblTableTitle);
        lblTxCount = new JLabel("Last 10");
        lblTxCount.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTxCount.setForeground(UITheme.MUTED);
        recentHead.add(lblTxCount);
        recentHead.add(Box.createHorizontalStrut(10));
        JLabel s = new JLabel("Search:");
        s.setFont(new Font("Segoe UI", Font.BOLD, 12));
        s.setForeground(UITheme.MUTED);
        recentHead.add(s);
        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(160, 32));
        txtSearch.setFont(UITheme.F_BODY);
        txtSearch.addActionListener(e -> searchRecent());
        recentHead.add(txtSearch);
        btnSearch = new UITheme.RoundedButton("Go");
        btnSearch.setBg(UITheme.TEAL);
        btnSearch.setHoverColor(new Color(9, 125, 136));
        btnSearch.setPreferredSize(new Dimension(64, 32));
        btnSearch.setArc(12);
        btnSearch.addActionListener(e -> searchRecent());
        recentHead.add(btnSearch);

        recentModel = new DefaultTableModel(
                new Object[]{"ID", "Date", "Category", "Description", "Amount"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        recentTable = new JTable(recentModel);
        UITheme.applyTableStyle(recentTable, 3);
        recentTable.setRowHeight(38);
        recentTable.getTableHeader().setDefaultRenderer(new CenterHeaderRenderer());
        recentTable.setToolTipText("Double-click a transaction to view its details");
        recentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showRecentDetails();
                }
            }
        });
        JScrollPane recentScroll = new JScrollPane(recentTable);
        recentScroll.setBorder(BorderFactory.createEmptyBorder(2, 12, 12, 12));
        card.add(recentHead, BorderLayout.NORTH);
        card.add(recentScroll, BorderLayout.CENTER);
        return card;
    }

    /** Financial Overview: monthly income / expense / savings. */
    private JPanel buildFinancialCard() {
        UITheme.RoundedPanel card = new UITheme.RoundedPanel();
        card.setArc(16);
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel title = new JLabel("Financial Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(UITheme.TEXT);
        JLabel month = new JLabel(monthName());
        month.setFont(new Font("Segoe UI", Font.BOLD, 11));
        month.setForeground(UITheme.TEAL);
        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(month, BorderLayout.EAST);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        finIncome = moneyValue(UITheme.GREEN);
        finExpense = moneyValue(UITheme.RED);
        finSavings = moneyValue(UITheme.BLUE);
        body.add(moneyRow("Monthly Income", finIncome));
        body.add(Box.createVerticalStrut(10));
        body.add(moneyRow("Monthly Expense", finExpense));
        body.add(Box.createVerticalStrut(10));
        body.add(moneyRow("Monthly Savings", finSavings));

        card.add(titleRow, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    /** Budget Alert panel with 4 metric tiles + zone-coloured progress bar. */
    private JPanel buildBudgetCard() {
        UITheme.RoundedPanel card = new UITheme.RoundedPanel();
        card.setArc(16);
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Budget Alert");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(UITheme.TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(2, 16, 14, 16));

        lbBudgetVal = new JLabel("\u20b9 0", SwingConstants.CENTER);
        lbSpentVal = new JLabel("\u20b9 0", SwingConstants.CENTER);
        lbRemainVal = new JLabel("\u20b9 0", SwingConstants.CENTER);
        lbUseVal = new JLabel("0%", SwingConstants.CENTER);

        JPanel tiles = new JPanel(new GridLayout(2, 2, 10, 10));
        tiles.setOpaque(false);
        tiles.add(metricTile("Current Budget", lbBudgetVal));
        tiles.add(metricTile("Spent", lbSpentVal));
        tiles.add(metricTile("Remaining", lbRemainVal));
        tiles.add(metricTile("Usage", lbUseVal));

        progressBudget = new JProgressBar(0, 1000);
        progressBudget.setStringPainted(true);
        progressBudget.setPreferredSize(new Dimension(0, 24));
        progressBudget.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        progressBudget.setFont(new Font("Segoe UI", Font.BOLD, 11));

        lblBudgetMsg = new JLabel(" ");
        lblBudgetMsg.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblBudgetMsg.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblBudgetHint = new JLabel(" ");
        lblBudgetHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBudgetHint.setForeground(UITheme.MUTED);
        lblBudgetHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(tiles);
        body.add(Box.createVerticalStrut(12));
        body.add(progressBudget);
        body.add(Box.createVerticalStrut(8));
        body.add(lblBudgetMsg);
        body.add(Box.createVerticalStrut(2));
        body.add(lblBudgetHint);

        card.add(title, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    /** Savings Tracker: totals + goal progress + actions. */
    private JPanel buildSavingsCard() {
        UITheme.RoundedPanel card = new UITheme.RoundedPanel();
        card.setArc(16);
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Savings Tracker");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(UITheme.TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(2, 16, 14, 16));

        saveIncome = moneyValue(UITheme.GREEN);
        saveExpense = moneyValue(UITheme.RED);
        saveSavings = moneyValue(UITheme.BLUE);
        body.add(moneyRow("Total Income", saveIncome));
        body.add(Box.createVerticalStrut(8));
        body.add(moneyRow("Total Expense", saveExpense));
        body.add(Box.createVerticalStrut(8));
        body.add(moneyRow("Current Savings", saveSavings));
        body.add(Box.createVerticalStrut(12));

        JLabel goalCap = new JLabel("Savings Goal Progress");
        goalCap.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        goalCap.setForeground(UITheme.MUTED);
        goalCap.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblGoalAmount = new JLabel("No goal set");
        lblGoalAmount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGoalAmount.setForeground(UITheme.PURPLE);
        lblGoalAmount.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressGoal = new JProgressBar(0, 1000);
        progressGoal.setStringPainted(true);
        progressGoal.setPreferredSize(new Dimension(0, 22));
        progressGoal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        lblGoalPct = new JLabel(" ");
        lblGoalPct.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblGoalPct.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        buttons.setOpaque(false);
        btnSetGoal = new UITheme.RoundedButton("Set Goal");
        btnSetGoal.setBg(UITheme.TEAL);
        btnSetGoal.setHoverColor(new Color(9, 125, 136));
        btnSetGoal.setPreferredSize(new Dimension(92, 30));
        btnSetGoal.setArc(12);
        btnSetGoal.addActionListener(e -> setGoal());
        btnUpdateGoal = new UITheme.RoundedButton("Update");
        btnUpdateGoal.setBg(UITheme.BLUE);
        btnUpdateGoal.setHoverColor(UITheme.BLUE_DARK);
        btnUpdateGoal.setPreferredSize(new Dimension(92, 30));
        btnUpdateGoal.setArc(12);
        btnUpdateGoal.addActionListener(e -> updateGoal());
        btnResetGoal = new UITheme.RoundedButton("Reset");
        btnResetGoal.setBg(new Color(140, 145, 155));
        btnResetGoal.setHoverColor(new Color(115, 122, 135));
        btnResetGoal.setPreferredSize(new Dimension(84, 30));
        btnResetGoal.setArc(12);
        btnResetGoal.addActionListener(e -> resetGoal());
        buttons.add(btnSetGoal);
        buttons.add(btnUpdateGoal);
        buttons.add(btnResetGoal);

        body.add(goalCap);
        body.add(Box.createVerticalStrut(3));
        body.add(lblGoalAmount);
        body.add(Box.createVerticalStrut(6));
        body.add(progressGoal);
        body.add(Box.createVerticalStrut(3));
        body.add(lblGoalPct);
        body.add(Box.createVerticalStrut(6));
        body.add(buttons);

        card.add(title, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    /** Expense Category Summary table. */
    private JPanel buildCategoryCard() {
        UITheme.RoundedPanel card = new UITheme.RoundedPanel();
        card.setArc(16);
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Expense Category Summary");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(UITheme.TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));

        categoryModel = new DefaultTableModel(new Object[]{"Category", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        categoryTable = new JTable(categoryModel);
        UITheme.applyTableStyle(categoryTable);
        categoryTable.setRowHeight(34);
        categoryTable.getTableHeader().setDefaultRenderer(new CenterHeaderRenderer());
        JScrollPane catScroll = new JScrollPane(categoryTable);
        catScroll.setBorder(BorderFactory.createEmptyBorder(2, 12, 12, 12));
        card.add(title, BorderLayout.NORTH);
        card.add(catScroll, BorderLayout.CENTER);
        return card;
    }

    /* ================= SMALL HELPERS ================= */

    private static String monthName() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
    }

    private static JLabel moneyValue(Color color) {
        JLabel l = new JLabel("\u20b9 0");
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        l.setForeground(color);
        return l;
    }

    private static JPanel moneyRow(String caption, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        JLabel cap = new JLabel("\u25cf  " + caption);
        cap.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cap.setForeground(UITheme.TEXT);
        row.add(cap, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    /** Light metric tile used inside the Budget Alert panel. */
    private static JPanel metricTile(String caption, JLabel value) {
        UITheme.RoundedPanel tile = new UITheme.RoundedPanel();
        tile.setArc(12);
        tile.setFill(new Color(246, 249, 253));
        tile.setLineColor(new Color(226, 233, 244));
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JLabel cap = new JLabel(caption);
        cap.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cap.setForeground(UITheme.MUTED);
        cap.setAlignmentX(Component.CENTER_ALIGNMENT);
        value.setFont(new Font("Segoe UI", Font.BOLD, 16));
        value.setAlignmentX(Component.CENTER_ALIGNMENT);
        tile.add(cap);
        tile.add(Box.createVerticalStrut(2));
        tile.add(value);
        return tile;
    }

    /** Budget usage bar colour zones: 0-70 green, 70-90 orange, 90-100 red, >100 dark red. */
    private static Color zoneColor(double fraction) {
        if (fraction > 1.0) {
            return DARK_RED;
        }
        if (fraction >= 0.9) {
            return UITheme.RED;
        }
        if (fraction >= 0.7) {
            return UITheme.GOLD;
        }
        return UITheme.GREEN;
    }

    /** Sets a 0..1000 progress bar to the given fraction with the zone colour. */
    private static void setUsageBar(JProgressBar bar, double fraction, String label) {
        int value = fraction <= 0 ? 0 : (fraction >= 1 ? 1000 : (int) (fraction * 1000));
        bar.setValue(value);
        bar.setString(label);
        bar.setStringPainted(true);
        bar.setForeground(zoneColor(fraction));
        bar.setBackground(new Color(235, 238, 245));
    }

    /** Status text for the budget alert panel. */
    private static String zoneMessage(double spent, double budget) {
        if (budget <= 0) {
            return "No monthly budget set. Use Set Budget.";
        }
        double frac = spent / budget;
        if (frac > 1.0) {
            return "\u2716 Budget Exceeded";
        }
        if (frac >= 0.7) {
            return "\u26a0 Near Budget Limit";
        }
        return "\u2714 On Track";
    }

    private static Color zoneMessageColor(double spent, double budget) {
        if (budget <= 0) {
            return UITheme.MUTED;
        }
        double frac = spent / budget;
        if (frac > 1.0) {
            return DARK_RED;
        }
        if (frac >= 0.9) {
            return UITheme.RED;
        }
        if (frac >= 0.7) {
            return UITheme.GOLD;
        }
        return UITheme.GREEN;
    }

    /** Centred, navy header renderer for dashboard tables. */
    private static class CenterHeaderRenderer extends DefaultTableCellRenderer {

        CenterHeaderRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            comp.setBackground(UITheme.NAVY);
            return comp;
        }
    }

    /* ===================== DATA ===================== */

    private void loadDashboard() {
        int uid = UserSession.userId;
        String month = BudgetUtil.currentMonth();

        double income = BudgetUtil.totalIncome(uid);
        double expenses = BudgetUtil.totalExpenses(uid);
        double budget = BudgetUtil.budgetForMonth(uid, month);
        double spent = BudgetUtil.spentForMonth(uid, month);
        double remaining = budget - spent;
        double savings = income - expenses;
        double goal = latestGoal(uid);

        /* cards */
        vIncome.setText(UITheme.rupee(income));
        vExpenses.setText(UITheme.rupee(expenses));
        vBudget.setText(UITheme.rupee(budget));
        vRemaining.setText(UITheme.rupee(remaining));
        vRemaining.setForeground(remaining >= 0 ? UITheme.GOLD : UITheme.RED);
        vSavings.setText(UITheme.rupee(savings));
        vSavings.setForeground(savings >= 0 ? UITheme.PURPLE : UITheme.RED);
        if (budget <= 0) {
            vBudgetPct.setText("\u2014");
            progressUsageCard.setValue(0);
            progressUsageCard.setForeground(new Color(200, 206, 218));
        } else {
            double frac = spent / budget;
            vBudgetPct.setText(String.format("%.0f%%", frac * 100));
            setUsageBar(progressUsageCard, frac, "");
        }

        /* financial overview (monthly) */
        double mIncome = monthlyIncome(uid, month);
        double mExpense = BudgetUtil.spentForMonth(uid, month);
        double mSavings = mIncome - mExpense;
        finIncome.setText(UITheme.rupee(mIncome));
        finExpense.setText(UITheme.rupee(mExpense));
        finSavings.setText(UITheme.rupee(mSavings));

        /* savings tracker (all-time) */
        saveIncome.setText(UITheme.rupee(income));
        saveExpense.setText(UITheme.rupee(expenses));
        saveSavings.setText(UITheme.rupee(savings));

        /* budget alert */
        if (budget <= 0) {
            lbBudgetVal.setText("\u20b9 0");
            lbSpentVal.setText("\u20b9 0");
            lbRemainVal.setText("\u20b9 0");
            lbUseVal.setText("0%");
            progressBudget.setValue(0);
            progressBudget.setString("No budget");
            progressBudget.setForeground(new Color(200, 206, 218));
            lblBudgetMsg.setText(zoneMessage(spent, budget));
            lblBudgetMsg.setForeground(UITheme.MUTED);
            lblBudgetHint.setText(month + " · set a budget from the sidebar");
        } else {
            double frac = spent / budget;
            lbBudgetVal.setText(UITheme.rupee(budget));
            lbBudgetVal.setForeground(UITheme.BLUE);
            lbSpentVal.setText(UITheme.rupee(spent));
            lbSpentVal.setForeground(UITheme.RED);
            lbRemainVal.setText(UITheme.rupee(remaining));
            lbRemainVal.setForeground(remaining >= 0 ? UITheme.GREEN : DARK_RED);
            lbUseVal.setText(String.format("%.0f%%", frac * 100));
            lbUseVal.setForeground(zoneColor(frac));
            setUsageBar(progressBudget, frac,
                    String.format("%.0f%% used", frac * 100));
            lblBudgetMsg.setText(zoneMessage(spent, budget));
            lblBudgetMsg.setForeground(zoneMessageColor(spent, budget));
            lblBudgetHint.setText(month + " · " + UITheme.rupee(spent)
                    + " spent of " + UITheme.rupee(budget));
        }

        /* savings goal */
        lblGoalAmount.setText(goal > 0 ? "Goal: " + UITheme.rupee(goal) : "No goal set yet");
        if (goal > 0) {
            double frac = Math.max(0, Math.min(1.0, savings / goal));
            UITheme.styleProgress(progressGoal, frac,
                    String.format("%.0f%% Completed", frac * 100));
            lblGoalPct.setText("Current savings " + UITheme.rupee(savings)
                    + " of " + UITheme.rupee(goal));
        } else {
            UITheme.styleProgress(progressGoal, 0, "0%");
            lblGoalPct.setText("Set a savings goal to track progress.");
        }
    }

    private double latestGoal(int uid) {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT goal_amount FROM savings_goals "
                        + "WHERE user_id = ? ORDER BY goal_id DESC LIMIT 1")) {
            pst.setInt(1, uid);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("goal_amount");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private double monthlyIncome(int uid, String month) {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT COALESCE(SUM(amount),0) FROM income "
                        + "WHERE user_id = ? AND LEFT(date, 7) = ?")) {
            pst.setInt(1, uid);
            pst.setString(2, month);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void loadRecentExpenses() {
        lblTableTitle.setText("Recent Transactions");
        lblTxCount.setText("Last 10");
        recentModel.setRowCount(0);
        String sql = "SELECT id, date, category, description, amount FROM expenses "
                + "WHERE user_id = ? ORDER BY date DESC, id DESC LIMIT 10";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, UserSession.userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    recentModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("date"),
                        rs.getString("category"),
                        rs.getString("description"),
                        UITheme.rupee(rs.getDouble("amount"))
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchRecent() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) {
            loadRecentExpenses();
            return;
        }
        recentModel.setRowCount(0);
        String pat = "%" + query + "%";
        String sql = "SELECT id, date, category, description, amount FROM expenses "
                + "WHERE user_id = ? AND (description LIKE ? OR category LIKE ? OR date LIKE ?) "
                + "ORDER BY date DESC, id DESC LIMIT 50";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, UserSession.userId);
            pst.setString(2, pat);
            pst.setString(3, pat);
            pst.setString(4, pat);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    recentModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("date"),
                        rs.getString("category"),
                        rs.getString("description"),
                        UITheme.rupee(rs.getDouble("amount"))
                    });
                }
            }
            lblTableTitle.setText("Search Results");
            lblTxCount.setText("(" + recentModel.getRowCount() + " found)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showRecentDetails() {
        int row = recentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a transaction first.", "Details",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = Integer.parseInt(String.valueOf(recentTable.getValueAt(row, 0)));
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT id, date, category, description, amount, expense_type, payment_mode "
                        + "FROM expenses WHERE id = ? AND user_id = ?")) {
            pst.setInt(1, id);
            pst.setInt(2, UserSession.userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String msg = "Expense #" + rs.getInt("id")
                            + "\nDate: " + rs.getString("date")
                            + "\nCategory: " + rs.getString("category")
                            + "\nDescription: " + rs.getString("description")
                            + "\nAmount: " + UITheme.rupee(rs.getDouble("amount"))
                            + "\nExpense Type: " + rs.getString("expense_type")
                            + "\nPayment Mode: " + rs.getString("payment_mode");
                    JOptionPane.showMessageDialog(this, msg, "Transaction Details",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCategorySummary() {
        categoryModel.setRowCount(0);

        Map<String, Double> totals = new LinkedHashMap<>();
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT category, COALESCE(SUM(amount),0) AS total FROM expenses "
                        + "WHERE user_id = ? GROUP BY category")) {
            pst.setInt(1, UserSession.userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    totals.put(rs.getString("category"), rs.getDouble("total"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> ordered = new ArrayList<>();
        for (String named : new String[]{"Food", "Transport", "Shopping", "Bills", "Others"}) {
            ordered.add(named);
        }
        List<String> rest = new ArrayList<>(totals.keySet());
        rest.removeAll(ordered);
        rest.sort((x, y) -> Double.compare(totals.getOrDefault(y, 0.0), totals.getOrDefault(x, 0.0)));
        ordered.addAll(rest);

        for (String cat : ordered) {
            double amt = totals.getOrDefault(cat, 0.0);
            categoryModel.addRow(new Object[]{
                cat,
                amt == 0 && !totals.containsKey(cat) ? "\u2014" : UITheme.rupee(amt)
            });
        }
    }

    /* ===================== GOAL ACTIONS ===================== */

    private void setGoal() {
        String input = JOptionPane.showInputDialog(this, "Enter savings goal (\u20b9):");
        if (input == null) {
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(input.trim());
            if (amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid goal greater than 0.",
                    "Goal", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection con = DBConnection.getConnection()) {
            int any = 0;
            try (PreparedStatement p = con.prepareStatement(
                    "SELECT COUNT(*) FROM savings_goals WHERE user_id = ?")) {
                p.setInt(1, UserSession.userId);
                try (ResultSet rs = p.executeQuery()) {
                    if (rs.next()) {
                        any = rs.getInt(1);
                    }
                }
            }
            if (any > 0) {
                try (PreparedStatement p = con.prepareStatement(
                        "UPDATE savings_goals SET goal_amount = ?, created_date = CURDATE() "
                        + "WHERE user_id = ?")) {
                    p.setDouble(1, amount);
                    p.setInt(2, UserSession.userId);
                    p.executeUpdate();
                }
            } else {
                try (PreparedStatement p = con.prepareStatement(
                        "INSERT INTO savings_goals (user_id, goal_amount, created_date) "
                        + "VALUES (?, ?, CURDATE())")) {
                    p.setInt(1, UserSession.userId);
                    p.setDouble(2, amount);
                    p.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not save goal: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        loadDashboard();
        JOptionPane.showMessageDialog(this, "Savings goal saved: " + UITheme.rupee(amount),
                "Goal", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateGoal() {
        double goal = latestGoal(UserSession.userId);
        if (goal <= 0) {
            JOptionPane.showMessageDialog(this, "No goal exists yet. Set one first.", "Goal",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String input = JOptionPane.showInputDialog(this, "Update savings goal (\u20b9):",
                String.valueOf(goal));
        if (input == null) {
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(input.trim());
            if (amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid goal greater than 0.",
                    "Goal", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection con = DBConnection.getConnection();
                PreparedStatement p = con.prepareStatement(
                        "UPDATE savings_goals SET goal_amount = ?, created_date = CURDATE() "
                        + "WHERE user_id = ?")) {
            p.setDouble(1, amount);
            p.setInt(2, UserSession.userId);
            p.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        loadDashboard();
        JOptionPane.showMessageDialog(this, "Goal updated.", "Goal",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetGoal() {
        if (latestGoal(UserSession.userId) <= 0) {
            JOptionPane.showMessageDialog(this, "No goal to reset.", "Goal",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int opt = JOptionPane.showConfirmDialog(this, "Reset your savings goal?",
                "Reset Goal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opt != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection con = DBConnection.getConnection();
                PreparedStatement p = con.prepareStatement(
                        "DELETE FROM savings_goals WHERE user_id = ?")) {
            p.setInt(1, UserSession.userId);
            p.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        loadDashboard();
        JOptionPane.showMessageDialog(this, "Savings goal reset.", "Goal",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            UserSession.userId = 1;
            UserSession.username = "admin";
            new DashboardUI().setVisible(true);
        });
    }
}