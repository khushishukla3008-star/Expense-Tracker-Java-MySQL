 import java.sql.Connection;
import java.sql.DriverManager;


 /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */




/**
 *
 * @author JK
 */
public class DBConnection {
    public static Connection getConnection() {
      Connection con = null;
      try {
          Class.forName("com.mysql.cj.jdbc.Driver");
          con = DriverManager.getConnection(
                  "jdbc:mysql://localhost:3306/expense_tracker",
                  "root",
                  "MySQL@123"
          );
      } catch (Exception e) {
            e.printStackTrace();
        }
      return con;
                  }
}

