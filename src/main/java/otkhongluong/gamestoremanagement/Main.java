package otkhongluong.gamestoremanagement;

public class Main {
    public static void main(String[] args) {
        // ✅ AppNavigator là entry point duy nhất của app.
        //    Nó tạo LoginController(navigator) và LoginView đúng thứ tự bên trong.
        //    Main không cần biết AuthService, LoginController hay LoginView.
        javax.swing.SwingUtilities.invokeLater(
            () -> new AppNavigator().goToLogin()
        );
    }
}