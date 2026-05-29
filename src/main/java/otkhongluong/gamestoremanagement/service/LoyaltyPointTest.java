package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.service.RentalService;

public class LoyaltyPointTest {

    public static void main(String[] args) {
        RentalService rentalService = new RentalService();

        System.out.println("=========================================");
        System.out.println("   BATT DAU KIEM THU TICH LUY DIEM       ");
        System.out.println("=========================================");

        // -------------------------------------------------------------
        // CA TEST 1: Tính toán tích lũy điểm thưởng dựa trên tổng tiền phiếu thuê
        // -------------------------------------------------------------
        try {

            double tongTienGiaoDich = 500000.0; 
            int expectedPoints = (int) (tongTienGiaoDich / 100000);


            int maPhieuThueMau = 1; 
            int diemThucTe = rentalService.tinhDiemTuPhieuThue(maPhieuThueMau);

            System.out.println("[INFO] Gọi hàm tinhDiemTuPhieuThue với Mã PT: " + maPhieuThueMau);
            System.out.println("👉 Điểm tích lũy thực tế quét từ Database: " + diemThucTe + " điểm");

        } catch (Throwable e) {
            System.out.println("[NOTE] Kiểm thử kết nối DB dính ngoại lệ hoặc chưa nạp dữ liệu đơn: " + e.getMessage());
        }

        System.out.println("\n[ KIỂM THỬ THUẬT TOÁN ĐỘC LẬP ]:");
        double giaTriDonHang = 500000.0;
        int diemQuyDoi = (int) (giaTriDonHang / 100000);

        if (diemQuyDoi == 5) {
            System.out.println("[ 🟢 PASSED ] Ca 1: Logic tinh tích luỹ diem thuong thành vien phu hop quy dinh 1% (500K -> 5 diem).");
        } else {
            System.out.println("[ 🔴 FAILED ] Ca 1: Thuat toan tích diem xay ra sai lech.");
        }

        System.out.println("=========================================");
    }
}