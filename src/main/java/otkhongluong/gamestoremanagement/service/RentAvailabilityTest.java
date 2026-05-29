package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.model.RentalOrder;
import otkhongluong.gamestoremanagement.model.CTPhieuThue;
import otkhongluong.gamestoremanagement.service.RentalService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RentAvailabilityTest {

    public static void main(String[] args) {
        RentalService rentalService = new RentalService();

        System.out.println("=========================================");
        System.out.println("   BATT DAU KIEM THU TRANG THAI DIA      ");
        System.out.println("=========================================");

        // -------------------------------------------------------------
        // CA TEST 1: Chặn tạo phiếu thuê khi đĩa đã có trạng thái 'DangThue' hoặc 'DaBan'
        // -------------------------------------------------------------
        try {
            RentalOrder badOrder = new RentalOrder();
            badOrder.setMaKH(1);
            badOrder.setMaNV(1);
            badOrder.setNgayThue(LocalDateTime.now());
            badOrder.setNgayTraDuKien(LocalDateTime.now().plusDays(2));
            badOrder.setTienCoc(150000.0);

            List<CTPhieuThue> danhSachChiTiet = new ArrayList<>();
            danhSachChiTiet.add(new CTPhieuThue(2, "Cyberpunk 2077 CD", 40000.0, "DangThue"));
            badOrder.setDanhSachChiTiet(danhSachChiTiet);

            boolean result = rentalService.createPhieuThue(badOrder);

            if (!result) {
                System.out.println("[ 🟢 PASSED ] Ca 1: He thong chan dung logic, tu choi cho thue dia dang ban/dang thue.");
            } else {
                System.out.println("[ 🔴 FAILED ] Ca 1 loi: Dia dang ban hoac dang thue ma van tao duoc phieu!");
            }

        } catch (Throwable e) {
            System.out.println("[ 🔴 FAILED ] Ca 1 dinh ngoai le loi: " + e.getMessage());
        }

        System.out.println("=========================================");
    }
}