package otkhongluong.gamestoremanagement.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import otkhongluong.gamestoremanagement.model.RentalOrder;
import java.time.LocalDate;

class RentAvailabilityTest {
    private RentalService rentalService;

    @BeforeEach
    void setUp() {
        rentalService = new RentalService();
    }

    @Test
    @DisplayName("Kiểm thử chặn nghiệp vụ tạo phiếu thuê khi đĩa không ở trạng thái sẵn sàng")
    void testCreateRentalOrder_Failure_DiscNotReady() {
        // Chuẩn bị dữ liệu phiếu thuê giả định liên kết với mã đĩa bận (ID = 2)
        RentalOrder badOrder = new RentalOrder();
        badOrder.setMaKhachHang(1);
        badOrder.setMaNhanVien(1);
        badOrder.setMaDia(2); 
        badOrder.setNgayThue(LocalDate.now());
        badOrder.setNgayHenTra(LocalDate.now().plusDays(2));

        boolean result = rentalService.createRentalOrder(badOrder);
        
        assertFalse(result, "Hệ thống bắt buộc phải trả về false khi đĩa đang bận");
    }
}