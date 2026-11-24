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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * ProfileUI — user profile page.
 *
 * Shows the logged-in user's name, username, email and registration date.
 * Lets the user edit the name/email, and change the password
 * (current password verified before the change).
 *
 * @author preetam-06
 */
public class ProfileUI extends javax.swing.JFrame {

    private JPanel jSidebar;
    private UITheme.RoundedField fName;
    private UITheme.RoundedField fEmail;
    private JTextField fUser;
    private JTextField fRegDate;
    private UITheme.RoundedPasswordField fCurrent;
    private UITheme.RoundedPasswordField fNewPass;
    private UITheme.RoundedPasswordField fConfirm;

    public ProfileUI() {
        buildUI();
        setTitle("My Profile - Expense Tracker");
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);
        loadProfile();
    }

    private void buildUI() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jSidebar = UITheme.createSidebar(this, 1);

        JPanel content = new JPanel();
        content.setBackground(UITheme.CONTENT_BG);
        content.setLayout(new BorderLayout());

        JPanel header = new JPanel(new java.awt.GridLayout(2, 1));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(24, 22, 0, 22));
        JLabel title = new JLabel("My Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UITheme.TEXT);
        JLabel sub = new JLabel("View and update your account details");
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        sub.setForeground(UITheme.MUTED);
        header.add(title);
        header.add(sub);

        /* ---------- Profile card ---------- */
        UITheme.RoundedPanel profileCard = new UITheme.RoundedPanel();
        profileCard.setArc(16);
        GridBagLayout gbl = new GridBagLayout();
        profileCard.setLayout(gbl);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 20, 0, 20);

        c.gridy = 0;
        profileCard.add(fieldLabel("Full Name"), c);
        c.gridy = 1;
        c.insets = new Insets(4, 20, 0, 20);
        fName = new UITheme.RoundedField();
        fName.setIconChar("N");
        profileCard.add(fName, c);

        c.gridy = 2;
        c.insets = new Insets(12, 20, 0, 20);
        profileCard.add(fieldLabel("Email"), c);
        c.gridy = 3;
        c.insets = new Insets(4, 20, 0, 20);
        fEmail = new UITheme.RoundedField();
        fEmail.setIconChar("E");
        profileCard.add(fEmail, c);

        c.gridy = 4;
        c.insets = new Insets(12, 20, 0, 20);
        profileCard.add(fieldLabel("Username (cannot be changed)"), c);
        c.gridy = 5;
        c.insets = new Insets(4, 20, 0, 20);
        fUser = readOnlyField();
        profileCard.add(fUser, c);

        c.gridy = 6;
        c.insets = new Insets(12, 20, 0, 20);
        profileCard.add(fieldLabel("Registration Date"), c);
        c.gridy = 7;
        c.insets = new Insets(4, 20, 0, 20);
        fRegDate = readOnlyField();
        profileCard.add(fRegDate, c);

        UITheme.RoundedButton save = new UITheme.RoundedButton("Save Changes");
        save.setBg(UITheme.TEAL);
        save.setHoverColor(new Color(9, 125, 136));
        save.addActionListener(e -> saveProfile());
        c.gridy = 8;
        c.insets = new Insets(20, 20, 20, 20);
        profileCard.add(save, c);

        /* ---------- Password card ---------- */
        UITheme.RoundedPanel passCard = new UITheme.RoundedPanel();
        passCard.setArc(16);
        passCard.setLayout(gbl);
        GridBagConstraints p = new GridBagConstraints();
        p.gridx = 0;
        p.weightx = 1;
        p.fill = GridBagConstraints.HORIZONTAL;
        p.insets = new Insets(8, 20, 0, 20);

        p.gridy = 0;
        passCard.add(fieldLabel("Current Password"), p);
        p.gridy = 1;
        p.insets = new Insets(4, 20, 0, 20);
        fCurrent = new UITheme.RoundedPasswordField();
        fCurrent.setIconChar("•");
        passCard.add(fCurrent, p);

        p.gridy = 2;
        p.insets = new Insets(12, 20, 0, 20);
        passCard.add(fieldLabel("New Password"), p);
        p.gridy = 3;
        p.insets = new Insets(4, 20, 0, 20);
        fNewPass = new UITheme.RoundedPasswordField();
        fNewPass.setIconChar("•");
        passCard.add(fNewPass, p);

        p.gridy = 4;
        p.insets = new Insets(12, 20, 0, 20);
        passCard.add(fieldLabel("Confirm New Password"), p);
        p.gridy = 5;
        p.insets = new Insets(4, 20, 0, 20);
        fConfirm = new UITheme.RoundedPasswordField();
        fConfirm.setIconChar("•");
        fConfirm.addActionListener(e -> changePassword());
        passCard.add(fConfirm, p);

        UITheme.RoundedButton change = new UITheme.RoundedButton("Change Password");
        change.setBg(UITheme.BLUE);
        change.setHoverColor(UITheme.BLUE_DARK);
        change.addActionListener(e -> changePassword());
        p.gridy = 6;
        p.insets = new Insets(20, 20, 20, 20);
        passCard.add(change, p);

        /* ---------- Assemble ---------- */
        JPanel cards = new JPanel();
        cards.setOpaque(false);
        cards.setLayout(new java.awt.GridLayout(1, 2, 18, 0));
        cards.setBorder(BorderFactory.createEmptyBorder(16, 22, 22, 22));
        cards.add(profileCard);
        cards.add(passCard);

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

    private static JTextField readOnlyField() {
        JTextField f = new JTextField();
        f.setEditable(false);
        f.setBackground(new Color(233, 238, 246));
        f.setForeground(new Color(90, 105, 125));
        f.setFont(UITheme.F_BODY);
        f.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        f.setPreferredSize(new Dimension(300, 38));
        return f;
    }

    /* ===================== DATA ===================== */

    private void loadProfile() {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT full_name, username, email, registration_date FROM users WHERE user_id = ?")) {
            pst.setInt(1, UserSession.userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    fName.setText(str(rs.getString("full_name")));
                    fUser.setText(str(rs.getString("username")));
                    fEmail.setText(str(rs.getString("email")));
                    java.sql.Date reg = rs.getDate("registration_date");
                    fRegDate.setText(reg == null ? "" : reg.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveProfile() {
        String name = fName.getText().trim();
        String email = fEmail.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full name cannot be empty.",
                    "Profile", JOptionPane.WARNING_MESSAGE);
            fName.requestFocusInWindow();
            return;
        }
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email cannot be empty.",
                    "Profile", JOptionPane.WARNING_MESSAGE);
            fEmail.requestFocusInWindow();
            return;
        }
        if (!email.matches("^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.",
                    "Profile", JOptionPane.WARNING_MESSAGE);
            fEmail.requestFocusInWindow();
            return;
        }
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "UPDATE users SET full_name = ?, email = ? WHERE user_id = ?")) {
            pst.setString(1, name);
            pst.setString(2, email);
            pst.setInt(3, UserSession.userId);
            pst.executeUpdate();
            UserSession.username = name; // dashboard greeting shows the new name
            JOptionPane.showMessageDialog(this, "Profile updated successfully.",
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not save profile: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changePassword() {
        String current = new String(fCurrent.getPassword());
        String next = new String(fNewPass.getPassword());
        String confirm = new String(fConfirm.getPassword());

        if (current.isEmpty() || next.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all three password fields.",
                    "Change Password", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (next.length() < 6) {
            JOptionPane.showMessageDialog(this, "New password must be at least 6 characters.",
                    "Change Password", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!next.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "New password and confirmation do not match.",
                    "Change Password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String stored = "";
            try (PreparedStatement p = con.prepareStatement(
                    "SELECT password FROM users WHERE user_id = ?")) {
                p.setInt(1, UserSession.userId);
                try (ResultSet rs = p.executeQuery()) {
                    if (rs.next()) {
                        stored = str(rs.getString("password"));
                    }
                }
            }
            if (!stored.equals(current)) {
                JOptionPane.showMessageDialog(this, "Current password is incorrect.",
                        "Change Password", JOptionPane.ERROR_MESSAGE);
                fCurrent.requestFocusInWindow();
                return;
            }
            try (PreparedStatement up = con.prepareStatement(
                    "UPDATE users SET password = ? WHERE user_id = ?")) {
                up.setString(1, next);
                up.setInt(2, UserSession.userId);
                up.executeUpdate();
            }
            fCurrent.setText("");
            fNewPass.setText("");
            fConfirm.setText("");
            JOptionPane.showMessageDialog(this, "Password changed successfully.",
                    "Changed", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not change password: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new ProfileUI().setVisible(true));
    }
}