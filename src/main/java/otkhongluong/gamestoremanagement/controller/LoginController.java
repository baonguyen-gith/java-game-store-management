package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.service.AuthService;
import otkhongluong.gamestoremanagement.model.User;

public class LoginController {

    private AuthService authService;
    
    public LoginController() {
        this.authService = new AuthService();
    }

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    public User login(String username, String password) {
        return authService.login(username, password);
    }
    
    public boolean isAdmin(User user) {
        return authService.isAdmin(user);
    }

    public boolean isStaff(User user) {
        return authService.isStaff(user);
    }
}