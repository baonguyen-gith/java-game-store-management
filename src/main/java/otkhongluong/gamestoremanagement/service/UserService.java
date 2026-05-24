package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.UserDAO;
import otkhongluong.gamestoremanagement.model.User;
import java.util.List;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public List<Object[]> getAllUsersWithEmployee() {
        return userDAO.findAllWithEmployee();
    }

    /** Delegate xuống DAO — nghiệp vụ lọc nằm ở DAO vì cần JOIN. */
    public List<Object[]> getNhanVienDropdown(int maNVEditDang) {
        return userDAO.findNhanVienDropdown(maNVEditDang);
    }

    // ── Thêm ──────────────────────────────────────────────────────────────────

    public void addUser(String username, String password, String roleStr, int maNV) {
        ValidationService.validateNotEmpty(username, "Username");
        ValidationService.validateNotEmpty(password, "Password");
        int role = parseRole(roleStr);

        if (userDAO.findByUsername(username.trim()) != null)
            throw new IllegalArgumentException("Username đã tồn tại!");

        User user = new User(0, username.trim(), password.trim(), role, maNV);
        if (!userDAO.insert(user))
            throw new RuntimeException("Thêm thất bại! Lỗi database.");
    }

    // ── Sửa ──────────────────────────────────────────────────────────────────

    /**
     * @param password Rỗng = giữ nguyên password cũ trong DB.
     */
    public void updateUser(int maUser, String username,
                           String password, String roleStr, int maNV) {
        ValidationService.validateNotEmpty(username, "Username");
        // Không validate password rỗng — rỗng nghĩa là giữ nguyên
        int role = parseRole(roleStr);

        User existing = userDAO.findByUsername(username.trim());
        if (existing != null && existing.getMaUser() != maUser)
            throw new IllegalArgumentException("Username đã tồn tại!");

        // Nếu password rỗng thì lấy password cũ từ DB
        String finalPassword = password.isEmpty()
            ? userDAO.findById(maUser).getPassword()
            : password.trim();

        User user = new User(maUser, username.trim(), finalPassword, role, maNV);
        if (!userDAO.update(user))
            throw new RuntimeException("Sửa thất bại! Lỗi database.");
    }

    // ── Xóa ──────────────────────────────────────────────────────────────────

    public void deleteUser(int maUser) {
        if (!userDAO.delete(maUser))
            throw new RuntimeException("Xóa thất bại! Lỗi database.");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private int parseRole(String roleStr) {
        ValidationService.validateNotEmpty(roleStr, "Role");
        try {
            int role = Integer.parseInt(roleStr.trim());
            if (role < 1 || role > 3)
                throw new IllegalArgumentException("Role phải là 1, 2 hoặc 3!");
            return role;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Role không hợp lệ!");
        }
    }
    
    public User getUserById(int maUser) {
        return userDAO.findById(maUser);
    }
}