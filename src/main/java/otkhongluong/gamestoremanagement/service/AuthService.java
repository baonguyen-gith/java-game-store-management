package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.UserDAO;
import otkhongluong.gamestoremanagement.model.User;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Service xác thực — chuẩn production.
 *
 * Xử lý 2 trường hợp trong giai đoạn migration:
 *   1. Password đã hash BCrypt ($2a$... / $2b$...) → dùng BCrypt.checkpw()
 *   2. Password vẫn còn plaintext (chưa migrate kịp) → so sánh trực tiếp
 *      và TỰ ĐỘNG hash lại ngay sau khi login thành công
 */
public class AuthService {

    // ✅ FIX: inject UserDAO, không phải User
    private final UserDAO userDAO;

    public AuthService(UserDAO userDAO) {
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

        // ✅ FIX: gọi đúng method trên UserDAO
        User user = userDAO.findByUsername(username);
        if (user == null) return null;

        String stored = user.getPassword();

        if (isHashed(stored)) {
            // Trường hợp 1: đã hash → dùng BCrypt
            return BCrypt.checkpw(password, stored) ? user : null;
        } else {
            // Trường hợp 2: vẫn plaintext → so sánh rồi tự migrate
            if (!password.equals(stored)) return null;

            String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));
            user.setPassword(hashed);
            // ✅ FIX: gọi đúng method trên UserDAO
            userDAO.update(user);
            System.out.println("[AUTH] Auto-migrated password for: " + username);
            return user;
        }
    }

    /**
     * Hash password khi tạo user mới.
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
    /** @return true nếu user là Manager (MaRole == 3) */
    public boolean isManager(User user) {
        return user != null && user.getMaRole() == 3;
    }

    // ── Private helpers ───────────────────────────────────────

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