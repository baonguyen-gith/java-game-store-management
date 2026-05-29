package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.service.*;
import otkhongluong.gamestoremanagement.dao.*;
import otkhongluong.gamestoremanagement.model.*;

public class RentalDeletionGuardTest {

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   BATT DAU KIEM THU CHAN XOA PHIEU THUE ");
        System.out.println("=========================================");

        // -------------------------------------------------------------
        // CA TEST 1: Chặn không cho xóa phiếu thuê đang ở trạng thái 'DangThue'
        // -------------------------------------------------------------
        try {
            RentalService rentalService = new RentalService();
            int maPhieuThueActive = 2; 

            boolean isDeleted = rentalService.deletePhieuThue(maPhieuThueActive);

            if (!isDeleted) {
                System.out.println("[ 🟢 PASSED ] Ca 1: Co che bao ve hoat dung dung, tu choi xoa phieu thue dang hoat dong.");
            } else {
                System.out.println("[ 🔴 FAILED ] Ca 1 loi: Phieu thue bận nhung van bi xoa khoi he thong!");
            }

        } catch (Throwable e) {

            String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            
            if (errorMsg.contains("conflict") || errorMsg.contains("foreign key") || errorMsg.contains("chặn") || errorMsg.contains("không được xóa")) {
                System.out.println("[ 🟢 PASSED ] Ca 1: Co che bao ve hoat dung dung, tu choi xoa phieu thue dang hoat dong.");
                System.out.println("             Ngoại lệ bảo vệ thu được: " + e.getMessage());
            } else {
                System.out.println("[ 🔴 FAILED ] Ca 1 dinh ngoai le loi he thong: " + e.getMessage());
            }
        }

        System.out.println("=========================================");
    }
}