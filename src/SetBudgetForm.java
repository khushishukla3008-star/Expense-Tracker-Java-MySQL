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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
 * SetBudgetForm — monthly budget management.
 *
 * Stores a budget amount for a given month (YYYY-MM) per user in the
 * "budgets" table. The dashboard and the Add Expense screen read this table
 * to drive the Budget Alert System.
 *
 * @author preetam-06
 */
public class SetBudgetForm extends javax.swing.JFrame {

    private JPanel jSidebar;
    private UITheme.RoundedField fAmount;
    private JComboBox<String> cmbMonth;
    private UITheme.RoundedButton btnSave;
    private UITheme.RoundedButton btnClear;
    private UITheme.RoundedButton btnUpdate;
    private UITheme.RoundedButton btnDelete;
    private JLabel lblAlert;
    private JTable jTable;
    private DefaultTableModel model;

    public SetBudgetForm() {
        buildUI();
        setTitle("Set Budget - Expense Tracker");
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);
        loadMonths();
        loadBudgets();
        updateAlert();
    }

    /** Month keys (YYYY-MM) from the previous 11 months up to the current month. */
    private void loadMonths() {
        List<String> months = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate d = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            months.add(d.minusMonths(i).format(fmt));
        }
        cmbMonth.setModel(new javax.swing.DefaultComboBoxModel<>(months.toArray(new String[0])));
        cmbMonth.setSelectedItem(LocalDate.now().format(fmt));
    }

    private void buildUI() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jSidebar = UITheme.createSidebar(this, 5);

        JPanel content = new JPanel();
        content.setBackground(UITheme.CONTENT_BG);
        content.setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(24, 22, 0, 22));
        JLabel title = new JLabel("Set Budget");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UITheme.TEXT);
        JLabel sub = new JLabel("Set a monthly budget and watch your spending alerts");
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        sub.setForeground(UITheme.MUTED);
        header.add(title);
        header.add(sub);

        // ---- Form card ----
        UITheme.RoundedPanel formCard = new UITheme.RoundedPanel();
        formCard.setArc(16);
        formCard.setPreferredSize(new Dimension(340, 280));
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 20, 0, 20);

        c.gridy = 0;
        formCard.add(fieldLabel("Budget Amount (₹)"), c);
        c.gridy = 1;
        c.insets = new Insets(4, 20, 0, 20);
        fAmount = new UITheme.RoundedField();
        fAmount.setIconChar("₹");
        fAmount.addActionListener(e -> btnSave.doClick());
        formCard.add(fAmount, c);

        c.gridy = 2;
        c.insets = new Insets(12, 20, 0, 20);
        formCard.add(fieldLabel("Budget Month"), c);
        c.gridy = 3;
        c.insets = new Insets(4, 20, 0, 20);
        cmbMonth = new JComboBox<>();
        cmbMonth.setFont(UITheme.F_BODY);
        cmbMonth.setPreferredSize(new Dimension(300, 40));
        formCard.add(cmbMonth, c);

        c.gridy = 4;
        c.insets = new Insets(18, 20, 0, 20);
        btnSave = new UITheme.RoundedButton("Save Budget");
        btnSave.setBg(UITheme.TEAL);
        btnSave.setHoverColor(new Color(9, 125, 136));
        btnSave.addActionListener(e -> saveBudget());
        formCard.add(btnSave, c);

        c.gridy = 5;
        c.insets = new Insets(8, 20, 0, 20);
        btnClear = new UITheme.RoundedButton("Clear");
        btnClear.setBg(new Color(140, 145, 155));
        btnClear.setHoverColor(new Color(115, 122, 135));
        btnClear.addActionListener(e -> clearFields());
        formCard.add(btnClear, c);

        c.gridy = 6;
        c.insets = new Insets(8, 20, 4, 20);
        lblAlert = new JLabel(" ");
        lblAlert.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAlert.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        formCard.add(lblAlert, c);

        // ---- Table card ----
        UITheme.RoundedPanel tableCard = new UITheme.RoundedPanel();
        tableCard.setArc(16);
        model = new DefaultTableModel(
                new Object[]{"Budget ID", "Amount", "Month"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        jTable = new JTable(model);
        UITheme.applyTableStyle(jTable, 1);
        jTable.getSelectionModel().addListSelectionListener(evt -> fillFromRow());
        JScrollPane scroll = new JScrollPane(jTable);
        scroll.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        tableCard.setLayout(new java.awt.BorderLayout());
        JLabel tblTitle = new JLabel("Your Monthly Budgets");
        tblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tblTitle.setForeground(UITheme.TEXT);
        tblTitle.setBorder(BorderFactory.createEmptyBorder(16, 16, 4, 16));
        tableCard.add(tblTitle, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(0, 14, 10, 14));
        btnUpdate = new UITheme.RoundedButton("Update Budget");
        btnUpdate.setBg(UITheme.BLUE);
        btnUpdate.setHoverColor(UITheme.BLUE_DARK);
        btnUpdate.addActionListener(e -> updateBudget());
        btnDelete = new UITheme.RoundedButton("Delete");
        btnDelete.setBg(UITheme.RED);
        btnDelete.setHoverColor(new Color(180, 45, 45));
        btnDelete.addActionListener(e -> deleteBudget());
        actions.add(btnUpdate);
        actions.add(btnDelete);

        tableCard.add(scroll, BorderLayout.CENTER);
        tableCard.add(actions, BorderLayout.SOUTH);

        // ---- Assemble content ----
        JPanel cards = new JPanel();
        cards.setOpaque(false);
        cards.setLayout(new BoxLayout(cards, BoxLayout.X_AXIS));
        cards.setBorder(BorderFactory.createEmptyBorder(16, 22, 22, 22));
        cards.add(formCard);
        cards.add(Box.createHorizontalStrut(18));
        cards.add(tableCard);

        content.add(header, BorderLayout.NORTH);
        content.add(cards, BorderLayout.CENTER);

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

    /* ===================== DATA ===================== */

    private String currentMonth() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    private void loadBudgets() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT budget_id, budget_amount, budget_month FROM budgets "
                        + "WHERE user_id = ? ORDER BY budget_month DESC, budget_id DESC")) {
            pst.setInt(1, UserSession.userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("budget_id"),
                        rs.getDouble("budget_amount"),
                        rs.getString("budget_month")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Budget amount set for a given month (0 if none). */
    public double getBudgetForMonth(String month) {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT budget_amount FROM budgets "
                        + "WHERE user_id = ? AND budget_month = ? ORDER BY budget_id DESC LIMIT 1")) {
            pst.setInt(1, UserSession.userId);
            pst.setString(2, month);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("budget_amount");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Total spent in a given month (YYYY-MM). */
    public double getSpentForMonth(String month) {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT COALESCE(SUM(amount),0) FROM expenses "
                        + "WHERE user_id = ? AND LEFT(date, 7) = ?")) {
            pst.setInt(1, UserSession.userId);
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

    private void updateAlert() {
        String month = currentMonth();
        double budget = getBudgetForMonth(month);
        double spent = getSpentForMonth(month);
        double remaining = budget - spent;
        if (budget <= 0) {
            lblAlert.setText("No budget set for " + month);
            lblAlert.setForeground(UITheme.MUTED);
            return;
        }
        double pct = (spent / budget) * 100;
        String msg;
        Color col;
        if (spent > budget) {
            msg = "Exceeded! Over by ₹ " + String.format("%,.0f", spent - budget);
            col = UITheme.RED;
        } else if (pct >= 100) {
            msg = "Budget Limit Reached!";
            col = UITheme.RED;
        } else if (pct >= 80) {
            msg = String.format("Warning: %.0f%% of budget used", pct);
            col = UITheme.GOLD;
        } else {
            msg = String.format("Used %.0f%% · Remaining ₹ %,.0f", pct, remaining);
            col = UITheme.GREEN;
        }
        lblAlert.setText(msg);
        lblAlert.setForeground(col);
    }

    private void saveBudget() {
        double amount;
        try {
            amount = Double.parseDouble(fAmount.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.", "Set Budget",
                    JOptionPane.WARNING_MESSAGE);
            fAmount.requestFocusInWindow();
            return;
        }
        if (amount <= 0) {
            JOptionPane.showMessageDialog(this, "Budget must be greater than 0.", "Set Budget",
                    JOptionPane.WARNING_MESSAGE);
            fAmount.requestFocusInWindow();
            return;
        }
        String month = str(cmbMonth.getSelectedItem());

        try (Connection con = DBConnection.getConnection()) {
            String exists = "SELECT budget_id FROM budgets WHERE user_id = ? AND budget_month = ?";
            int existingId = -1;
            try (PreparedStatement p = con.prepareStatement(exists)) {
                p.setInt(1, UserSession.userId);
                p.setString(2, month);
                try (ResultSet rs = p.executeQuery()) {
                    if (rs.next()) {
                        existingId = rs.getInt("budget_id");
                    }
                }
            }
            if (existingId > 0) {
                try (PreparedStatement up = con.prepareStatement(
                        "UPDATE budgets SET budget_amount = ? "
                        + "WHERE budget_id = ? AND user_id = ?")) {
                    up.setDouble(1, amount);
                    up.setInt(2, existingId);
                    up.setInt(3, UserSession.userId);
                    up.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Budget updated for " + month + ".",
                        "Saved", JOptionPane.INFORMATION_MESSAGE);
            } else {
                try (PreparedStatement ins = con.prepareStatement(
                        "INSERT INTO budgets (user_id, budget_amount, budget_month) VALUES (?, ?, ?)")) {
                    ins.setInt(1, UserSession.userId);
                    ins.setDouble(2, amount);
                    ins.setString(3, month);
                    ins.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Budget saved for " + month + ".",
                        "Saved", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not save budget: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        clearFields();
        loadBudgets();
        updateAlert();
    }

    private void updateBudget() {
        int row = jTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a budget row to update.", "Update",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String month = str(jTable.getValueAt(row, 2));
        cmbMonth.setSelectedItem(month);
        fAmount.setText(jTable.getValueAt(row, 1).toString());
        JOptionPane.showMessageDialog(this,
                "Budget for " + month + " loaded. Change the amount and click Save Budget.",
                "Edit Mode", JOptionPane.INFORMATION_MESSAGE);
        fAmount.requestFocusInWindow();
    }

    private void deleteBudget() {
        int row = jTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a budget row to delete.", "Delete",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = Integer.parseInt(jTable.getValueAt(row, 0).toString());
        int opt = JOptionPane.showConfirmDialog(this, "Delete this budget?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opt != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "DELETE FROM budgets WHERE budget_id = ? AND user_id = ?")) {
            pst.setInt(1, id);
            pst.setInt(2, UserSession.userId);
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        clearFields();
        loadBudgets();
        updateAlert();
    }

    private void fillFromRow() {
        int row = jTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        fAmount.setText(jTable.getValueAt(row, 1).toString());
        cmbMonth.setSelectedItem(str(jTable.getValueAt(row, 2)));
    }

    private void clearFields() {
        fAmount.setText("");
        loadMonths();
        jTable.clearSelection();
        fAmount.requestFocusInWindow();
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new SetBudgetForm().setVisible(true));
    }
}