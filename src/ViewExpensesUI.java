import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Expense History — the centre for viewing, searching, editing and deleting
 * expenses. Dashboard and sidebar cards navigate here.
 *
 * Filters: category, payment mode, expense type (incl. Annual + legacy Yearly),
 * keyword and date range.
 *
 * @author preetam-06
 */
public class ViewExpensesUI extends javax.swing.JFrame implements MouseListener {

    private static final String[] PAYMENT_MODES =
            {"Cash", "UPI", "Credit Card", "Debit Card", "Net Banking", "Wallet", "Cheque"};
    private static final String[] EXPENSE_TYPES = {"Daily", "Monthly", "Annual", "Yearly"};
    private static final String[] DEFAULT_CATEGORIES =
            {"Food", "Travel", "Shopping", "Bills", "Medicine", "Others"};

    private final String periodFilter;
    private final String filterTitle;

    private JPanel jSidebar;
    private JComboBox<String> cmbCategory;
    private JComboBox<String> cmbPayment;
    private JComboBox<String> cmbExpenseType;
    private JTextField jKeyword;
    private JTextField jFrom;
    private JTextField jTo;
    private JTable jTable1;
    private DefaultTableModel model;
    private UITheme.RoundedButton btnApply;
    private UITheme.RoundedButton btnReset;
    private UITheme.RoundedButton btnRefresh;
    private UITheme.RoundedButton btnEdit;
    private UITheme.RoundedButton btnDelete;

    public ViewExpensesUI() {
        this("", "");
    }

    public ViewExpensesUI(String whereClause, String titleSuffix) {
        this.periodFilter = (whereClause == null ? "" : whereClause);
        this.filterTitle = (titleSuffix == null ? "" : titleSuffix);
        buildUI();
        setTitle("Expense History - " + UserSession.username
                + (filterTitle.isEmpty() ? "" : " - " + filterTitle));
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);
        populateFilters();
        setupShortcuts();
        loadExpenses();
    }

    /* ===================== FILTERS ===================== */

    private void populateFilters() {
        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(withAll(getCategories())));
        cmbPayment.setModel(new javax.swing.DefaultComboBoxModel<>(withAll(PAYMENT_MODES)));
        cmbExpenseType.setModel(new javax.swing.DefaultComboBoxModel<>(withAll(EXPENSE_TYPES)));
    }

    private static String[] withAll(String[] items) {
        String[] out = new String[items.length + 1];
        out[0] = "All";
        System.arraycopy(items, 0, out, 1, items.length);
        return out;
    }

    private String[] getCategories() {
        List<String> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT category_name FROM categories ORDER BY category_name");
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("category_name");
                if (name != null) {
                    list.add(name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list.isEmpty() ? DEFAULT_CATEGORIES : list.toArray(new String[0]);
    }

    private void setupShortcuts() {
        jKeyword.addActionListener(e -> btnApply.doClick());
        jFrom.addActionListener(e -> btnApply.doClick());
        jTo.addActionListener(e -> btnApply.doClick());

        jTable1.addMouseListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            btnEdit.doClick();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    /* ===================== UI ===================== */

    private void buildUI() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jSidebar = UITheme.createSidebar(this, 4);

        JPanel content = new JPanel();
        content.setBackground(UITheme.CONTENT_BG);
        content.setLayout(new BorderLayout());

        JPanel header = new JPanel(new java.awt.GridLayout(2, 1));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(24, 22, 0, 22));
        JLabel title = new JLabel("Expense History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UITheme.TEXT);
        JLabel sub = new JLabel("Search, edit and delete your expenses");
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        sub.setForeground(UITheme.MUTED);
        header.add(title);
        header.add(sub);

        /* Filter bar */
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterBar.setOpaque(false);
        filterBar.setBorder(BorderFactory.createEmptyBorder(18, 10, 4, 10));
        filterBar.add(fieldLabel("Category", true));
        cmbCategory = new JComboBox<>();
        styleCombo(cmbCategory);
        filterBar.add(cmbCategory);
        filterBar.add(fieldLabel("Payment", true));
        cmbPayment = new JComboBox<>();
        styleCombo(cmbPayment);
        filterBar.add(cmbPayment);
        filterBar.add(fieldLabel("Type", true));
        cmbExpenseType = new JComboBox<>();
        styleCombo(cmbExpenseType);
        filterBar.add(cmbExpenseType);
        filterBar.add(fieldLabel("Keyword", true));
        jKeyword = new JTextField();
        styleField(jKeyword, 140);
        filterBar.add(jKeyword);

        JPanel filterBar2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterBar2.setOpaque(false);
        filterBar2.setBorder(BorderFactory.createEmptyBorder(4, 10, 8, 10));
        filterBar2.add(fieldLabel("From (yyyy-MM-dd)", true));
        jFrom = new JTextField();
        styleField(jFrom, 110);
        filterBar2.add(jFrom);
        filterBar2.add(fieldLabel("To (yyyy-MM-dd)", true));
        jTo = new JTextField();
        styleField(jTo, 110);
        filterBar2.add(jTo);
        filterBar2.add(javax.swing.Box.createHorizontalStrut(10));

        btnApply = new UITheme.RoundedButton("Apply Filters");
        btnApply.setBg(UITheme.TEAL);
        btnApply.setHoverColor(new Color(9, 125, 136));
        btnApply.setPreferredSize(new Dimension(130, 38));
        btnApply.addActionListener(e -> loadExpenses());
        filterBar2.add(btnApply);

        btnReset = new UITheme.RoundedButton("Reset");
        btnReset.setBg(new Color(140, 145, 155));
        btnReset.setHoverColor(new Color(115, 122, 135));
        btnReset.setPreferredSize(new Dimension(90, 38));
        btnReset.addActionListener(e -> resetFilters());
        filterBar2.add(btnReset);

        /* Table card */
        UITheme.RoundedPanel tableCard = new UITheme.RoundedPanel();
        tableCard.setArc(16);
        tableCard.setLayout(new BorderLayout());
        model = new DefaultTableModel(
                new Object[]{"ID", "Amount", "Category", "Description",
                    "Payment Mode", "Expense Type", "Date"}, 0);
        jTable1 = new JTable(model);
        UITheme.applyTableStyle(jTable1, 3);
        jTable1.setDefaultEditor(Object.class, null);
        jTable1.setToolTipText("Double-click a row to edit it");
        JScrollPane scroll = new JScrollPane(jTable1);
        scroll.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        tableCard.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(2, 14, 12, 14));
        btnRefresh = new UITheme.RoundedButton("Refresh");
        btnRefresh.setBg(UITheme.BLUE);
        btnRefresh.setHoverColor(UITheme.BLUE_DARK);
        btnRefresh.setPreferredSize(new Dimension(110, 38));
        btnRefresh.addActionListener(e -> loadExpenses());
        btnEdit = new UITheme.RoundedButton("Edit Expense");
        btnEdit.setBg(new Color(46, 160, 90));
        btnEdit.setHoverColor(new Color(28, 140, 76));
        btnEdit.setPreferredSize(new Dimension(130, 38));
        btnEdit.addActionListener(e -> editExpense());
        btnDelete = new UITheme.RoundedButton("Delete Expense");
        btnDelete.setBg(UITheme.RED);
        btnDelete.setHoverColor(new Color(180, 45, 45));
        btnDelete.setPreferredSize(new Dimension(130, 38));
        btnDelete.addActionListener(e -> deleteExpense());
        bottom.add(btnRefresh);
        bottom.add(btnEdit);
        bottom.add(btnDelete);
        tableCard.add(bottom, BorderLayout.SOUTH);

        JPanel midd = new JPanel(new BorderLayout());
        midd.setOpaque(false);
        midd.setBorder(BorderFactory.createEmptyBorder(10, 12, 22, 12));
        midd.add(filterBar, BorderLayout.NORTH);
        midd.add(tableCard, BorderLayout.CENTER);
        midd.add(filterBar2, BorderLayout.SOUTH);

        content.add(header, BorderLayout.NORTH);
        content.add(midd, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(jSidebar, BorderLayout.WEST);
        getContentPane().add(content, BorderLayout.CENTER);
        pack();
    }

    private static JLabel fieldLabel(String text, boolean topAlign) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(90, 105, 125));
        return l;
    }

    private static void styleCombo(JComboBox<String> c) {
        c.setFont(UITheme.F_BODY);
        c.setPreferredSize(new Dimension(130, 36));
    }

    private static void styleField(JTextField f, int w) {
        f.setFont(UITheme.F_BODY);
        f.setPreferredSize(new Dimension(w, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 214, 230)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }

    private void resetFilters() {
        if (cmbCategory.getItemCount() > 0) cmbCategory.setSelectedIndex(0);
        if (cmbPayment.getItemCount() > 0) cmbPayment.setSelectedIndex(0);
        if (cmbExpenseType.getItemCount() > 0) cmbExpenseType.setSelectedIndex(0);
        jKeyword.setText("");
        jFrom.setText("");
        jTo.setText("");
        loadExpenses();
    }

    /* ===================== DATA ===================== */

    private void loadExpenses() {
        StringBuilder sql = new StringBuilder(
                "SELECT id, amount, category, description, payment_mode, expense_type, date "
                + "FROM expenses WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(UserSession.userId);

        if (!periodFilter.isEmpty()) {
            sql.append(periodFilter);
        }

        String category = str(cmbCategory.getSelectedItem());
        if (!"All".equals(category)) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        String payment = str(cmbPayment.getSelectedItem());
        if (!"All".equals(payment)) {
            sql.append(" AND payment_mode = ?");
            params.add(payment);
        }
        String type = str(cmbExpenseType.getSelectedItem());
        if (!"All".equals(type)) {
            sql.append(" AND expense_type = ?");
            params.add(type);
        }
        String keyword = jKeyword.getText().trim();
        if (!keyword.isEmpty()) {
            sql.append(" AND (category LIKE ? OR description LIKE ? "
                    + "OR payment_mode LIKE ? OR expense_type LIKE ? OR date LIKE ?)");
            String pat = "%" + keyword + "%";
            for (int i = 0; i < 5; i++) {
                params.add(pat);
            }
        }
        String from = jFrom.getText().trim();
        if (!from.isEmpty()) {
            sql.append(" AND date >= ?");
            params.add(from);
        }
        String to = jTo.getText().trim();
        if (!to.isEmpty()) {
            sql.append(" AND date <= ?");
            params.add(to);
        }
        sql.append(" ORDER BY date DESC, id DESC");

        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pst.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getString("payment_mode"),
                        rs.getString("expense_type"),
                        rs.getString("date")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Could not load expenses: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteExpense() {
        int row = jTable1.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an expense row first.", "Delete",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = Integer.parseInt(str(jTable1.getValueAt(row, 0)));
        int opt = JOptionPane.showConfirmDialog(this,
                "Delete expense #" + id + "? This cannot be undone.", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opt != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "DELETE FROM expenses WHERE id = ? AND user_id = ?")) {
            pst.setInt(1, id);
            pst.setInt(2, UserSession.userId);
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not delete: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        loadExpenses();
        JOptionPane.showMessageDialog(this, "Expense deleted.", "Deleted",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void editExpense() {
        int row = jTable1.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an expense first.", "Edit",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = Integer.parseInt(str(jTable1.getValueAt(row, 0)));
        String currentAmount = str(jTable1.getValueAt(row, 1));
        String currentCategory = str(jTable1.getValueAt(row, 2));
        String currentDescription = str(jTable1.getValueAt(row, 3));
        String currentPaymentMode = str(jTable1.getValueAt(row, 4));
        String currentExpenseType = str(jTable1.getValueAt(row, 5)).isEmpty()
                ? "Daily" : str(jTable1.getValueAt(row, 5));

        String amount = JOptionPane.showInputDialog(this,
                "Enter New Amount (Rs.):", currentAmount);
        if (amount == null) return;
        try {
            if (Double.parseDouble(amount.trim()) <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid amount greater than 0!", "Edit",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String category = (String) JOptionPane.showInputDialog(this,
                "Select New Category:", currentCategory,
                JOptionPane.PLAIN_MESSAGE, null, getCategories(), currentCategory);
        if (category == null) return;

        String expenseType = (String) JOptionPane.showInputDialog(this,
                "Select New Expense Type:", currentExpenseType,
                JOptionPane.PLAIN_MESSAGE, null, EXPENSE_TYPES, currentExpenseType);
        if (expenseType == null) return;

        String paymentMode = (String) JOptionPane.showInputDialog(this,
                "Select New Payment Mode:", currentPaymentMode,
                JOptionPane.PLAIN_MESSAGE, null, PAYMENT_MODES, currentPaymentMode);
        if (paymentMode == null) return;

        String description = JOptionPane.showInputDialog(this,
                "Enter New Description:", currentDescription);
        if (description == null) return;

        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "UPDATE expenses SET amount=?, category=?, description=?, "
                        + "payment_mode=?, expense_type=? WHERE id=? AND user_id=?")) {
            pst.setDouble(1, Double.parseDouble(amount.trim()));
            pst.setString(2, category);
            pst.setString(3, description);
            pst.setString(4, paymentMode);
            pst.setString(5, expenseType);
            pst.setInt(6, id);
            pst.setInt(7, UserSession.userId);
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not update: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        loadExpenses();
        JOptionPane.showMessageDialog(this, "Expense updated.", "Updated",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new ViewExpensesUI().setVisible(true));
    }
}