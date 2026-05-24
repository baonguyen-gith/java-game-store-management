package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.service.UserService;
import java.util.List;
import otkhongluong.gamestoremanagement.model.User;

public class UserController {

    private final UserService service = new UserService();

    public List<Object[]> getAllUsersWithEmployee() {
        return service.getAllUsersWithEmployee();
    }

    public List<Object[]> getNhanVienDropdown(int maNVEditDang) {
        return service.getNhanVienDropdown(maNVEditDang);
    }

    /** @return null nếu thành công, chuỗi lỗi nếu thất bại */
    public String addUser(String username, String password, String roleStr, int maNV) {
        try {
            service.addUser(username, password, roleStr, maNV);
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * @param password Rỗng = giữ nguyên password cũ.
     * @return null nếu thành công, chuỗi lỗi nếu thất bại
     */
    public String updateUser(int maUser, String username,
                             String password, String roleStr, int maNV) {
        try {
            service.updateUser(maUser, username, password, roleStr, maNV);
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String deleteUser(int maUser) {
        try {
            service.deleteUser(maUser);
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
    
    public User getUserById(int maUser) {
        return service.getUserById(maUser);
    }
}