package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.model.RentalOrder;
import otkhongluong.gamestoremanagement.model.CTPhieuThue;
import otkhongluong.gamestoremanagement.service.RentalService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OverdueFineTest {

    public static void main(String[] args) {
        RentalService rentalService = new RentalService();

        System.out.println("=========================================");
        System.out.println("   BATT DAU KIEM THU TINH TIEN PHAT      ");
        System.out.println("=========================================");

        // -------------------------------------------------------------
        // CA TEST 1: Tính toán chính xác tiền phạt phát sinh do trả trễ hạn 5 ngày
        // -------------------------------------------------------------
        try {            RentalOrder pt = new RentalOrder();
            pt.setNgayTraDuKien(LocalDateTime.now().minusDays(5)); 
            
            LocalDateTime ngayTraThucTe = LocalDateTime.now(); 

            List<CTPhieuThue> cds = new ArrayList<>();
            CTPhieuThue ct = new CTPhieuThue();
            ct.setGiaThueNgay(20000.0);  
            ct.setTrangThai("BINH_THUONG");
            cds.add(ct);


            double tienPhatThucTe = rentalService.tinhTienPhat(pt, ngayTraThucTe, cds);


            double expectedFine = 5 * 20000.0 * 1.5; 

            if (tienPhatThucTe == expectedFine) {
                System.out.println("[ 🟢 PASSED ] Ca 1: Thuat toan tinh tien phat tre han dung tuyet doi!");
                System.out.println("             So tien phat thuc te: " + tienPhatThucTe + " VND");
            } else {
                System.out.println("[ 🔴 FAILED ] Ca 1 tinh sai! Ket qua thuc te: " + tienPhatThucTe + " VND (Ky vong: " + expectedFine + " VND)");
            }

        } catch (Throwable e) {
            System.out.println("[ 🔴 FAILED ] Ca 1 dính ngoại lệ lỗi: " + e.getMessage());
        }

        System.out.println("=========================================");
    }
}