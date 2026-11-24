import javax.swing.UIManager;

/**
 * Application entry point.
 * Applies the Nimbus look & feel, a friendlier default font,
 * then opens the Login form centered on screen.
 */
public class Main {

    public static void main(String[] args) {
        setupLookAndFeel();

        java.awt.EventQueue.invokeLater(() -> {
            new LoginUI().setVisible(true);
        });
    }

    private static void setupLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // fall back to the default look and feel
        }

        try {
            UIManager.put("defaultFont", new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        } catch (Exception e) {
            // ignore font override failures
        }
    }
}