import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * CategoriesUI — full Category Management screen.
 *
 * Features:
 *  - Add, Update, Delete, Clear
 *  - Search by category name
 *  - Auto-generated Category ID (displayed read-only)
 *  - Validation (empty name, duplicate names)
 *  - Confirmation before delete
 *  - Modern sidebar navigation + responsive layout
 *
 * All persistence goes through CategoryDAO (PreparedStatement only).
 *
 * @author preetam-06
 */
public class CategoriesUI extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(CategoriesUI.class.getName());

    private final CategoryDAO dao = new CategoryDAO();

    private JPanel jSidebar;
    private UITheme.RoundedPanel jFormCard;
    private UITheme.RoundedPanel jTableCard;
    private JTextField jTxtId;
    private UITheme.RoundedField jTxtName;
    private UITheme.RoundedField jTxtSearch;
    private UITheme.RoundedButton btnAdd;
    private UITheme.RoundedButton btnUpdate;
    private UITheme.RoundedButton btnDelete;
    private UITheme.RoundedButton btnClear;
    private UITheme.RoundedButton btnSearch;
    private UITheme.RoundedButton btnShowAll;
    private JScrollPane jScroll;
    private javax.swing.JTable jTable1;

    public CategoriesUI() {
        buildUI();
        setTitle("Categories - Expense Tracker");
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1080, 640));
        jTable1.getSelectionModel().addListSelectionListener(e -> populateFromRow());
        loadCategories();
    }

    private void buildUI() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jSidebar = UITheme.createSidebar(this, 6);

        JPanel content = new JPanel();
        content.setBackground(UITheme.CONTENT_BG);
        content.setLayout(new BorderLayout());

        /* Header */
        JPanel header = new JPanel(new java.awt.GridLayout(2, 1));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(22, 22, 2, 22));
        JLabel pageTitle = new JLabel("Category Management");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        pageTitle.setForeground(UITheme.TEXT);
        JLabel pageSub = new JLabel("Add, edit, delete and search expense categories");
        pageSub.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        pageSub.setForeground(UITheme.MUTED);
        header.add(pageTitle);
        header.add(pageSub);

        /* Form card */
        jFormCard = new UITheme.RoundedPanel();
        jFormCard.setArc(16);
        jFormCard.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 20, 0, 20);

        c.gridy = 0;
        jFormCard.add(fieldLabel("Category ID (auto)"), c);
        c.gridy = 1;
        c.insets = new Insets(4, 20, 0, 20);
        jTxtId = new JTextField("Auto-generated");
        jTxtId.setEditable(false);
        jTxtId.setBackground(new Color(233, 238, 246));
        jTxtId.setForeground(new Color(110, 120, 138));
        jTxtId.setFont(UITheme.F_BODY);
        jTxtId.setPreferredSize(new Dimension(220, 34));
        jFormCard.add(jTxtId, c);

        c.gridy = 2;
        c.insets = new Insets(12, 20, 0, 20);
        jFormCard.add(fieldLabel("Category Name"), c);
        c.gridy = 3;
        c.insets = new Insets(4, 20, 0, 20);
        jTxtName = new UITheme.RoundedField();
        jTxtName.setIconChar("\u2727");
        jTxtName.setToolTipText("Enter a category name");
        jTxtName.addActionListener(e -> btnAdd.doClick());
        jFormCard.add(jTxtName, c);

        c.gridy = 4;
        c.insets = new Insets(6, 20, 0, 20);
        JLabel hint = new JLabel("Select a row to edit it, or type a new name to add.");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hint.setForeground(new Color(118, 128, 148));
        jFormCard.add(hint, c);

        c.gridy = 5;
        c.insets = new Insets(16, 20, 0, 20);
        btnAdd = new UITheme.RoundedButton("Add Category");
        btnAdd.setBg(UITheme.GREEN);
        btnAdd.setHoverColor(new Color(28, 140, 76));
        btnAdd.addActionListener(e -> addCategory());
        jFormCard.add(btnAdd, c);

        c.gridy = 6;
        c.insets = new Insets(8, 20, 0, 20);
        btnUpdate = new UITheme.RoundedButton("Update Category");
        btnUpdate.setBg(UITheme.BLUE);
        btnUpdate.setHoverColor(UITheme.BLUE_DARK);
        btnUpdate.addActionListener(e -> updateCategory());
        jFormCard.add(btnUpdate, c);

        c.gridy = 7;
        c.insets = new Insets(8, 20, 0, 20);
        btnDelete = new UITheme.RoundedButton("Delete Category");
        btnDelete.setBg(UITheme.RED);
        btnDelete.setHoverColor(new Color(180, 45, 45));
        btnDelete.addActionListener(e -> deleteCategory());
        jFormCard.add(btnDelete, c);

        c.gridy = 8;
        c.insets = new Insets(8, 20, 20, 20);
        btnClear = new UITheme.RoundedButton("Clear");
        btnClear.setBg(new Color(140, 145, 155));
        btnClear.setHoverColor(new Color(115, 122, 135));
        btnClear.addActionListener(e -> clearFields());
        jFormCard.add(btnClear, c);

        /* Table card */
        jTableCard = new UITheme.RoundedPanel();
        jTableCard.setArc(16);
        jTableCard.setLayout(new BorderLayout());

        JPanel tableHead = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 12));
        tableHead.setOpaque(false);
        jTxtSearch = new UITheme.RoundedField();
        jTxtSearch.setIconChar("\u2315");
        jTxtSearch.setToolTipText("Search categories by name");
        jTxtSearch.setPreferredSize(new Dimension(240, 36));
        jTxtSearch.addActionListener(e -> btnSearch.doClick());
        btnSearch = new UITheme.RoundedButton("Search");
        btnSearch.setBg(UITheme.TEAL);
        btnSearch.setHoverColor(new Color(9, 125, 136));
        btnSearch.setPreferredSize(new Dimension(100, 36));
        btnSearch.addActionListener(e -> searchCategories());
        btnShowAll = new UITheme.RoundedButton("Show All");
        btnShowAll.setBg(new Color(140, 145, 155));
        btnShowAll.setHoverColor(new Color(115, 122, 135));
        btnShowAll.setPreferredSize(new Dimension(100, 36));
        btnShowAll.addActionListener(e -> {
            jTxtSearch.setText("");
            loadCategories();
        });
        tableHead.add(jTxtSearch);
        tableHead.add(btnSearch);
        tableHead.add(btnShowAll);

        jTable1 = new javax.swing.JTable();
        jTable1.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Category ID", "Category Name"}) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        });
        UITheme.applyTableStyle(jTable1, 5);
        jScroll = new JScrollPane(jTable1);
        jScroll.setBorder(BorderFactory.createEmptyBorder(2, 12, 12, 12));

        jTableCard.add(tableHead, BorderLayout.NORTH);
        jTableCard.add(jScroll, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(14, 0));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(12, 22, 22, 22));
        center.add(jFormCard, BorderLayout.WEST);
        center.add(jTableCard, BorderLayout.CENTER);

        content.add(header, BorderLayout.NORTH);
        content.add(center, BorderLayout.CENTER);

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

    private void loadCategories() {
        jTxtSearch.setText("");
        try {
            List<Category> categories = dao.getAllCategories();
            refreshTable(categories);
        } catch (SQLException ex) {
            logger.warning("loadCategories failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Could not load categories: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchCategories() {
        String keyword = jTxtSearch.getText().trim();
        try {
            List<Category> categories = keyword.isEmpty()
                    ? dao.getAllCategories()
                    : dao.searchCategory(keyword);
            refreshTable(categories);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Search failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable(List<Category> categories) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        for (Category c : categories) {
            model.addRow(new Object[]{c.getCategoryId(), c.getCategoryName()});
        }
    }

    /* ===================== ACTIONS ===================== */

    private void addCategory() {
        String name = jTxtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Category name cannot be empty.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            jTxtName.requestFocusInWindow();
            return;
        }
        try {
            dao.addCategory(new Category(0, name));
            JOptionPane.showMessageDialog(this,
                    "Category \"" + name + "\" added successfully.",
                    "Added", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            loadCategories();
        } catch (SQLException ex) {
            showDaoError(ex, "add");
        }
    }

    private void updateCategory() {
        String idText = selectedId();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a category row to update.",
                    "Update", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String name = jTxtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Category name cannot be empty.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            jTxtName.requestFocusInWindow();
            return;
        }
        try {
            int id = Integer.parseInt(idText);
            dao.updateCategory(new Category(id, name));
            JOptionPane.showMessageDialog(this,
                    "Category updated successfully.",
                    "Updated", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            loadCategories();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Select a valid category row first.",
                    "Update", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDaoError(ex, "update");
        }
    }

    private void deleteCategory() {
        String idText = selectedId();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a category row to delete.",
                    "Delete", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String name = jTxtName.getText().trim();
        int option = JOptionPane.showConfirmDialog(this,
                "Delete category \"" + name + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int id = Integer.parseInt(idText);
            dao.deleteCategory(id);
            JOptionPane.showMessageDialog(this,
                    "Category deleted.", "Deleted",
                    JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            loadCategories();
        } catch (SQLException ex) {
            showDaoError(ex, "delete");
        }
    }

    private void clearFields() {
        jTxtId.setText("Auto-generated");
        jTxtName.setText("");
        jTable1.clearSelection();
        jTxtName.requestFocusInWindow();
    }

    private String selectedId() {
        int row = jTable1.getSelectedRow();
        if (row < 0) {
            return "";
        }
        Object v = jTable1.getValueAt(row, 0);
        return v == null ? "" : v.toString();
    }

    private void populateFromRow() {
        int row = jTable1.getSelectedRow();
        if (row < 0) {
            return;
        }
        Object id = jTable1.getValueAt(row, 0);
        Object name = jTable1.getValueAt(row, 1);
        jTxtId.setText(id == null ? "" : id.toString());
        jTxtName.setText(name == null ? "" : name.toString());
    }

    private void showDaoError(SQLException ex, String action) {
        String msg = ex.getMessage();
        if (msg != null && msg.contains("Duplicate")) {
            JOptionPane.showMessageDialog(this,
                    msg, "Duplicate Category", JOptionPane.WARNING_MESSAGE);
        } else {
            logger.warning(action + " category failed: " + msg);
            JOptionPane.showMessageDialog(this,
                    "Could not " + action + " category: " + msg,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            UserSession.userId = 1;
            UserSession.username = "admin";
            new CategoriesUI().setVisible(true);
        });
    }
}