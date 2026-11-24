import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * IncomeUI — income tracking with add/delete, summary and sidebar navigation.
 *
 * @author preetam-06
 */
public class IncomeUI extends javax.swing.JFrame {

    private static final String[] SOURCES =
            {"Salary", "Business", "Allowance", "Gift", "Interest", "Other"};

    private JPanel jSidebar;
    private UITheme.RoundedField fAmount;
    private UITheme.RoundedField fDescription;
    private JTextField fDate;
    private JComboBox<String> cmbSource;
    private UITheme.RoundedButton btnAdd;
    private UITheme.RoundedButton btnDelete;
    private JScrollPane scroll;
    private JTableUI table;
    private DefaultTableModel model;
    private JLabel lblIncome;
    private JLabel lblExpenses;
    private JLabel lblSavings;

    public IncomeUI() {
        buildUI();
        setTitle("Income - Expense Tracker");
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);
        fAmount.addActionListener(e -> btnAdd.doClick());
        fDescription.addActionListener(e -> btnAdd.doClick());
        loadIncome();
        updateSummary();
        fAmount.requestFocusInWindow();
    }

    private void buildUI() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jSidebar = UITheme.createSidebar(this, 2);

        JPanel content = new JPanel();
        content.setBackground(UITheme.CONTENT_BG);
        content.setLayout(new BorderLayout());

        JPanel header = new JPanel(new java.awt.GridLayout(2, 1));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(24, 22, 0, 22));
        JLabel title = new JLabel("Income Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UITheme.TEXT);
        JLabel sub = new JLabel("Track your income sources");
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        sub.setForeground(UITheme.MUTED);
        header.add(title);
        header.add(sub);

        /* Form card */
        UITheme.RoundedPanel formCard = new UITheme.RoundedPanel();
        formCard.setArc(16);
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 20, 0, 20);

        c.gridy = 0;
        formCard.add(fieldLabel("Amount (₹)"), c);
        c.gridy = 1;
        c.insets = new Insets(4, 20, 0, 20);
        fAmount = new UITheme.RoundedField();
        fAmount.setIconChar("₹");
        formCard.add(fAmount, c);

        c.gridy = 2;
        c.insets = new Insets(12, 20, 0, 20);
        formCard.add(fieldLabel("Source"), c);
        c.gridy = 3;
        c.insets = new Insets(4, 20, 0, 20);
        cmbSource = new JComboBox<>(SOURCES);
        cmbSource.setFont(UITheme.F_BODY);
        cmbSource.setPreferredSize(new Dimension(300, 40));
        formCard.add(cmbSource, c);

        c.gridy = 4;
        c.insets = new Insets(12, 20, 0, 20);
        formCard.add(fieldLabel("Date (yyyy-MM-dd)"), c);
        c.gridy = 5;
        c.insets = new Insets(4, 20, 0, 20);
        fDate = new JTextField();
        fDate.setText(java.time.LocalDate.now().toString());
        styleField(fDate);
        formCard.add(fDate, c);

        c.gridy = 6;
        c.insets = new Insets(12, 20, 0, 20);
        formCard.add(fieldLabel("Description"), c);
        c.gridy = 7;
        c.insets = new Insets(4, 20, 0, 20);
        fDescription = new UITheme.RoundedField();
        fDescription.setIconChar("✓");
        formCard.add(fDescription, c);

        c.gridy = 8;
        c.insets = new Insets(18, 20, 0, 20);
        btnAdd = new UITheme.RoundedButton("Add Income");
        btnAdd.setBg(new Color(46, 160, 90));
        btnAdd.setHoverColor(new Color(28, 140, 76));
        btnAdd.addActionListener(e -> addIncome());
        formCard.add(btnAdd, c);

        c.gridy = 9;
        c.insets = new Insets(8, 20, 20, 20);
        btnDelete = new UITheme.RoundedButton("Delete Selected");
        btnDelete.setBg(UITheme.RED);
        btnDelete.setHoverColor(new Color(180, 45, 45));
        btnDelete.addActionListener(e -> deleteIncome());
        formCard.add(btnDelete, c);

        /* Summary strip */
        JPanel summary = new JPanel(new FlowLayout(FlowLayout.CENTER, 28, 10));
        summary.setOpaque(false);
        lblIncome = summaryLabel();
        lblExpenses = summaryLabel();
        lblSavings = summaryLabel();
        summary.add(lblIncome);
        summary.add(lblExpenses);
        summary.add(lblSavings);

        /* Table card */
        UITheme.RoundedPanel tableCard = new UITheme.RoundedPanel();
        tableCard.setArc(16);
        tableCard.setLayout(new BorderLayout());
        JLabel tblTitle = new JLabel("Income History");
        tblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tblTitle.setForeground(UITheme.TEXT);
        tblTitle.setBorder(BorderFactory.createEmptyBorder(14, 16, 6, 16));
        tableCard.add(tblTitle, BorderLayout.NORTH);
        model = new DefaultTableModel(
                new Object[]{"ID", "Amount", "Source", "Date", "Description"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTableUI(model);
        UITheme.applyTableStyle(table, 4);
        scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(4, 10, 10, 10));
        tableCard.add(scroll, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(tableCard, BorderLayout.CENTER);

        JPanel cards = new JPanel(new BorderLayout());
        cards.setOpaque(false);
        cards.setBorder(BorderFactory.createEmptyBorder(16, 22, 10, 22));
        cards.add(formCard, BorderLayout.WEST);
        cards.add(right, BorderLayout.CENTER);

        content.add(header, BorderLayout.NORTH);
        content.add(cards, BorderLayout.CENTER);
        content.add(summary, BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(jSidebar, BorderLayout.WEST);
        getContentPane().add(content, BorderLayout.CENTER);
        pack();
    }

    private static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(UITheme.TEXT);
        return l;
    }

    private static JLabel summaryLabel() {
        JLabel l = new JLabel("");
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(UITheme.TEXT);
        return l;
    }

    private static void styleField(JTextField f) {
        f.setFont(UITheme.F_BODY);
        f.setPreferredSize(new Dimension(300, 40));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 214, 230)),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
    }

    /* ===================== DATA ===================== */

    private static class JTableUI extends JTable {
        JTableUI(DefaultTableModel m) {
            super(m);
        }
    }

    private void loadIncome() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT id, amount, source, date, description "
                        + "FROM income WHERE user_id = ? ORDER BY date DESC, id DESC")) {
            pst.setInt(1, UserSession.userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getDouble("amount"),
                        rs.getString("source"),
                        rs.getString("date"),
                        rs.getString("description")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSummary() {
        double income = BudgetUtil.totalIncome(UserSession.userId);
        double expenses = BudgetUtil.totalExpenses(UserSession.userId);
        double savings = income - expenses;
        lblIncome.setText("Total Income: " + UITheme.rupee(income));
        lblIncome.setForeground(UITheme.GOLD);
        lblExpenses.setText("Total Expenses: " + UITheme.rupee(expenses));
        lblExpenses.setForeground(UITheme.RED);
        lblSavings.setText("Savings: " + UITheme.rupee(savings));
        lblSavings.setForeground(savings >= 0 ? UITheme.GREEN : UITheme.RED);
    }

    private boolean isValidAmount(String text) {
        try {
            return Double.parseDouble(text.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void addIncome() {
        if (fAmount.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an amount!",
                    "Income", JOptionPane.WARNING_MESSAGE);
            fAmount.requestFocusInWindow();
            return;
        }
        if (!isValidAmount(fAmount.getText())) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount greater than 0!",
                    "Income", JOptionPane.WARNING_MESSAGE);
            fAmount.requestFocusInWindow();
            return;
        }
        String source = str(cmbSource.getSelectedItem());
        String date = fDate.getText().trim();
        String description = fDescription.getText().trim();

        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO income (user_id, amount, source, date, description) "
                        + "VALUES (?, ?, ?, ?, ?)")) {
            pst.setInt(1, UserSession.userId);
            pst.setDouble(2, Double.parseDouble(fAmount.getText().trim()));
            pst.setString(3, source);
            pst.setString(4, date);
            pst.setString(5, description);
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to add income: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        fAmount.setText("");
        fDescription.setText("");
        cmbSource.setSelectedIndex(0);
        loadIncome();
        updateSummary();
        JOptionPane.showMessageDialog(this, "Income added successfully!",
                "Added", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteIncome() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an income row to delete.",
                    "Delete", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = Integer.parseInt(str(table.getValueAt(row, 0)));
        int opt = JOptionPane.showConfirmDialog(this,
                "Delete this income entry?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opt != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "DELETE FROM income WHERE id = ? AND user_id = ?")) {
            pst.setInt(1, id);
            pst.setInt(2, UserSession.userId);
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to delete: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        loadIncome();
        updateSummary();
        JOptionPane.showMessageDialog(this, "Income deleted.", "Deleted",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new IncomeUI().setVisible(true));
    }
}