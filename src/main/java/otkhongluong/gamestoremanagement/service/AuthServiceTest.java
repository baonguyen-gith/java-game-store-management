package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.model.User;
import otkhongluong.gamestoremanagement.service.AuthService;
import otkhongluong.gamestoremanagement.dao.UserDAO; 

public class AuthServiceTest {

    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();    
        AuthService authService = new AuthService(userDAO);
        
        System.out.println("=========================================");
        System.out.println("   BẮT ĐẦU CHẠY KIỂM THỬ ĐĂNG NHẬP       ");
        System.out.println("=========================================");

        try {
            String username = "an";
            String password = "123"; 
            User loggedInUser = authService.login(username, password);

            if (loggedInUser != null) {
                System.out.println("[DEBUG] Username thuc te lay tu DB: " + loggedInUser.getUsername());
            } else {
                System.out.println("[DEBUG] Ham login dang tra ve NULL (Khong tim thay user hoac sai pass)!");
            }

            if (loggedInUser != null && "an".equals(loggedInUser.getUsername())) {
                System.out.println("[ 🟢 PASSED ] Ca 1: Đăng nhập thành công hợp lệ.");
            } else {
                System.out.println("[ 🔴 FAILED ] Ca 1: Sai thông tin đối chiếu hoặc trả về null.");
            }
        } catch (Throwable e) {
            System.out.println("[ 🔴 FAILED ] Ca 1 dính ngoại lệ: " + e.getMessage());
        }

        try {
            String username = "an";
            String password = "mat_khau_bi_go_sai_123";
            User loggedInUser = authService.login(username, password);

            if (loggedInUser == null) {
                System.out.println("[ 🟢 PASSED ] Ca 2: Hệ thống đã chặn thành công mật khẩu sai.");
            } else {
                System.out.println("[ 🔴 FAILED ] Ca 2: Sai mật khẩu nhưng vẫn đăng nhập được!");
            }
        } catch (Throwable e) {
            System.out.println("[ 🔴 FAILED ] Ca 2 lỗi: " + e.getMessage());
        }

        try {
            String username = "tai_khoan_ao_grape_999";
            String password = "any_password";
            User loggedInUser = authService.login(username, password);

            if (loggedInUser == null) {
                System.out.println("[ 🟢 PASSED ] Ca 3: Hệ thống đã chặn thành công tài khoản ảo.");
            } else {
                System.out.println("[ 🔴 FAILED ] Ca 3: Tài khoản không tồn tại nhưng vẫn đăng nhập được!");
            }
        } catch (Throwable e) {
            System.out.println("[ 🔴 FAILED ] Ca 3 lỗi: " + e.getMessage());
        }

        System.out.println("=========================================");
    }
}