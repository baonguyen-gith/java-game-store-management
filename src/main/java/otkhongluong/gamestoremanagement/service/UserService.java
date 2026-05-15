package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.UserDAO;
import otkhongluong.gamestoremanagement.model.User;

import java.util.List;

public class UserService {

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    /** Lấy toàn bộ danh sách user. */
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    /**
     * [MỚI] Lấy danh sách user kèm thông tin nhân viên (JOIN NHANVIEN).
     * Uỷ quyền thẳng xuống DAO vì đây là truy vấn đọc thuần tuý, không có nghiệp vụ.
     */
    public List<Object[]> getAllUsersWithEmployee() {
        return userDAO.findAllWithEmployee();
    }

    /** Thêm user mới. @throws IllegalArgumentException nếu dữ liệu không hợp lệ */
    public void addUser(String username, String password, String roleStr) {
        ValidationService.validateNotEmpty(username, "Username");
        ValidationService.validateNotEmpty(password, "Password");
        int role = parseRole(roleStr);
        if (userDAO.findByUsername(username.trim()) != null)
            throw new IllegalArgumentException("Username đã tồn tại!");
        User user = new User(0, username.trim(), password.trim(), role);
        if (!userDAO.insert(user))
            throw new RuntimeException("Thêm thất bại! Lỗi database.");
    }

    /** Sửa thông tin user. @throws IllegalArgumentException nếu dữ liệu không hợp lệ */
    public void updateUser(int maUser, String username, String password, String roleStr) {
        ValidationService.validateNotEmpty(username, "Username");
        ValidationService.validateNotEmpty(password, "Password");
        int role = parseRole(roleStr);
        User existing = userDAO.findByUsername(username.trim());
        if (existing != null && existing.getMaUser() != maUser)
            throw new IllegalArgumentException("Username đã tồn tại!");
        User user = new User(maUser, username.trim(), password.trim(), role);
        if (!userDAO.update(user))
            throw new RuntimeException("Sửa thất bại! Lỗi database.");
    }

    /** Xóa user theo ID. @throws RuntimeException nếu xóa thất bại */
    public void deleteUser(int maUser) {
        if (!userDAO.delete(maUser))
            throw new RuntimeException("Xóa thất bại! Lỗi database.");
    }

    // ─── Private helpers ───────────────────────────────────────────────────────
    private int parseRole(String roleStr) {
        ValidationService.validateNotEmpty(roleStr, "Role");
        try {
            int role = Integer.parseInt(roleStr.trim());
            if (role != 1 && role != 2)
                throw new IllegalArgumentException("Role phải là 1 (Admin) hoặc 2 (Staff)!");
            return role;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Role không hợp lệ! Phải là số nguyên.");
        }
    }
}