package otkhongluong.gamestoremanagement.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:sqlserver://localhost\\ANSQL;databaseName=qlgamee;encrypt=false;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "Tha2k6.*#2303";

    public static Connection getConnection() {

        Connection conn = null;

        try {
            // Load Driver (optional với JDBC mới nhưng cứ giữ)
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // ✅ PHẢI truyền USER + PASSWORD
            conn = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("✅ Connected SQL Server successfully!");

        } catch (Exception e) {
            System.out.println("❌ Database connection failed!");
            e.printStackTrace();
        }

        return conn;
    }

    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}