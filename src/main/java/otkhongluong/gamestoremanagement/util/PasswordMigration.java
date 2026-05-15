package otkhongluong.gamestoremanagement.util;

import otkhongluong.gamestoremanagement.dao.UserDAO;
import otkhongluong.gamestoremanagement.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Chạy MỘT LẦN DUY NHẤT để hash toàn bộ password plaintext trong DB.
 *
 * Sau khi chạy xong → XÓA hoặc DISABLE class này.
 * Không bao giờ chạy lần 2 (sẽ hash password đã hash → không đăng nhập được).
 *
 * Cách chạy: tạo main tạm hoặc gọi PasswordMigration.run() từ Main một lần.
 */
public class PasswordMigration {

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        UserDAO userDAO = new UserDAO();
        List<User> users = userDAO.findAll();

        int success = 0, skip = 0, fail = 0;

        for (User user : users) {
            String pw = user.getPassword();

            // Bỏ qua nếu đã là BCrypt hash (bắt đầu bằng $2a$ hoặc $2b$)
            if (pw != null && (pw.startsWith("$2a$") || pw.startsWith("$2b$"))) {
                System.out.println("[SKIP] " + user.getUsername() + " — đã hash rồi");
                skip++;
                continue;
            }

            try {
                String hashed = BCrypt.hashpw(pw, BCrypt.gensalt(12));
                boolean ok = updatePassword(user.getMaUser(), hashed);
                if (ok) {
                    System.out.println("[OK]   " + user.getUsername() + " — hash thành công");
                    success++;
                } else {
                    System.out.println("[FAIL] " + user.getUsername() + " — update thất bại");
                    fail++;
                }
            } catch (Exception e) {
                System.out.println("[ERR]  " + user.getUsername() + " — " + e.getMessage());
                fail++;
            }
        }

        System.out.println("\n=== KẾT QUẢ MIGRATION ===");
        System.out.println("Thành công : " + success);
        System.out.println("Bỏ qua     : " + skip);
        System.out.println("Thất bại   : " + fail);
        System.out.println("=========================");
        System.out.println("Xong! Hãy xóa hoặc disable class PasswordMigration.");
    }

    private static boolean updatePassword(int maUser, String hashedPassword) {
        String sql = "UPDATE USERS SET Password = ? WHERE MaUser = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, maUser);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi update password: " + e.getMessage(), e);
        }
    }
}