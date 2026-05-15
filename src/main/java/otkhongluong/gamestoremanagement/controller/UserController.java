package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.User;
import otkhongluong.gamestoremanagement.service.UserService;

import java.util.List;

public class UserController {

    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    /** Lấy toàn bộ danh sách user (không kèm tên NV) — dùng nội bộ nếu cần. */
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * [MỚI] Lấy danh sách user kèm thông tin nhân viên liên kết.
     * Mỗi Object[] gồm: [MaUser, Username, Role, MaNVFormatted ("NV1"/""), HoTen ("—" nếu chưa gắn)]
     */
    public List<Object[]> getAllUsersWithEmployee() {
        try {
            return userService.getAllUsersWithEmployee();
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    /** @return null nếu thành công, String thông báo lỗi nếu thất bại */
    public String addUser(String username, String password, String roleStr) {
        try {
            userService.addUser(username, password, roleStr);
            return null;
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    public String updateUser(int maUser, String username, String password, String roleStr) {
        try {
            userService.updateUser(maUser, username, password, roleStr);
            return null;
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    /** @return null nếu thành công, String thông báo lỗi nếu thất bại */
    public String deleteUser(int maUser) {
        try {
            userService.deleteUser(maUser);
            return null;
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }
}