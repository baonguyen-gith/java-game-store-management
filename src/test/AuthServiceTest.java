package otkhongluong.gamestoremanagement.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import otkhongluong.gamestoremanagement.model.User;

class AuthServiceTest {
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
    }

    @Test
    @DisplayName("Chức năng cơ bản: Đăng nhập thành công")
    void testLogin_Success() {
        String username = "admin";
        String password = "admin123"; 

        User loggedInUser = authService.login(username, password);

        assertNotNull(loggedInUser, "Tài khoản đúng thì kết quả không được trả về null");
        assertEquals("admin", loggedInUser.getUsername(), "Username trong đối tượng trả về phải trùng khớp");
    }

    @Test
    @DisplayName("Chức năng bổ sung: Từ chối khi sai mật khẩu")
    void testLogin_Failure_WrongPassword() {
        // 1. Given: Tài khoản có thật nhưng mật khẩu bị cố tình nhập sai
        String username = "admin";
        String password = "mat_khau_bi_go_sai_123";

        // 2. When: Gọi hàm xử lý đăng nhập
        User loggedInUser = authService.login(username, password);

        // 3. Then: Kỳ vọng hệ thống trả về null (Đăng nhập thất bại)
        assertNull(loggedInUser, "Nhập sai mật khẩu thì hệ thống bắt buộc phải trả về null");
    }

    @Test
    @DisplayName("Chức năng bổ sung: Từ chối khi tài khoản không tồn tại")
    void testLogin_Failure_UserNotFound() {
        // 1. Given: Tài khoản hoàn toàn bịa ra, không có dưới Database
        String username = "tai_khoan_ao_grape_999";
        String password = "any_password";

        // 2. When: Gọi hàm xử lý đăng nhập
        User loggedInUser = authService.login(username, password);

        // 3. Then: Kỳ vọng hệ thống trả về null công bằng
        assertNull(loggedInUser, "Tài khoản không tồn tại thì hệ thống phải chặn lại và trả về null");
    }
}