package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.UserDAO;
import otkhongluong.gamestoremanagement.model.User;

public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public User login(String username, String password) {
        ValidationService.validateNotEmpty(username, "Tài khoản");
        ValidationService.validateNotEmpty(password, "Mật khẩu");

        User user = userDAO.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public boolean isAdmin(User user) {
        // Giả sử MaRole = 1 là Admin
        return user != null && user.getMaRole() == 1;
    }

    public boolean isStaff(User user) {
        // Giả sử MaRole = 2 là Staff
        return user != null && user.getMaRole() == 2;
    }
}
