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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTextField;

/**
 * Add Expense — create-only expense entry screen.
 *
 * Records: Expense ID (auto), Description (Notes), Amount, Category,
 * Expense Type (Daily / Monthly / Annual) and Date. A Budget Alert panel
 * shows the current month's usage against the monthly budget with a
 * progress bar and an 80% / 100% / exceeded warning.
 *
 * Update, delete, search and expense management moved to Expense History.
 *
 * @author preetam-06
 */
public class ExpenseTrackerUI extends javax.swing.JFrame {

    private static final String[] EXPENSE_TYPES = {"Daily", "Monthly", "Annual"};
    private static final String[] DEFAULT_CATEGORIES =
            {"Food", "Travel", "Shopping", "Bills", "Others"};

    private JPanel jSidebar;
    private JTextField txtExpenseId;
    private UITheme.RoundedField fAmount;
    private UITheme.RoundedField txtDescription;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JComboBox<String> cmbExpenseType;
    private JSpinner spinnerDate;
    private UITheme.RoundedButton btnSave;
    private UITheme.RoundedButton btnClear;
    private JProgressBar progressAlert;
    private JLabel lblAlert;
    private JLabel lblSummary;

    public ExpenseTrackerUI() {
        buildUI();
        setTitle("Add Expense - Expense Tracker");
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);
        cmbCategory.setModel(new DefaultComboBoxModel<>(getCategories()));
        cmbExpenseType.setModel(new DefaultComboBoxModel<>(EXPENSE_TYPES));
        fAmount.addActionListener(e -> btnSave.doClick());
        txtDescription.addActionListener(e -> btnSave.doClick());
        fAmount.requestFocusInWindow();
        refreshBudgetPanel();
    }

    /* ===================== CATEGORIES ===================== */

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
        if (!list.contains("Others")) {
            list.add("Others");
        }
        return list.isEmpty() ? DEFAULT_CATEGORIES : list.toArray(new String[0]);
    }

    /* ===================== UI ===================== */

    private void buildUI() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jSidebar = UITheme.createSidebar(this, 3);

        JPanel content = new JPanel();
        content.setBackground(UITheme.CONTENT_BG);
        content.setLayout(new BorderLayout());

        JPanel header = new JPanel(new java.awt.GridLayout(2, 1));
        header.setOpaque(false);
        header.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 22, 0, 22));
        JLabel title = new JLabel("Add Expense");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UITheme.TEXT);
        JLabel sub = new JLabel("Record a new expense");
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        sub.setForeground(UITheme.MUTED);
        header.add(title);
        header.add(sub);

        /* ---- Form card ---- */
        UITheme.RoundedPanel formCard = new UITheme.RoundedPanel();
        formCard.setArc(16);
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 20, 0, 20);

        c.gridy = 0;
        formCard.add(fieldLabel("Expense ID (auto)"), c);
        c.gridy = 1;
        c.insets = new Insets(4, 20, 0, 20);
        txtExpenseId = readOnlyField("Auto");
        formCard.add(txtExpenseId, c);

        c.gridy = 2;
        c.insets = new Insets(12, 20, 0, 20);
        formCard.add(fieldLabel("Description / Notes (optional)"), c);
        c.gridy = 3;
        c.insets = new Insets(4, 20, 0, 20);
        txtDescription = new UITheme.RoundedField();
        txtDescription.setIconChar("✓");
        formCard.add(txtDescription, c);

        c.gridy = 4;
        c.insets = new Insets(12, 20, 0, 20);
        formCard.add(fieldLabel("Amount (₹)"), c);
        c.gridy = 5;
        c.insets = new Insets(4, 20, 0, 20);
        fAmount = new UITheme.RoundedField();
        fAmount.setIconChar("₹");
        formCard.add(fAmount, c);

        c.gridy = 6;
        c.insets = new Insets(12, 20, 0, 20);
        formCard.add(fieldLabel("Category"), c);
        c.gridy = 7;
        c.insets = new Insets(4, 20, 0, 20);
        cmbCategory = new javax.swing.JComboBox<>();
        cmbCategory.setFont(UITheme.F_BODY);
        cmbCategory.setPreferredSize(new Dimension(300, 40));
        formCard.add(cmbCategory, c);

        c.gridy = 8;
        c.insets = new Insets(12, 20, 0, 20);
        formCard.add(fieldLabel("Expense Type"), c);
        c.gridy = 9;
        c.insets = new Insets(4, 20, 0, 20);
        cmbExpenseType = new javax.swing.JComboBox<>();
        cmbExpenseType.setFont(UITheme.F_BODY);
        cmbExpenseType.setPreferredSize(new Dimension(300, 40));
        formCard.add(cmbExpenseType, c);

        c.gridy = 10;
        c.insets = new Insets(12, 20, 0, 20);
        formCard.add(fieldLabel("Date"), c);
        c.gridy = 11;
        c.insets = new Insets(4, 20, 0, 20);
        spinnerDate = UITheme.dateSpinner(LocalDate.now());
        spinnerDate.setPreferredSize(new Dimension(300, 40));
        formCard.add(spinnerDate, c);

        c.gridy = 12;
        c.insets = new Insets(18, 20, 0, 20);
        btnSave = new UITheme.RoundedButton("Save Expense");
        btnSave.setBg(new Color(46, 160, 90));
        btnSave.setHoverColor(new Color(28, 140, 76));
        btnSave.addActionListener(e -> saveExpense());
        formCard.add(btnSave, c);

        c.gridy = 13;
        c.insets = new Insets(8, 20, 20, 20);
        btnClear = new UITheme.RoundedButton("Clear");
        btnClear.setBg(new Color(140, 145, 155));
        btnClear.setHoverColor(new Color(115, 122, 135));
        btnClear.addActionListener(e -> clearForm());
        formCard.add(btnClear, c);

        /* ---- Budget alert card ---- */
        UITheme.RoundedPanel alertCard = new UITheme.RoundedPanel();
        alertCard.setArc(16);
        alertCard.setPreferredSize(new Dimension(360, 260));
        alertCard.setLayout(new GridBagLayout());
        GridBagConstraints a = new GridBagConstraints();
        a.gridx = 0;
        a.weightx = 1;
        a.fill = GridBagConstraints.HORIZONTAL;

        a.gridy = 0;
        a.insets = new Insets(0, 0, 0, 0);
        alertCard.add(fieldLabel("Budget Alert"), a);

        a.gridy = 1;
        a.insets = new Insets(14, 0, 0, 0);
        progressAlert = new JProgressBar(0, 1000);
        progressAlert.setStringPainted(true);
        progressAlert.setPreferredSize(new Dimension(300, 28));
        alertCard.add(progressAlert, a);

        a.gridy = 2;
        a.insets = new Insets(12, 0, 0, 0);
        lblAlert = new JLabel(" ");
        lblAlert.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAlert.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        alertCard.add(lblAlert, a);

        a.gridy = 3;
        a.insets = new Insets(10, 4, 0, 4);
        lblSummary = new JLabel(" ");
        lblSummary.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSummary.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        alertCard.add(lblSummary, a);

        JPanel cards = new JPanel();
        cards.setOpaque(false);
        cards.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        cards.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 22, 22, 22));
        cards.add(formCard);
        cards.add(alertCard);

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

    private static JTextField readOnlyField(String text) {
        JTextField f = new JTextField(text);
        f.setEditable(false);
        f.setBackground(new Color(233, 238, 246));
        f.setForeground(new Color(90, 105, 125));
        f.setFont(UITheme.F_BODY);
        f.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12));
        f.setPreferredSize(new Dimension(300, 38));
        return f;
    }

    /* ===================== BUDGET ALERT ===================== */

    private String selectedDate() {
        Object v = spinnerDate.getValue();
        return new SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) v);
    }

    private void refreshBudgetPanel() {
        String month = BudgetUtil.currentMonth();
        double budget = BudgetUtil.budgetForMonth(UserSession.userId, month);
        double spent = BudgetUtil.spentForMonth(UserSession.userId, month);

        double used = BudgetUtil.usageFraction(spent, budget);
        String barText = budget <= 0
                ? "No budget"
                : String.format("Used ₹ %,.0f of ₹ %,.0f", spent, budget);
        UITheme.styleProgress(progressAlert, used, barText);
        lblAlert.setText(BudgetUtil.alertMessage(spent, budget));
        lblAlert.setForeground(BudgetUtil.alertColor(spent, budget));

        lblSummary.setText("<html><div style='text-align:center'>"
                + "<b>Month:</b> " + month
                + " &nbsp;|&nbsp; <b>Budget:</b> ₹ " + String.format("%,.0f", budget)
                + " &nbsp;|&nbsp; <b>Spent:</b> ₹ " + String.format("%,.0f", spent)
                + " &nbsp;|&nbsp; <b>Remaining:</b> ₹ " + String.format("%,.0f", budget - spent)
                + "</div></html>");
    }

    private void showBudgetAlertDialog() {
        String month = BudgetUtil.currentMonth();
        double budget = BudgetUtil.budgetForMonth(UserSession.userId, month);
        double spent = BudgetUtil.spentForMonth(UserSession.userId, month);
        if (budget <= 0) {
            return;
        }
        String msg = BudgetUtil.alertMessage(spent, budget);
        int type;
        if (spent > budget || spent / budget >= 1.0) {
            type = JOptionPane.WARNING_MESSAGE;
        } else if (spent / budget >= 0.8) {
            type = JOptionPane.WARNING_MESSAGE;
        } else {
            return; // only warn at/above the 80% threshold
        }
        JOptionPane.showMessageDialog(this, msg, "Budget Alert", type);
    }

    /* ===================== ACTIONS ===================== */

    private boolean isValidAmount(String text) {
        try {
            return Double.parseDouble(text.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void saveExpense() {
        String amountText = fAmount.getText().trim();
        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Budget Limit Exceeded!!",
                    "Add Expense", JOptionPane.WARNING_MESSAGE);
            fAmount.requestFocusInWindow();
            return;
        }
        if (!isValidAmount(amountText)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount greater than 0!",
                    "Add Expense", JOptionPane.WARNING_MESSAGE);
            fAmount.requestFocusInWindow();
            return;
        }

        String category = str(cmbCategory.getSelectedItem());
        String expenseType = str(cmbExpenseType.getSelectedItem());
        String date = selectedDate();
        String description = txtDescription.getText().trim();

        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO expenses(user_id, amount, category, date, description, payment_mode, expense_type) "
                        + "VALUES (?, ?, ?, ?, ?, 'Cash', ?)",
                        PreparedStatement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, UserSession.userId);
            pst.setDouble(2, Double.parseDouble(amountText));
            pst.setString(3, category);
            pst.setString(4, date);
            pst.setString(5, description);
            pst.setString(6, expenseType);
            pst.executeUpdate();

            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    txtExpenseId.setText("Expense #" + keys.getInt(1));
                }
            }

            clearForm();
            refreshBudgetPanel();
            JOptionPane.showMessageDialog(this, "Expense added successfully!",
                    "Added", JOptionPane.INFORMATION_MESSAGE);
            showBudgetAlertDialog();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not add expense: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        fAmount.setText("");
        txtDescription.setText("");
        ((javax.swing.SpinnerDateModel) spinnerDate.getModel())
                .setValue(java.sql.Date.valueOf(LocalDate.now()));
        fAmount.requestFocusInWindow();
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new ExpenseTrackerUI().setVisible(true));
    }
}