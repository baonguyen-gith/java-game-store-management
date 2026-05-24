package otkhongluong.gamestoremanagement.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import otkhongluong.gamestoremanagement.model.RentalOrder;
import java.time.LocalDate;

class RentalServiceTest {
    private RentalService rentalService;

    @BeforeEach
    void setUp() {
        rentalService = new RentalService();
    }

    @Test
    void testCreateRentalOrder_Success() {
        // Given: Thiết lập thông tin phiếu thuê đĩa game hợp lệ
        RentalOrder order = new RentalOrder();
        order.setMaKhachHang(1);
        order.setMaNhanVien(1);
        order.setMaDia(5); // Giả định đĩa mã số 5 đang rảnh (Sẵn sàng)
        order.setNgayThue(LocalDate.now());
        order.setNgayHenTra(LocalDate.now().plusDays(3)); // Thuê trong 3 ngày
        order.setTienCoc(200000.0);

        // When: Thực hiện lưu phiếu thuê
        boolean isRented = rentalService.createRentalOrder(order);

        // Then: Hệ thống phải chấp nhận và kích hoạt trạng thái cho thuê
        assertTrue(isRented, "Phiếu thuê với đĩa hợp lệ phải được tạo thành công");
    }
}
