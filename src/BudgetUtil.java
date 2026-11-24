import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * BudgetUtil — shared queries for the Budget Alert System.
 *
 * Reads monthly budgets from the "budgets" table and monthly expense totals
 * from "expenses" (dates are stored as yyyy-MM-dd strings). Used by the
 * Add Expense screen, Set Budget screen and the Dashboard.
 *
 * @author preetam-06
 */
public final class BudgetUtil {

    private BudgetUtil() {
    }

    /** Current month key in YYYY-MM form. */
    public static String currentMonth() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    /** Budget amount set for the given month (0 if none). */
    public static double budgetForMonth(int userId, String month) {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT budget_amount FROM budgets "
                        + "WHERE user_id = ? AND budget_month = ? ORDER BY budget_id DESC LIMIT 1")) {
            pst.setInt(1, userId);
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

    /** Total spent in the given month (YYYY-MM). */
    public static double spentForMonth(int userId, String month) {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT COALESCE(SUM(amount),0) FROM expenses "
                        + "WHERE user_id = ? AND LEFT(date, 7) = ?")) {
            pst.setInt(1, userId);
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

    /** All-time total expenses for the user. */
    public static double totalExpenses(int userId) {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT COALESCE(SUM(amount),0) FROM expenses WHERE user_id = ?")) {
            pst.setInt(1, userId);
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

    /** All-time total income for the user. */
    public static double totalIncome(int userId) {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT COALESCE(SUM(amount),0) FROM income WHERE user_id = ?")) {
            pst.setInt(1, userId);
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

    /** Usage fraction spent of the monthly budget (0 if no budget). */
    public static double usageFraction(double spent, double budget) {
        if (budget <= 0) {
            return 0;
        }
        return spent / budget;
    }

    /** Human-readable alert message for the given spent/budget totals. */
    public static String alertMessage(double spent, double budget) {
        if (budget <= 0) {
            return "No monthly budget set yet. Visit Set Budget.";
        }
        if (spent > budget) {
            return "You have exceeded your monthly budget by ₹ "
                    + String.format("%,.0f", spent - budget);
        }
        double pct = (spent / budget) * 100;
        if (pct >= 100) {
            return "Budget Limit Reached!";
        }
        if (pct >= 80) {
            return "Warning: You have used "
                    + String.format("%.0f", pct) + "% of your monthly budget.";
        }
        return String.format("On track — Used %.0f%% · Remaining ₹ %,.0f",
                pct, budget - spent);
    }

    /** Status colour matching the alert severity. */
    public static Color alertColor(double spent, double budget) {
        if (budget <= 0) {
            return UITheme.MUTED;
        }
        if (spent > budget) {
            return UITheme.RED;
        }
        if (spent / budget >= 0.8) {
            return UITheme.GOLD;
        }
        return UITheme.GREEN;
    }
}