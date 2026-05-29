package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.service.RentalService;
import java.time.LocalDateTime;

public class RentalExtensionTest {

    public static void main(String[] args) {
        RentalService rentalService = new RentalService();

        System.out.println("=========================================");
        System.out.println("   BATT DAU KIEM THU GIA HAN PHIEU THUE  ");
        System.out.println("=========================================");

        // -------------------------------------------------------------
        // CA TEST 1: Gia hạn phiếu thuê thành công với tham số hợp lệ
        // -------------------------------------------------------------
        try {
            int maPhieuThueActive = 1;
            
            LocalDateTime ngayTraMoi = LocalDateTime.now().plusDays(3); 
            double phatTre = 0.0;    
            double phiGiaHan = 20000.0; 

            boolean isExtended = rentalService.extendRental(maPhieuThueActive, ngayTraMoi, phatTre, phiGiaHan);

            if (isExtended) {
                System.out.println("[ 🟢 PASSED ] Ca 1: Gia han phieu thue thanh cong, ngay hen tra moi da duoc cap nhat.");
            } else {
                System.out.println("[ 🔴 FAILED ] Ca 1 that bai. (Kiem tra xem DB da co phieu thue ma so " + maPhieuThueActive + " chua)");
            }

        } catch (Throwable e) {
            System.out.println("[ 🔴 FAILED ] Ca 1 dính ngoại lệ lỗi: " + e.getMessage());
        }

        System.out.println("=========================================");
    }
}