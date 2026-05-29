package otkhongluong.gamestoremanagement.service;

import java.time.LocalDateTime;

public class ReturnGameTest {

    public static void main(String[] args) {
        RentalService rentalService = new RentalService();

        System.out.println("=========================================");
        System.out.println("   BAT DAU CHAY KIEM THU TRA GAME       ");
        System.out.println("=========================================");

        // -------------------------------------------------------------
        // CA TEST 1: Trả game đúng hạn với mã phiếu thuê hợp lệ
        // -------------------------------------------------------------
        try {

            int maPhieuThue = 1;
            LocalDateTime ngayTraThucTe = LocalDateTime.now();

            boolean isReturned = rentalService.returnCD(maPhieuThue, ngayTraThucTe);

            if (isReturned) {
                System.out.println("[ 🟢 PASSED ] Ca 1: Tra game dung han thanh cong, he thong tat toan mượt mà.");
            } else {
                System.out.println("[ 🔴 FAILED ] Ca 1 that bai. (Kiem tra xem DB da co phieu thue mã so " + maPhieuThue + " chua)");
            }
        } catch (Throwable e) {
            System.out.println("[ 🔴 FAILED ] Ca 1 dinh ngoai le loi: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // CA TEST 2: Chặn và từ chối khi mã phiếu thuê không tồn tại
        // -------------------------------------------------------------
        try {
            int maPhieuAo = -999;
            LocalDateTime ngayTraThucTe = LocalDateTime.now();

            boolean isReturned = rentalService.returnCD(maPhieuAo, ngayTraThucTe);

            if (!isReturned) {
                System.out.println("[ 🟢 PASSED ] Ca 2: He thong chan dung logic khi ma phieu thue khong ton tai.");
            } else {
                System.out.println("[ 🔴 FAILED ] Ca 2 loi, ma phieu ao nhung van báo tra đĩa thanh cong!");
            }
        } catch (Throwable e) {
            System.out.println("[ 🔴 FAILED ] Ca 2 dinh loi: " + e.getMessage());
        }

        System.out.println("=========================================");
    }
}