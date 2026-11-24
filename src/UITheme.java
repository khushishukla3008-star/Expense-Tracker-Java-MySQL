import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * UITheme — shared modern look-and-feel helpers for the Expense Tracker system.
 *
 * Provides the color palette, fonts and reusable custom-painted Swing
 * components (gradient panels, rounded panels, rounded input fields,
 * rounded buttons, sidebar buttons, icon badges and table styling).
 *
 * @author preetam-06
 */
public final class UITheme {

    private UITheme() {
    }

    /* ======================= PALETTE ======================= */

    public static final Color NAVY = new Color(23, 42, 75);
    public static final Color NAVY_DARK = new Color(14, 28, 55);
    public static final Color BLUE = new Color(43, 125, 233);
    public static final Color BLUE_DARK = new Color(28, 95, 195);
    public static final Color TEAL = new Color(14, 154, 167);
    public static final Color MUTED = new Color(118, 128, 148);
    public static final Color CONTENT_BG = new Color(244, 247, 252);
    public static final Color TEXT = new Color(40, 50, 70);
    public static final Color CARD_LINE = new Color(222, 229, 240);
    public static final Color GREEN = new Color(35, 165, 90);
    public static final Color RED = new Color(210, 60, 60);
    public static final Color GOLD = new Color(224, 160, 40);
    public static final Color PURPLE = new Color(140, 90, 210);

    /* ======================= FONTS ======================= */

    public static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font F_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 16);
    public static final Font F_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font F_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font F_TAG = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    /** Formats a Rupee amount like "₹ 1,23,456". */
    public static String rupee(double value) {
        return "₹ " + String.format("%,.0f", value);
    }

    /* ================== GRADIENT PANEL ================== */

    /** A JPanel filled with a vertical blue → teal gradient. */
    public static class GradientPanel extends JPanel {

        private Color color1 = BLUE;
        private Color color2 = TEAL;

        public GradientPanel() {
            setOpaque(false);
        }

        public GradientPanel(java.awt.LayoutManager lm) {
            super(lm);
            setOpaque(false);
        }

        public void setColor1(Color c) {
            color1 = c;
            repaint();
        }

        public void setColor2(Color c) {
            color2 = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (w > 0 && h > 0) {
                g2.setPaint(new GradientPaint(0, 0, color1, 0, h, color2));
                g2.fillRect(0, 0, w, h);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /* =================== ROUNDED PANEL =================== */

    /** A white card with rounded corners, a soft shadow and an optional border. */
    public static class RoundedPanel extends JPanel {

        private Color fill = Color.WHITE;
        private Color line = CARD_LINE;
        private int arc = 18;

        public RoundedPanel() {
            setOpaque(false);
        }

        public RoundedPanel(java.awt.LayoutManager lm) {
            super(lm);
            setOpaque(false);
        }

        public void setFill(Color c) {
            fill = c;
            repaint();
        }

        public void setLineColor(Color c) {
            line = c;
            repaint();
        }

        public void setArc(int a) {
            arc = a;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (w > 0 && h > 0) {
                g2.setColor(new Color(0, 0, 0, 16));
                g2.fill(new RoundRectangle2D.Double(0, 3, w, h, arc, arc));
                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, arc, arc));
                if (line != null) {
                    g2.setColor(line);
                    g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 2, h - 2, arc, arc));
                }
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /* ================= ROUNDED TEXT FIELD ================= */

    /** A rounded, white text field with an optional leading icon glyph. */
    public static class RoundedField extends JTextField {

        private String icon = "";

        public RoundedField() {
            setOpaque(false);
            setBorder(new EmptyBorder(0, 36, 0, 14));
            setFont(F_BODY);
            setForeground(TEXT);
            setCaretColor(BLUE);
        }

        public void setIconChar(String s) {
            icon = s == null ? "" : s;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (w > 0 && h > 0) {
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 12, 12));
                g2.setColor(new Color(205, 214, 230));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 2, h - 2, 12, 12));
                if (!icon.isEmpty()) {
                    g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
                    g2.setColor(new Color(90, 112, 140));
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (h + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(icon, 12, y);
                }
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /* ================ ROUNDED PASSWORD FIELD ================ */

    /** A rounded password field with an optional leading icon glyph. */
    public static class RoundedPasswordField extends JPasswordField {

        private String icon = "";

        public RoundedPasswordField() {
            setEchoChar('\u2022');
            setOpaque(false);
            setBorder(new EmptyBorder(0, 36, 0, 14));
            setFont(F_BODY);
            setForeground(TEXT);
            setCaretColor(BLUE);
        }

        public void setIconChar(String s) {
            icon = s == null ? "" : s;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (w > 0 && h > 0) {
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 12, 12));
                g2.setColor(new Color(205, 214, 230));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 2, h - 2, 12, 12));
                if (!icon.isEmpty()) {
                    g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
                    g2.setColor(new Color(90, 112, 140));
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (h + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(icon, 12, y);
                }
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /* ================== ROUNDED BUTTON ================== */

    /** A rounded, flat button with hover and pressed effects. */
    public static class RoundedButton extends JButton {

        private Color bg = BLUE;
        private Color hover = BLUE_DARK;
        private Color pressed = new Color(20, 70, 150);
        private Color cur = bg;
        private int arc = 14;

        public RoundedButton() {
            this("Button");
        }

        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(F_BOLD);
            setForeground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    cur = hover;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    cur = bg;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    cur = pressed;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    cur = hover;
                    repaint();
                }
            });
        }

        public void setBg(Color c) {
            bg = c;
            cur = c;
            repaint();
        }

        public void setHoverColor(Color c) {
            hover = c;
        }

        public void setPressedColor(Color c) {
            pressed = c;
        }

        public void setFg(Color c) {
            setForeground(c);
        }

        public void setArc(int a) {
            arc = a;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (w > 0 && h > 0) {
                g2.setColor(cur);
                g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, arc, arc));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /* ================= SIDEBAR BUTTON ================= */

    /** A flat, full-width navigation button used inside the sidebar. */
    public static class SidebarButton extends JButton {

        private String glyph;
        private Color hoverColor = new Color(38, 68, 118);
        private boolean hovered = false;
        private boolean selected = false;

        public SidebarButton() {
            this("", "Button");
        }

        public SidebarButton(String glyph, String text) {
            super(text);
            this.glyph = glyph == null ? "" : glyph;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setHorizontalTextPosition(SwingConstants.RIGHT);
            setIconTextGap(26);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(new Color(215, 225, 240));
            setBorder(new EmptyBorder(11, 20, 11, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        public void setSelectedItem(boolean sel) {
            selected = sel;
            repaint();
        }

        public void setGlyph(String s) {
            glyph = s == null ? "" : s;
            repaint();
        }

        public void setHoverColor(Color c) {
            hoverColor = c;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (w > 0 && h > 0 && (selected || hovered)) {
                g2.setColor(hoverColor);
                g2.fill(new RoundRectangle2D.Double(4, 3, w - 8, h - 6, 10, 10));
                if (selected) {
                    g2.setColor(TEAL);
                    g2.fill(new RoundRectangle2D.Double(4, 3, 3, h - 6, 2, 2));
                }
            }
            g2.dispose();

            if (!glyph.isEmpty()) {
                g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 15));
                g2.setColor(selected ? new Color(130, 210, 235) : new Color(120, 190, 255));
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() + (fm.getAscent() - fm.getDescent())) / 2;
                g2.drawString(glyph, 12, y);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    /* =================== ICON BADGE =================== */

    /** A circular colored badge showing a single glyph (used on dashboard cards). */
    public static class IconBadge extends JPanel {

        private String glyph = "";
        private Color color = BLUE;

        public IconBadge() {
            setOpaque(false);
            setPreferredSize(new Dimension(46, 46));
        }

        public IconBadge(String glyph, Color color) {
            this();
            this.glyph = glyph == null ? "" : glyph;
            this.color = color;
        }

        public void setGlyph(String s) {
            glyph = s == null ? "" : s;
            repaint();
        }

        public void setBadgeColor(Color c) {
            color = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int d = Math.min(getWidth(), getHeight());
            if (d > 0) {
                g2.setColor(new Color(0, 0, 0, 18));
                g2.fillOval(1, 3, d - 3, d - 3);
                g2.setColor(color);
                g2.fillOval(0, 0, d - 1, d - 1);
                g2.setFont(new Font("Segoe UI Symbol", Font.BOLD, 20));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = d / 2 - fm.stringWidth(glyph) / 2;
                int y = (d + (fm.getAscent() - fm.getDescent())) / 2 - 1;
                g2.drawString(glyph, x, y);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /* ================== TABLE STYLING ================== */

    /**
     * Applies the shared table style: navy header, alternate row colors,
     * centered cells, auto-resize and non-editable cells.
     *
     * @param leftColumns column indexes whose content should stay left-aligned
     */
    public static void applyTableStyle(JTable table, int... leftColumns) {
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setGridColor(new Color(228, 233, 242));
        table.setSelectionBackground(new Color(70, 120, 220));
        table.setSelectionForeground(Color.WHITE);
        table.setFont(F_BODY);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setDefaultEditor(Object.class, null);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setBackground(NAVY);
        header.setForeground(Color.WHITE);
        header.setFont(F_TAG);
        header.setPreferredSize(new Dimension(0, 34));

        AltRenderer renderer = new AltRenderer();
        renderer.leftColumns.addAll(toSet(leftColumns));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private static java.util.List<Integer> toSet(int... cols) {
        java.util.List<Integer> l = new java.util.ArrayList<>();
        if (cols != null) {
            for (int c : cols) {
                l.add(c);
            }
        }
        return l;
    }

    /** Default renderer with alternate row colours and centered text. */
    public static class AltRenderer extends DefaultTableCellRenderer {

        private final java.util.List<Integer> leftColumns = new java.util.ArrayList<>();
        private final BorderlessEmpty border = new BorderlessEmpty();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(border);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(243, 246, 252));
                c.setForeground(TEXT);
            }
            if (leftColumns.contains(column)) {
                setHorizontalAlignment(SwingConstants.LEFT);
            }
            return c;
        }
    }

    /** Simple padded border shared by table cells. */
    static class BorderlessEmpty extends EmptyBorder {
        BorderlessEmpty() {
            super(0, 8, 0, 8);
        }
    }

    /** Returns a rounded line border with the given colour (used ad-hoc). */
    public static javax.swing.border.Border roundedLine(Color color, int thickness, int arc) {
        return new javax.swing.border.LineBorder(color, thickness, true) {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getLineColor());
                g2.setStroke(new java.awt.BasicStroke(thickness));
                g2.draw(new RoundRectangle2D.Double(x, y, width - 1, height - 1, arc, arc));
                g2.dispose();
            }
        };
    }

    public static Color derive(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    public static javax.swing.border.Border emptyBorder(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    /* =================== DATE SPINNER =================== */

    /** Returns a JSpinner date picker formatted as yyyy-MM-dd. */
    public static JSpinner dateSpinner(LocalDate value) {
        LocalDate d = value == null ? LocalDate.now() : value;
        SpinnerDateModel model = new SpinnerDateModel(
                java.sql.Date.valueOf(d), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
        spinner.setFont(F_BODY);
        return spinner;
    }

    /* =================== PROGRESS BAR STYLING =================== */

    /**
     * Styles a progress bar to show a fraction (0..1) with a labelled string.
     * The bar turns green/orange/red depending on the usage fraction.
     */
    public static void styleProgress(JProgressBar bar, double fraction, String label) {
        int value = fraction >= 1 ? 1000 : (fraction <= 0 ? 0 : (int) (fraction * 1000));
        bar.setMinimum(0);
        bar.setMaximum(1000);
        bar.setValue(value);
        bar.setStringPainted(true);
        bar.setString(label);
        bar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        bar.setBackground(new Color(235, 235, 242));
        if (fraction >= 1.0) {
            bar.setForeground(new Color(210, 60, 60));
        } else if (fraction >= 0.8) {
            bar.setForeground(new Color(224, 160, 40));
        } else {
            bar.setForeground(new Color(46, 170, 90));
        }
    }

    /* =================== SIDEBAR NAVIGATION =================== */

    /** Sidebar menu labels shown on every post-login screen. */
    private static final String[][] NAV_ITEMS = {
        {"\u2302", "Dashboard"},
        {"\u2605", "My Profile"},
        {"\u2713", "Income"},
        {"+", "Add Expense"},
        {"\u25b6", "Expense History"},
        {"\u20b9", "Set Budget"},
        {"\u2726", "Categories"},
        {"\u2630", "Reports"},
        {"\u23ce", "Logout"}
    };

    /**
     * Builds the shared navigation sidebar for the given screen.
     *
     * @param owner         the screen that will be replaced on navigation
     * @param selectedIndex index of the active menu item
     */
    public static JPanel createSidebar(final JFrame owner, final int selectedIndex) {
        JPanel p = new JPanel();
        p.setBackground(NAVY);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("  Expense Tracker");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brand.setForeground(Color.WHITE);
        brand.setBorder(BorderFactory.createEmptyBorder(26, 12, 22, 12));
        p.add(brand);

        for (int i = 0; i < NAV_ITEMS.length; i++) {
            final int index = i;
            SidebarButton b = new SidebarButton(NAV_ITEMS[i][0], NAV_ITEMS[i][1]);
            b.setSelectedItem(index == selectedIndex);
            b.addActionListener(e -> openScreen(owner, index));
            p.add(b);
        }
        return p;
    }

    /** Opens the screen at the given sidebar index and disposes the current owner. */
    public static void openScreen(JFrame owner, int index) {
        JFrame target = null;
        switch (index) {
            case 0:
                target = new DashboardUI();
                break;
            case 1:
                target = new ProfileUI();
                break;
            case 2:
                target = new IncomeUI();
                break;
            case 3:
                target = new ExpenseTrackerUI();
                break;
            case 4:
                target = new ViewExpensesUI();
                break;
            case 5:
                target = new SetBudgetForm();
                break;
            case 6:
                target = new CategoriesUI();
                break;
            case 7:
                target = new ReportsUI();
                break;
            case 8:
            default:
                int opt = JOptionPane.showConfirmDialog(owner,
                        "Logout and return to the login screen?",
                        "Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (opt == JOptionPane.YES_OPTION) {
                    UserSession.clear();
                    target = new LoginUI();
                }
                if (owner != null) {
                    owner.dispose();
                }
                if (target != null) {
                    target.setVisible(true);
                }
                return;
        }
        if (owner != null) {
            owner.dispose();
        }
        target.setVisible(true);
    }
}