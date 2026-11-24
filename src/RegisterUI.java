import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;

/*
 * Registration form - creates a new user with full-name, email and validation.
 * An interactive, code-only JFrame (no .form file).
 */

/**
 * Interactive registration — Modern Expense Tracker.
 *
 * @author preetam-06
 */
public class RegisterUI extends javax.swing.JFrame {

    private UITheme.GradientPanel root;
    private UITheme.RoundedPanel card;
    private UITheme.IconBadge badge;
    private JLabel title;
    private JLabel subtitle;
    private UITheme.RoundedField fName;
    private JLabel errName;
    private UITheme.RoundedField fUser;
    private JLabel errUser;
    private UITheme.RoundedField fEmail;
    private JLabel errEmail;
    private UITheme.RoundedPasswordField fPass;
    private JProgressBar strength;
    private JLabel strengthLabel;
    private JLabel errPass;
    private UITheme.RoundedPasswordField fConfirm;
    private JLabel errConfirm;
    private UITheme.RoundedButton btnShowPass;
    private UITheme.RoundedButton btnRegister;
    private UITheme.RoundedButton btnBack;

    private boolean passVisible = false;

    public RegisterUI() {
        buildUI();
        setTitle("Create Account - Expense Tracker");
        setMinimumSize(new Dimension(860, 620));
        setLocationRelativeTo(null);
        setTooltips();
        fName.requestFocusInWindow();
    }

    private void setTooltips() {
        fName.setToolTipText("Your full name (required)");
        fUser.setToolTipText("Choose a unique username");
        fEmail.setToolTipText("Your email address");
        fPass.setToolTipText("Minimum 6 characters");
        fConfirm.setToolTipText("Re-type the password");
        btnShowPass.setToolTipText("Show / hide the passwords");
        btnRegister.setToolTipText("Create the account");
        btnBack.setToolTipText("Return to the login screen");
    }

    private void buildUI() {
        root = new UITheme.GradientPanel();
        root.setColor1(UITheme.NAVY);
        root.setColor2(UITheme.TEAL);

        card = new UITheme.RoundedPanel();
        card.setArc(22);
        card.setPreferredSize(new Dimension(560, 600));

        badge = new UITheme.IconBadge("★", new Color(60, 130, 215));

        title = new JLabel("Create Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(UITheme.NAVY);

        subtitle = new JLabel("Join Expense Tracker to manage your budget");
        subtitle.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        subtitle.setForeground(UITheme.MUTED);

        fName = new UITheme.RoundedField();
        fName.setIconChar("N");
        fName.addActionListener(e -> fUser.requestFocusInWindow());

        fUser = new UITheme.RoundedField();
        fUser.setIconChar("@");
        fUser.addActionListener(e -> fEmail.requestFocusInWindow());

        fEmail = new UITheme.RoundedField();
        fEmail.setIconChar("E");
        fEmail.addActionListener(e -> fPass.requestFocusInWindow());

        errName = errorLabel();
        errUser = errorLabel();
        errEmail = errorLabel();
        errPass = errorLabel();
        errConfirm = errorLabel();

        fPass = new UITheme.RoundedPasswordField();
        fPass.setIconChar("•");
        fPass.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }
        });

        strength = new JProgressBar(0, 100);
        strength.setPreferredSize(new Dimension(300, 14));
        strength.setStringPainted(false);
        strengthLabel = new JLabel("Password strength: —");
        strengthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        strengthLabel.setForeground(UITheme.MUTED);

        fConfirm = new UITheme.RoundedPasswordField();
        fConfirm.setIconChar("•");
        fConfirm.addActionListener(e -> btnRegister.doClick());

        btnShowPass = new UITheme.RoundedButton("Show");
        btnShowPass.setBg(new Color(90, 112, 140));
        btnShowPass.setHoverColor(new Color(70, 92, 120));
        btnShowPass.setPreferredSize(new Dimension(96, 40));
        btnShowPass.addActionListener(e -> togglePass());

        btnRegister = new UITheme.RoundedButton("Register");
        btnRegister.setBg(UITheme.TEAL);
        btnRegister.setHoverColor(new Color(9, 125, 136));
        btnRegister.setPreferredSize(new Dimension(360, 42));
        btnRegister.addActionListener(e -> register());

        btnBack = new UITheme.RoundedButton("Back to Login");
        btnBack.setBg(UITheme.GOLD);
        btnBack.setHoverColor(new Color(200, 140, 30));
        btnBack.setPreferredSize(new Dimension(360, 42));
        btnBack.addActionListener(e -> {
            new LoginUI().setVisible(true);
            dispose();
        });

        // Stack all fields on the card.
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);

        c.gridy++;
        form.add(centered(badge), c);
        c.gridy++;
        c.insets = new Insets(12, 0, 0, 0);
        form.add(centered(title), c);
        c.gridy++;
        c.insets = new Insets(2, 0, 0, 0);
        form.add(centered(subtitle), c);

        c.insets = new Insets(14, 30, 0, 30);
        c.anchor = GridBagConstraints.WEST;
        c.gridy++;
        form.add(new JLabel("Full Name"), c);
        c.gridy++;
        c.insets = new Insets(4, 30, 0, 30);
        form.add(fName, c);
        c.gridy++;
        form.add(errName, c);

        c.gridy++;
        c.insets = new Insets(10, 30, 0, 30);
        form.add(new JLabel("Username"), c);
        c.gridy++;
        c.insets = new Insets(4, 30, 0, 30);
        form.add(fUser, c);
        c.gridy++;
        form.add(errUser, c);

        c.gridy++;
        c.insets = new Insets(10, 30, 0, 30);
        form.add(new JLabel("Email"), c);
        c.gridy++;
        c.insets = new Insets(4, 30, 0, 30);
        form.add(fEmail, c);
        c.gridy++;
        form.add(errEmail, c);

        c.gridy++;
        c.insets = new Insets(10, 30, 0, 30);
        form.add(new JLabel("Password"), c);
        c.gridy++;
        c.insets = new Insets(4, 30, 0, 30);
        form.add(fPass, c);

        c.gridy++;
        c.insets = new Insets(6, 30, 0, 30);
        JPanel strRow = new JPanel(new GridBagLayout());
        strRow.setOpaque(false);
        GridBagConstraints s = new GridBagConstraints();
        s.weightx = 1;
        s.fill = GridBagConstraints.HORIZONTAL;
        strRow.add(strength, s);
        s.weightx = 0;
        s.insets = new Insets(0, 10, 0, 0);
        strRow.add(strengthLabel, s);
        s.insets = new Insets(0, 0, 0, 0);
        form.add(strRow, c);
        c.gridy++;
        form.add(errPass, c);

        c.gridy++;
        c.insets = new Insets(10, 30, 0, 30);
        JPanel confirmRow = new JPanel(new GridBagLayout());
        confirmRow.setOpaque(false);
        FormRowPacker.packRow(confirmRow, fConfirm, btnShowPass);
        form.add(confirmRow, c);
        c.gridy++;
        form.add(errConfirm, c);

        c.gridy++;
        c.insets = new Insets(16, 30, 0, 30);
        form.add(btnRegister, c);
        c.gridy++;
        c.insets = new Insets(10, 30, 6, 30);
        form.add(btnBack, c);

        JPanel cardInner = new JPanel(new java.awt.BorderLayout());
        cardInner.setOpaque(false);
        cardInner.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));
        cardInner.add(form, java.awt.BorderLayout.CENTER);
        card.setLayout(new java.awt.BorderLayout());
        card.add(cardInner, java.awt.BorderLayout.CENTER);

        java.awt.GridBagLayout g = new java.awt.GridBagLayout();
        root.setLayout(g);
        GridBagConstraints rc = new GridBagConstraints();
        rc.weightx = 1;
        rc.weighty = 1;
        root.add(card, rc);

        getContentPane().setLayout(new java.awt.BorderLayout());
        getContentPane().add(root, java.awt.BorderLayout.CENTER);

        pack();
    }

    private static JLabel errorLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(new Color(200, 50, 50));
        return l;
    }

    private static <T extends java.awt.Component> T centered(T l) {
        if (l instanceof JLabel) {
            ((JLabel) l).setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        }
        return l;
    }

    private void togglePass() {
        passVisible = !passVisible;
        char ch = passVisible ? 0 : '\u2022';
        fPass.setEchoChar(ch);
        fConfirm.setEchoChar(ch);
        btnShowPass.setText(passVisible ? "Hide" : "Show");
    }

    private void updateStrength() {
        String pw = new String(fPass.getPassword());
        int score = 0;
        if (pw.length() >= 6) score += 20;
        if (pw.length() >= 10) score += 20;
        if (pw.matches(".*[A-Z].*")) score += 20;
        if (pw.matches(".*[a-z].*")) score += 10;
        if (pw.matches(".*\\d.*")) score += 15;
        if (pw.matches(".*[^A-Za-z0-9].*")) score += 15;

        strength.setValue(score);
        if (pw.isEmpty()) {
            strengthLabel.setText("Password strength: —");
            strength.setForeground(UITheme.MUTED);
        } else if (score < 40) {
            strengthLabel.setText("Weak");
            strength.setForeground(UITheme.RED);
        } else if (score < 70) {
            strengthLabel.setText("Fair");
            strength.setForeground(UITheme.GOLD);
        } else if (score < 90) {
            strengthLabel.setText("Good");
            strength.setForeground(UITheme.BLUE);
        } else {
            strengthLabel.setText("Strong");
            strength.setForeground(UITheme.GREEN);
        }
    }

    private boolean usernameExists(String username) {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT COUNT(*) FROM users WHERE username = ?")) {
            pst.setString(1, username);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private void register() {
        String name = fName.getText().trim();
        String username = fUser.getText().trim();
        String email = fEmail.getText().trim();
        String pass = new String(fPass.getPassword());
        String confirm = new String(fConfirm.getPassword());

        boolean ok = true;
        if (name.isEmpty()) {
            errName.setText("Please enter your full name.");
            fName.requestFocusInWindow();
            ok = false;
        } else {
            errName.setText(" ");
        }

        if (username.isEmpty()) {
            errUser.setText("Please choose a username.");
            fUser.requestFocusInWindow();
            ok = false;
        } else if (username.length() > 50) {
            errUser.setText("Username must be at most 50 characters.");
            fUser.requestFocusInWindow();
            ok = false;
        } else if (usernameExists(username)) {
            errUser.setText("Username already exists. Choose another.");
            fUser.requestFocusInWindow();
            ok = false;
        } else {
            errUser.setText(" ");
        }

        if (email.isEmpty()) {
            errEmail.setText("Please enter your email.");
            fEmail.requestFocusInWindow();
            ok = false;
        } else if (!email.matches("^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            errEmail.setText("Please enter a valid email address.");
            fEmail.requestFocusInWindow();
            ok = false;
        } else {
            errEmail.setText(" ");
        }

        if (pass.isEmpty()) {
            errPass.setText("Please choose a password.");
            fPass.requestFocusInWindow();
            ok = false;
        } else if (pass.length() < 6) {
            errPass.setText("Password must be at least 6 characters.");
            fPass.requestFocusInWindow();
            ok = false;
        } else {
            errPass.setText(" ");
        }

        if (!pass.equals(confirm)) {
            errConfirm.setText("Passwords do not match.");
            fConfirm.requestFocusInWindow();
            ok = false;
        } else {
            errConfirm.setText(" ");
        }

        if (!ok) {
            return;
        }

        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO users (full_name, username, email, password, registration_date) "
                        + "VALUES (?, ?, ?, ?, CURDATE())")) {
            pst.setString(1, name);
            pst.setString(2, username);
            pst.setString(3, email);
            pst.setString(4, pass);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Registration Successful! Please login.",
                    "Welcome", JOptionPane.INFORMATION_MESSAGE);
            new LoginUI().setVisible(true);
            dispose();
        } catch (Exception e) {
            String msg = e.getMessage() == null ? e.toString() : e.getMessage();
            if (msg.toLowerCase().contains("duplicate")) {
                errUser.setText("Username already exists. Choose another.");
            } else {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Registration failed: " + msg,
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Small helper that lays a growable field beside a fixed button. */
    private static class FormRowPacker {
        static void packRow(java.awt.Container row, java.awt.Component grow, java.awt.Component fixed) {
            java.awt.GridBagLayout g = new java.awt.GridBagLayout();
            row.setLayout(g);
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            row.add(grow, c);
            c.gridx = 1;
            c.weightx = 0;
            c.insets = new Insets(0, 10, 0, 0);
            row.add(fixed, c);
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new RegisterUI().setVisible(true));
    }
}