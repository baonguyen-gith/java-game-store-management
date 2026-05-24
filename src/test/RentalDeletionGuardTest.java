package otkhongluong.gamestoremanagement.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RentalDeletionGuardTest {
    private RentalService rentalService;

    @BeforeEach
    void setUp() {
        rentalService = new RentalService();
    }

    @Test
    @DisplayName("Kiểm thử cơ chế bảo vệ chặn hành vi xóa phiếu thuê khi chưa trả đĩa")
    void testDeleteRentalOrder_Failure_ActiveOrder() {
        int maPhieuThueActive = 2; 

        boolean isDeleted = rentalService.deleteRentalOrder(maPhieuThueActive);

        assertFalse(isDeleted, "Hệ thống bảo vệ bắt buộc phải chặn lại và trả về false");
    }
}