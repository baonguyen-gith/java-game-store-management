package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.IUserDAO;
import otkhongluong.gamestoremanagement.model.User;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Service xác thực — chuẩn production.
 *
 * Xử lý 2 trường hợp trong giai đoạn migration:
 *   1. Password đã hash BCrypt ($2a$... / $2b$...) → dùng BCrypt.checkpw()
 *   2. Password vẫn còn plaintext (chưa migrate kịp) → so sánh trực tiếp
 *      và TỰ ĐỘNG hash lại ngay sau khi login thành công
 *
 * Sau khi PasswordMigration.run() chạy xong và toàn bộ DB đã hash,
 * nhánh plaintext sẽ không bao giờ được kích hoạt nữa — an toàn để giữ lại.
 */
public class AuthService {

    private final IUserDAO userDAO;

    public AuthService(IUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Xác thực đăng nhập.
     *
     * @return User nếu đúng, null nếu sai
     * @throws IllegalArgumentException nếu username hoặc password rỗng
     */
    public User login(String username, String password) {
        validateNotEmpty(username, "Tài khoản");
        validateNotEmpty(password, "Mật khẩu");

        User user = userDAO.findByUsername(username);
        if (user == null) return null;

        String stored = user.getPassword();

        if (isHashed(stored)) {
            // ── Trường hợp 1: đã hash → dùng BCrypt ──────────────
            return BCrypt.checkpw(password, stored) ? user : null;

        } else {
            // ── Trường hợp 2: vẫn plaintext → so sánh rồi tự migrate
            if (!password.equals(stored)) return null;

            // Tự động hash lại ngay sau khi login thành công
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));
            user.setPassword(hashed);
            userDAO.update(user);
            System.out.println("[AUTH] Auto-migrated password for: " + username);

            return user;
        }
    }

    /**
     * Hash password khi tạo user mới.
     * Luôn gọi method này thay vì BCrypt trực tiếp để dễ thay thuật toán sau.
     */
    public static String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password không được để trống!");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
    }

    /** @return true nếu user là Admin (MaRole == 1) */
    public boolean isAdmin(User user) {
        return user != null && user.getMaRole() == 1;
    }

    /** @return true nếu user là Staff (MaRole == 2) */
    public boolean isStaff(User user) {
        return user != null && user.getMaRole() == 2;
    }

    // ── Private helpers ───────────────────────────────────────

    /** Kiểm tra password đã được hash BCrypt chưa */
    private boolean isHashed(String password) {
        return password != null
            && (password.startsWith("$2a$") || password.startsWith("$2b$"))
            && password.length() == 60;
    }

    private void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " không được để trống!");
        }
    }
}