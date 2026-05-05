package otkhongluong.gamestoremanagement;

import otkhongluong.gamestoremanagement.view.LoginView;
import otkhongluong.gamestoremanagement.controller.LoginController;
import otkhongluong.gamestoremanagement.service.AuthService;

public class Main {
    public static void main(String[] args) {

        AuthService authService = new AuthService();
        LoginController controller = new LoginController(authService);

        LoginView view = new LoginView(controller);
        view.setVisible(true);
    }
}