package otkhongluong.gamestoremanagement.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import otkhongluong.gamestoremanagement.model.Customer;

class CustomerValidationTest {
    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    @Test
    @DisplayName("Kiểm thử chặn hành vi tạo hồ sơ khách hàng mới khi thiếu số CCCD")
    void testAddCustomer_Failure_MissingCCCD() {
        Customer badCustomer = new Customer();
        badCustomer.setTenKhachHang("Nguyễn Văn Khách");
        badCustomer.setSoDienThoai("0945678901");
        badCustomer.setCccd(""); // Cố tình để trống trường này

        boolean isValid = validationService.validateCustomer(badCustomer);
        
        assertFalse(isValid, "Hàm validate bắt buộc phải trả về false để ngăn lưu xuống database");
    }
}