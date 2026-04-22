package otkhongluong.gamestoremanagement.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String HOST = "localhost";
    private static final String PORT = "1521";
    private static final String SID = "xe";
    private static final String USER = "system"; 
    private static final String PASS = "your_password";
    
    private static final String URL = "jdbc:oracle:thin:@" + HOST + ":" + PORT + ":" + SID;

    public static Connection getConnection() throws SQLException {
        Connection conn = null;
        try {

            Class.forName("oracle.jdbc.driver.OracleDriver");
            
            conn = DriverManager.getConnection(URL, USER, PASS);
            
            if (conn != null) {
                System.out.println("Kết nối Database thành công!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Lỗi: Không tìm thấy thư viện Driver Oracle JDBC!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Lỗi: Không thể kết nối tới Database. Kiểm tra URL, User hoặc Pass!");
            throw e;
        }
        return conn;
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}