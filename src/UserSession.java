/**
 * Holds the currently logged-in user's session.
 * All forms read UserSession.userId (or UserSession.username)
 * so every query/insert is scoped to the logged-in user only.
 */
public class UserSession {

    public static int userId = 0;
    public static String username = "";

    public static void setSession(int id, String name) {
        userId = id;
        username = name;
    }

    public static void clear() {
        userId = 0;
        username = "";
    }

    public static boolean isLoggedIn() {
        return userId > 0;
    }
}