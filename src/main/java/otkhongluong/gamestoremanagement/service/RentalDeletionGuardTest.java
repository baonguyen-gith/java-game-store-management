package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.service.RentalService;

public class RentalDeletionGuardTest {

    public static void main(String[] args) {
        RentalService rentalService = new RentalService();

        System.out.println("=========================================");
        System.out.println("   BATT DAU KIEM THU CHAN XOA PHIEU THUE ");
        System.out.println("=========================================");

        // -------------------------------------------------------------
        // CA TEST 1: Chặn không cho xóa phiếu thuê đang ở trạng thái 'DangThue'
        // -------------------------------------------------------------
        try {
            int maPhieuThueActive = 2; 

            boolean isDeleted = rentalService.deletePhieuThue(maPhieuThueActive);

            if (!isDeleted) {
                System.out.println("[ 🟢 PASSED ] Ca 1: Co che bao ve hoat dong dung, tu choi xoa phieu thue dang hoat dong.");
            } else {
                System.out.println("[ 🔴 FAILED ] Ca 1 loi: Phieu thue dang o trang thai 'DangThue' nhung van bi xoa mat khoi DB!");
            }

        } catch (Throwable e) {
            System.out.println("[ 🔴 FAILED ] Ca 1 dính ngoại lệ lỗi: " + e.getMessage());
        }

        System.out.println("=========================================");
    }
}