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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Reports screen — Expense Summary Report.
 *
 * Shows category-wise expense totals plus Total Income / Total Expense /
 * Total Savings cards for three scopes:
 *   - All Expenses Report  (every expense ever recorded)
 *   - Monthly Report       (a single yyyy-MM, grouped via DATE_FORMAT(date,'%Y-%m'))
 *   - Annual Report        (a single year, grouped via YEAR(date))
 *
 * Savings = Total Income - Total Expenses.
 * Uses the same visual theme as the Dashboard (stat cards, rounded panels,
 * navy table headers). No anonymous inner classes — the screen is fully
 * contained in ReportsUI.class so it can never break from partial builds.
 *
 * @author preetam-06
 */
public class ReportsUI extends javax.swing.JFrame {

    private static final String[] CANONICAL_ORDER =
            {"Food", "Transport", "Shopping", "Bills", "Others"};

    private JPanel jSidebar;

    private JLabel lblScope;
    private JLabel lblTotalLine;

    private JLabel vIncome;
    private JLabel vExpense;
    private JLabel vSavings;

    private JComboBox<String> cmbMonth;
    private JComboBox<String> cmbYear;
    private UITheme.RoundedButton btnAll;
    private UITheme.RoundedButton btnMonthly;
    private UITheme.RoundedButton btnAnnual;

    private JTable jTable1;
    private DefaultTableModel model;

    private boolean monthlyMode;
    private boolean annualMode;

    public ReportsUI() {
        buildUI();
        setTitle("Reports - " + UserSession.username);
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);
        populateScopes();
        loadAll();
    }

    /* ===================== UI ===================== */

    private void buildUI() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jSidebar = UITheme.createSidebar(this, 7);

        JPanel content = new JPanel();
        content.setBackground(UITheme.CONTENT_BG);
        content.setLayout(new BorderLayout());

        /* Header */
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(22, 22, 2, 22));
        JPanel headerLeft = new JPanel(new GridLayout(2, 1));
        headerLeft.setOpaque(false);
        JLabel title = new JLabel("Expense Summary Report");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(UITheme.TEXT);
        JLabel sub = new JLabel("Category-wise expense breakdown with savings overview");
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        sub.setForeground(UITheme.MUTED);
        headerLeft.add(title);
        headerLeft.add(sub);
        lblScope = new JLabel("\u25cf  All Expenses");
        lblScope.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblScope.setForeground(UITheme.BLUE);
        header.add(headerLeft, BorderLayout.WEST);
        header.add(lblScope, BorderLayout.EAST);

        /* Scrollable content stack */
        JPanel stack = new JPanel();
        stack.setBackground(UITheme.CONTENT_BG);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setBorder(BorderFactory.createEmptyBorder(10, 22, 22, 22));

        JPanel cards = new JPanel(new GridLayout(1, 3, 15, 15));
        cards.setOpaque(false);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.add(makeStatCard("\u20b9", "Total Income", UITheme.GREEN));
        cards.add(makeStatCard("\u2715", "Total Expense", UITheme.RED));
        cards.add(makeStatCard("\u2605", "Total Savings", UITheme.PURPLE));

        stack.add(cards);
        stack.add(Box.createVerticalStrut(18));
        stack.add(buildFilterCard());
        stack.add(Box.createVerticalStrut(18));
        stack.add(buildCategoryCard());
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
    private JPanel makeStatCard(String glyph, String statName, Color accent) {
        UITheme.RoundedPanel card = new UITheme.RoundedPanel();
        card.setArc(15);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        UITheme.IconBadge badge = new UITheme.IconBadge(glyph, accent);
        badge.setPreferredSize(new Dimension(44, 44));

        JPanel txt = new JPanel();
        txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(statName);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(UITheme.MUTED);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel v = new JLabel(UITheme.rupee(0));
        v.setFont(new Font("Segoe UI", Font.BOLD, 24));
        v.setForeground(accent);
        v.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.add(t);
        txt.add(Box.createVerticalStrut(2));
        txt.add(v);

        switch (statName) {
            case "Total Income":
                vIncome = v;
                break;
            case "Total Expense":
                vExpense = v;
                break;
            case "Total Savings":
                vSavings = v;
                break;
            default:
                break;
        }
        card.add(badge, BorderLayout.WEST);
        card.add(txt, BorderLayout.CENTER);
        return card;
    }

    /** Filter card with the three report buttons and month/year selectors. */
    private JPanel buildFilterCard() {
        UITheme.RoundedPanel card = new UITheme.RoundedPanel();
        card.setArc(16);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 16, 16));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel cap = new JLabel("Report Filters");
        cap.setFont(new Font("Segoe UI", Font.BOLD, 15));
        cap.setForeground(UITheme.TEXT);
        JLabel hint = new JLabel("Totals recalculate automatically");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(UITheme.MUTED);
        titleRow.add(cap, BorderLayout.WEST);
        titleRow.add(hint, BorderLayout.EAST);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        row.setOpaque(false);

        btnAll = filterButton("All Expenses Report", UITheme.TEAL, new Color(9, 125, 136));
        btnAll.setPreferredSize(new Dimension(170, 38));
        btnAll.addActionListener(e -> loadAll());

        btnMonthly = filterButton("Monthly Report", UITheme.BLUE, UITheme.BLUE_DARK);
        btnMonthly.setPreferredSize(new Dimension(150, 38));
        btnMonthly.addActionListener(e -> loadMonthly());

        btnAnnual = filterButton("Annual Report", UITheme.GOLD, new Color(190, 132, 24));
        btnAnnual.setPreferredSize(new Dimension(145, 38));
        btnAnnual.addActionListener(e -> loadAnnual());

        row.add(btnAll);
        row.add(btnMonthly);
        row.add(btnAnnual);

        JLabel lM = new JLabel("Month:");
        lM.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lM.setForeground(UITheme.MUTED);
        cmbMonth = new JComboBox<>();
        cmbMonth.setFont(UITheme.F_BODY);
        cmbMonth.setPreferredSize(new Dimension(110, 36));
        cmbMonth.addActionListener(e -> {
            if (monthlyMode) {
                loadMonthly();
            }
        });
        row.add(lM);
        row.add(cmbMonth);

        JLabel lY = new JLabel("Year:");
        lY.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lY.setForeground(UITheme.MUTED);
        cmbYear = new JComboBox<>();
        cmbYear.setFont(UITheme.F_BODY);
        cmbYear.setPreferredSize(new Dimension(90, 36));
        cmbYear.addActionListener(e -> {
            if (annualMode) {
                loadAnnual();
            }
        });
        row.add(lY);
        row.add(cmbYear);

        card.add(titleRow, BorderLayout.NORTH);
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private UITheme.RoundedButton filterButton(String text, Color bg, Color hover) {
        UITheme.RoundedButton b = new UITheme.RoundedButton(text);
        b.setBg(bg);
        b.setHoverColor(hover);
        b.setArc(12);
        return b;
    }

    private void setActive(UITheme.RoundedButton active) {
        UITheme.RoundedButton[] buttons = {btnAll, btnMonthly, btnAnnual};
        Color[] colors = {UITheme.TEAL, UITheme.BLUE, UITheme.GOLD};
        Color[] hovers = {new Color(9, 125, 136), UITheme.BLUE_DARK, new Color(190, 132, 24)};
        Color grey = new Color(150, 156, 168);
        Color greyHover = new Color(130, 136, 150);
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] == active) {
                buttons[i].setBg(colors[i]);
                buttons[i].setHoverColor(hovers[i]);
            } else {
                buttons[i].setBg(grey);
                buttons[i].setHoverColor(greyHover);
            }
        }
    }

    /** Card with the Category / Total Amount table. */
    private JPanel buildCategoryCard() {
        UITheme.RoundedPanel card = new UITheme.RoundedPanel();
        card.setArc(16);
        card.setLayout(new BorderLayout());

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(BorderFactory.createEmptyBorder(12, 16, 4, 16));
        JLabel cap = new JLabel("Category Wise Expense Breakdown");
        cap.setFont(new Font("Segoe UI", Font.BOLD, 15));
        cap.setForeground(UITheme.TEXT);
        lblTotalLine = new JLabel("Total Expenses: " + UITheme.rupee(0));
        lblTotalLine.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalLine.setForeground(UITheme.RED);
        head.add(cap, BorderLayout.WEST);
        head.add(lblTotalLine, BorderLayout.EAST);

        model = new DefaultTableModel(new Object[]{"Category", "Total Amount"}, 0);
        jTable1 = new JTable(model);
        UITheme.applyTableStyle(jTable1, 0);
        jTable1.setRowHeight(34);
        JScrollPane scroll = new JScrollPane(jTable1);
        scroll.setBorder(BorderFactory.createEmptyBorder(2, 12, 12, 12));

        card.add(head, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    /* ===================== SCOPES ===================== */

    private void populateScopes() {
        Set<String> months = new LinkedHashSet<>();
        Set<String> years = new LinkedHashSet<>();
        String[] tables = {"expenses", "income"};
        try (Connection con = DBConnection.getConnection()) {
            for (String t : tables) {
                try (PreparedStatement p = con.prepareStatement(
                        "SELECT DISTINCT DATE_FORMAT(date, '%Y-%m') AS m, YEAR(date) AS y "
                        + "FROM " + t + " WHERE user_id = ?")) {
                    p.setInt(1, UserSession.userId);
                    try (ResultSet rs = p.executeQuery()) {
                        while (rs.next()) {
                            months.add(rs.getString("m"));
                            years.add(rs.getString("y"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> mList = new ArrayList<>(months);
        mList.sort((a, b) -> b.compareTo(a));
        if (mList.isEmpty()) {
            mList.add(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        for (String s : mList) {
            cmbMonth.addItem(s);
        }
        int mi = mList.indexOf(currentMonth);
        if (mi > 0) {
            cmbMonth.setSelectedIndex(mi);
        }

        List<String> yList = new ArrayList<>(years);
        yList.sort((a, b) -> b.compareTo(a));
        if (yList.isEmpty()) {
            yList.add(String.valueOf(LocalDate.now().getYear()));
        }
        String currentYear = String.valueOf(LocalDate.now().getYear());
        for (String s : yList) {
            cmbYear.addItem(s);
        }
        int yi = yList.indexOf(currentYear);
        if (yi > 0) {
            cmbYear.setSelectedIndex(yi);
        }
    }

    private void loadAll() {
        monthlyMode = false;
        annualMode = false;
        setActive(btnAll);
        lblScope.setText("\u25cf  All Expenses");
        cmbMonth.setEnabled(false);
        cmbYear.setEnabled(false);
        loadCategoryReport(null, null);
        double exp = expenseTotal(null, null);
        double inc = incomeTotal(null, null);
        updateCards(inc, exp);
    }

    private void loadMonthly() {
        monthlyMode = true;
        annualMode = false;
        setActive(btnMonthly);
        String month = (String) cmbMonth.getSelectedItem();
        lblScope.setText("\u25cf  Monthly Report  \u00b7 " + (month == null ? "" : month));
        cmbMonth.setEnabled(true);
        cmbYear.setEnabled(false);
        loadCategoryReport("m", month);
        double exp = expenseTotal("m", month);
        double inc = incomeTotal("m", month);
        updateCards(inc, exp);
    }

    private void loadAnnual() {
        monthlyMode = false;
        annualMode = true;
        setActive(btnAnnual);
        String year = (String) cmbYear.getSelectedItem();
        lblScope.setText("\u25cf  Annual Report  \u00b7 " + (year == null ? "" : year));
        cmbMonth.setEnabled(false);
        cmbYear.setEnabled(true);
        loadCategoryReport("y", year);
        double exp = expenseTotal("y", year);
        double inc = incomeTotal("y", year);
        updateCards(inc, exp);
    }

    /* ===================== DATA ===================== */

    private void loadCategoryReport(String kind, String value) {
        model.setRowCount(0);

        String sql = "SELECT category, COALESCE(SUM(amount),0) AS total "
                + "FROM expenses WHERE user_id = ?";
        if ("m".equals(kind)) {
            sql += " AND DATE_FORMAT(date, '%Y-%m') = ?";
        }
        if ("y".equals(kind)) {
            sql += " AND YEAR(date) = ?";
        }
        sql += " GROUP BY category";

        Map<String, Double> totals = new LinkedHashMap<>();
        double grand = 0;
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, UserSession.userId);
            if ("m".equals(kind)) {
                pst.setString(2, value);
            }
            if ("y".equals(kind)) {
                pst.setString(2, value);
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    double total = rs.getDouble("total");
                    totals.put(rs.getString("category"), total);
                    grand += total;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Could not load category report: " + e.getMessage(),
                    "Report Error", JOptionPane.ERROR_MESSAGE);
        }
        lblTotalLine.setText("Total Expenses: " + UITheme.rupee(grand));

        List<String> ordered = new ArrayList<>();
        for (String named : CANONICAL_ORDER) {
            ordered.add(named);
        }
        List<String> rest = new ArrayList<>(totals.keySet());
        rest.removeAll(ordered);
        rest.sort((x, y) -> Double.compare(
                totals.getOrDefault(y, 0.0), totals.getOrDefault(x, 0.0)));
        ordered.addAll(rest);

        for (String cat : ordered) {
            double amt = totals.getOrDefault(cat, 0.0);
            model.addRow(new Object[]{
                cat,
                (amt == 0 && !totals.containsKey(cat)) ? "\u2014" : UITheme.rupee(amt)
            });
        }
    }

    private double expenseTotal(String kind, String value) {
        return aggregate("expenses", "amount", kind, value);
    }

    private double incomeTotal(String kind, String value) {
        return aggregate("income", "amount", kind, value);
    }

    private double aggregate(String table, String col, String kind, String value) {
        String sql = "SELECT COALESCE(SUM(" + col + "),0) FROM " + table
                + " WHERE user_id = ?";
        if ("m".equals(kind)) {
            sql += " AND DATE_FORMAT(date, '%Y-%m') = ?";
        }
        if ("y".equals(kind)) {
            sql += " AND YEAR(date) = ?";
        }
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, UserSession.userId);
            if ("m".equals(kind)) {
                pst.setString(2, value);
            }
            if ("y".equals(kind)) {
                pst.setString(2, value);
            }
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

    private void updateCards(double income, double expense) {
        double savings = income - expense;
        vIncome.setText(UITheme.rupee(income));
        vExpense.setText(UITheme.rupee(expense));
        vSavings.setText(UITheme.rupee(savings));
        vSavings.setForeground(savings >= 0 ? UITheme.PURPLE : UITheme.RED);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            UserSession.userId = 1;
            UserSession.username = "admin";
            new ReportsUI().setVisible(true);
        });
    }
}