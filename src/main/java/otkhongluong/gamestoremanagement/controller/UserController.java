package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.dao.UserDAO;
import otkhongluong.gamestoremanagement.model.User;

import java.util.List;

public class UserController {

    private final UserDAO userDAO = new UserDAO();

    // Lấy toàn bộ danh sách user
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    // Thêm user — trả về thông báo lỗi hoặc null nếu thành công
    public String addUser(String username, String password, String roleStr) {
        if (username == null || username.trim().isEmpty()) return "Username không được để trống!";
        if (password == null || password.trim().isEmpty()) return "Password không được để trống!";

        int role;
        try {
            role = Integer.parseInt(roleStr.trim());
            if (role != 1 && role != 2) return "Role phải là 1 (Admin) hoặc 2 (Staff)!";
        } catch (NumberFormatException e) {
            return "Role không hợp lệ!";
        }

        if (userDAO.findByUsername(username.trim()) != null) {
            return "Username đã tồn tại!";
        }

        User user = new User(0, username.trim(), password.trim(), role);
        boolean ok = userDAO.insert(user);
        return ok ? null : "Thêm thất bại! Lỗi database.";
    }

    // Sửa user — trả về thông báo lỗi hoặc null nếu thành công
    public String updateUser(int maUser, String username, String password, String roleStr) {
        if (username == null || username.trim().isEmpty()) return "Username không được để trống!";
        if (password == null || password.trim().isEmpty()) return "Password không được để trống!";

        int role;
        try {
            role = Integer.parseInt(roleStr.trim());
            if (role != 1 && role != 2) return "Role phải là 1 (Admin) hoặc 2 (Staff)!";
        } catch (NumberFormatException e) {
            return "Role không hợp lệ!";
        }

        User existing = userDAO.findByUsername(username.trim());
        if (existing != null && existing.getMaUser() != maUser) {
            return "Username đã tồn tại!";
        }

        User user = new User(maUser, username.trim(), password.trim(), role);
        boolean ok = userDAO.update(user);
        return ok ? null : "Sửa thất bại! Lỗi database.";
    }

    // Xóa user — trả về thông báo lỗi hoặc null nếu thành công
    public String deleteUser(int maUser) {
        boolean ok = userDAO.delete(maUser);
        return ok ? null : "Xóa thất bại! Lỗi database.";
    }

    // Kiểm tra quyền Admin
    public boolean isAdmin(User user) {
        return user != null && user.getMaRole() == 1;
    }
}