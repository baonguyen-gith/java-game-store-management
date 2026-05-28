package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.model.RentalOrder;
import otkhongluong.gamestoremanagement.model.CTPhieuThue;
import otkhongluong.gamestoremanagement.service.RentalService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RentalServiceTest {

    public static void main(String[] args) {
        RentalService rentalService = new RentalService();

        System.out.println("=========================================");
        System.out.println("   BATT DAU CHAY KIEM THU THUE GAME      ");
        System.out.println("=========================================");

        // -------------------------------------------------------------
        // CA TEST 1: Tạo phiếu thuê thành công với đĩa game Sẵn Sàng
        // -------------------------------------------------------------
        try {
            RentalOrder pt = new RentalOrder();
            pt.setMaKH(1);
            pt.setMaNV(1);
            pt.setNgayThue(LocalDateTime.now());
            pt.setNgayTraDuKien(LocalDateTime.now().plusDays(3)); 
            pt.setTienCoc(200000.0);


            List<CTPhieuThue> danhSachChiTiet = new ArrayList<>();

            danhSachChiTiet.add(new CTPhieuThue(5, "League of Legends Offline", 30000.0, "SanSang"));
            pt.setDanhSachChiTiet(danhSachChiTiet);

            boolean isSuccess = rentalService.createPhieuThue(pt);

            if (isSuccess) {
                System.out.println("[ 🟢 PASSED ] Ca 1: Tao phieu thue thanh cong, trang thai CD da chuyen sang DangThue.");
            } else {
                System.out.println("[ 🔴 FAILED ] Ca 1 that bai. (Kiem tra lai MaCD xem co dung la 'SanSang' duoi DB khong)");
            }

        } catch (Throwable e) {
            System.out.println("[ 🔴 FAILED ] Ca 1 dinh ngoai le: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // CA TEST 2: Chặn tạo phiếu thuê khi danh sách chi tiết rỗng (Null)
        // -------------------------------------------------------------
        try {
            RentalOrder ptNull = new RentalOrder();
            ptNull.setDanhSachChiTiet(null);

            boolean isSuccess = rentalService.createPhieuThue(ptNull);

            if (!isSuccess) {
                System.out.println("[ 🟢 PASSED ] Ca 2: He thong chan dung logic khi danh sach chi tiet thue bi Null.");
            } else {
                System.out.println("[ 🔴 FAILED ] Ca 2 loi, he thong khong chan duoc du lieu rong.");
            }
        } catch (Throwable e) {
            System.out.println("[ 🔴 FAILED ] Ca 2 dinh loi: " + e.getMessage());
        }

        System.out.println("=========================================");
    }
}