package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.model.User;
import otkhongluong.gamestoremanagement.service.AuthService;
import otkhongluong.gamestoremanagement.dao.UserDAO;

public class AuthFailureTest {

    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        AuthService authService = new AuthService(userDAO);

        System.out.println("=========================================");
        System.out.println("   BATT DAU CHAY KIEM THU DANG NHAP LUI  ");
        System.out.println("=========================================");

        // -------------------------------------------------------------
        // CA TEST 1: Tài khoản đúng nhưng mật khẩu bị sai
        // -------------------------------------------------------------
        try {
            String username = "admin";
            String password = "sai_mat_khau_123";
            
            User wrongPassUser = authService.login(username, password);

            if (wrongPassUser == null) {
                System.out.println("[ 🟢 PASSED ] Ca 1: He thong da chan thanh cong khi nhap sai mat khau.");
            } else {
                System.out.println("[ 🔴 FAILED ] Ca 1 loi: Nhap sai mat khau nhung van dang nhap duoc!");
            }
        } catch (Throwable e) {
            System.out.println("[ 🔴 FAILED ] Ca 1 dinh ngoai le: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // CA TEST 2: Tài khoản không hề tồn tại trong cơ sở dữ liệu
        // -------------------------------------------------------------
        try {
            String username = "user_khong_ton_tai";
            String password = "admin123";
            
            User nonExistUser = authService.login(username, password);

            if (nonExistUser == null) {
                System.out.println("[ 🟢 PASSED ] Ca 2: He thong da chan thanh cong khi tai khoan khong ton tai.");
            } else {
                System.out.println("[ 🔴 FAILED ] Ca 2 loi: Tai khoan ao nhung van bao dang nhap thanh cong!");
            }
        } catch (Throwable e) {
            System.out.println("[ 🔴 FAILED ] Ca 2 dinh ngoai le: " + e.getMessage());
        }

        System.out.println("=========================================");
    }
}