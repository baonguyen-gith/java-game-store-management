package otkhongluong.gamestoremanagement.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import otkhongluong.gamestoremanagement.model.User;

class AuthFailureTest {
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
    }

    @Test
    @DisplayName("Kiểm thử đăng nhập thất bại khi sai mật khẩu hoặc tài khoản không tồn tại")
    void testLogin_Failure_Scenarios() {
        // Trường hợp 1: Tài khoản đúng nhưng mật khẩu bị sai
        User wrongPassUser = authService.login("admin", "sai_mat_khau_123");
        assertNull(wrongPassUser, "Hệ thống phải trả về null khi nhập sai mật khẩu");

        // Trường hợp 2: Tài khoản không hề tồn tại trong cơ sở dữ liệu
        User nonExistUser = authService.login("user_khong_ton_tai", "admin123");
        assertNull(nonExistUser, "Hệ thống phải trả về null khi tài khoản không tồn tại");
    }
}