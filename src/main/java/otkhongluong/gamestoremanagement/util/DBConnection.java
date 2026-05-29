package otkhongluong.gamestoremanagement.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

// 1. Sửa URL: Bỏ tên Instance \\ANSQL, thay bằng cổng :1443
// Sửa lại URL như sau:
private static final String URL = "jdbc:sqlserver://127.0.0.1:1443;databaseName=qlgamee;encrypt=false;trustServerCertificate=true;loginTimeout=10";

// 2. Kiểm tra lại tên User (Đảm bảo khớp 100% với SSMS)
private static final String USER = "DOANJAVA"; // Hoặc "DOẠNAVA" tùy máy bạn
private static final String PASSWORD = "123";

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
