package otkhongluong.gamestoremanagement.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RentalExtensionTest {
    private RentalService rentalService;

    @BeforeEach
    void setUp() {
        rentalService = new RentalService();
    }

    @Test
    @DisplayName("Kiểm thử chức năng gia hạn thời gian phiếu thuê đang hoạt động")
    void testExtendRentalOrder_Success_Scenario() {
        int maPhieuThueActive = 1;
        int soNgayGiaHanThem = 3; // Xin giữ đĩa thêm 3 ngày nữa

        boolean isExtended = rentalService.extendRentalPeriod(maPhieuThueActive, soNgayGiaHanThem);

        assertTrue(isExtended, "Hệ thống phải thực thi ghi nhận ngày hẹn trả mới thành công");
    }
}