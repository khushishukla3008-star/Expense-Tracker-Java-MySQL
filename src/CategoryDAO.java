import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * CategoryDAO — JDBC data access for the categories table.
 *
 * All queries use PreparedStatement to prevent SQL injection.
 * Duplicate names are prevented in two ways: an application-level
 * exists() check (clean error message) AND a database UNIQUE index.
 *
 * @author preetam-06
 */
public class CategoryDAO {

    /** Inserts a category. Returns the auto-generated category id (or -1). */
    public int addCategory(Category category) throws SQLException {
        if (exists(category.getCategoryName(), -1)) {
            throw new SQLException("Duplicate category '" + category.getCategoryName()
                    + "' already exists.");
        }
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO categories(category_name) VALUES(?)",
                        PreparedStatement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, category.getCategoryName());
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    /** Updates the name of the category with the given id. */
    public boolean updateCategory(Category category) throws SQLException {
        if (category.getCategoryId() <= 0) {
            return false;
        }
        if (exists(category.getCategoryName(), category.getCategoryId())) {
            throw new SQLException("Duplicate category '" + category.getCategoryName()
                    + "' already exists.");
        }
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "UPDATE categories SET category_name = ? WHERE id = ?")) {
            pst.setString(1, category.getCategoryName());
            pst.setInt(2, category.getCategoryId());
            return pst.executeUpdate() > 0;
        }
    }

    /** Deletes the category with the given id. */
    public boolean deleteCategory(int categoryId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "DELETE FROM categories WHERE id = ?")) {
            pst.setInt(1, categoryId);
            return pst.executeUpdate() > 0;
        }
    }

    /** Returns all categories ordered by name. */
    public List<Category> getAllCategories() throws SQLException {
        List<Category> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT id, category_name FROM categories ORDER BY category_name");
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(new Category(rs.getInt("id"), rs.getString("category_name")));
            }
        }
        return list;
    }

    /** Returns categories whose name contains the given keyword. */
    public List<Category> searchCategory(String keyword) throws SQLException {
        List<Category> list = new ArrayList<>();
        String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT id, category_name FROM categories "
                        + "WHERE category_name LIKE ? ORDER BY category_name")) {
            pst.setString(1, like);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(new Category(rs.getInt("id"), rs.getString("category_name")));
                }
            }
        }
        return list;
    }

    /**
     * Returns true when a category with the given name exists.
     *
     * @param name       the category name to check
     * @param excludeId  ignore this id (used when updating a row itself) or -1
     */
    public boolean exists(String name, int excludeId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT COUNT(*) FROM categories WHERE category_name = ? AND id != ?")) {
            pst.setString(1, name == null ? "" : name.trim());
            pst.setInt(2, excludeId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Convenience overload: any duplicate (no id excluded). */
    public boolean exists(String name) throws SQLException {
        return exists(name, -1);
    }
}