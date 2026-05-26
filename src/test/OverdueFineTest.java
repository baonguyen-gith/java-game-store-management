package otkhongluong.gamestoremanagement.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

class OverdueFineTest {
    private RentalService rentalService;

    @BeforeEach
    void setUp() {
        rentalService = new RentalService();
    }

    @Test
    @DisplayName("Kiểm thử tính toán chính xác tiền phạt phát sinh do trả trễ hạn")
    void testCalculateOverdueFine_Logic() {
        LocalDate ngayHenTra = LocalDate.now().minusDays(5); // Quá hạn mất 5 ngày
        LocalDate ngayTraThucTe = LocalDate.now();
        double giaPhatMoiNgay = 20000.0; // 20.000đ một ngày

        double tienPhatThucTe = rentalService.calculateFine(ngayHenTra, ngayTraThucTe, giaPhatMoiNgay);

        assertEquals(100000.0, tienPhatThucTe, "Số tiền phạt tính ra bắt buộc phải là 100,000đ");
    }
}