package otkhongluong.gamestoremanagement.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoyaltyPointTest {
    private PointService pointService;

    @BeforeEach
    void setUp() {
        pointService = new PointService();
    }

    @Test
    @DisplayName("Kiểm thử tự động cộng điểm thưởng thành viên chính xác")
    void testCalculateLoyaltyPoints_Success() {
        double tongTienThanhToan = 500000.0; // Giao dịch trị giá 500.000đ

        int diemThuongTichLuy = pointService.calculateEarnedPoints(tongTienThanhToan);

        assertEquals(5, diemThuongTichLuy, "Đơn hàng 500k với tỷ lệ 1% phải tích được đúng 5 điểm");
    }   
}