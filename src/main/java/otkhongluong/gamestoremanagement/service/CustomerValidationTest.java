package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.model.Customer;
import otkhongluong.gamestoremanagement.service.ValidationService;

public class CustomerValidationTest {

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   BATT DAU KIEM THU VALIDATE KHACH HANG ");
        System.out.println("=========================================");

        // -------------------------------------------------------------
        // CA TEST 1: Chặn hành vi tạo hồ sơ khách hàng mới khi thiếu số CCCD
        // -------------------------------------------------------------
        try {
            Customer badCustomer = new Customer();
            
            badCustomer.setHoTen("Nguyễn Văn Khách");
            badCustomer.setSdt("0945678901"); 
            badCustomer.setCccd("");  

            ValidationService.validateNotEmpty(badCustomer.getCccd(), "Số CCCD");

            System.out.println("[ 🔴 FAILED ] Ca 1 lỗi: Trường CCCD trống nhưng không bị hệ thống chặn!");

        } catch (IllegalArgumentException e) {
            System.out.println("[ 🟢 PASSED ] Ca 1: Chan thanh cong! He thong tu choi ho so khach hang thieu CCCD.");
            System.out.println("             Thong bao loi thu duoc: " + e.getMessage());
        } catch (Throwable e) {
            System.out.println("[ 🔴 FAILED ] Ca 1 dính ngoại lệ khác: " + e.getMessage());
        }

        System.out.println("=========================================");
    }
}