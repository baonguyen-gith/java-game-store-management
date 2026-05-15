package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.User;
import otkhongluong.gamestoremanagement.service.UserService;

import java.util.List;

/**
 * Controller quản lý User.
 *
 * ✅ Chỉ gọi UserService — không chứa validation hay business logic.
 * ✅ Không còn isAdmin() — logic đó thuộc AuthService (đã có sẵn).
 * ✅ Bắt exception từ Service rồi trả về String lỗi cho View.
 *    View chỉ gọi controller, không biết gì về Service hay DAO.
 *
 * Pattern trả lỗi: trả null = thành công, trả String = thông báo lỗi.
 * View hiển thị thông báo lỗi này bằng JOptionPane hoặc label.
 */
public class UserController {

    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    /** Lấy toàn bộ danh sách user để hiển thị lên bảng. */
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Thêm user mới.
     *
     * @return null nếu thành công, String thông báo lỗi nếu thất bại
     */
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

    /**
     * Xóa user theo ID.
     *
     * @return null nếu thành công, String thông báo lỗi nếu thất bại
     */
    public String deleteUser(int maUser) {
        try {
            userService.deleteUser(maUser);
            return null;
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }
}