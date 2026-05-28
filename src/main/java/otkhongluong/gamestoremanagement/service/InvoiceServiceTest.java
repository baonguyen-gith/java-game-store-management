package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.model.CartItem;
import otkhongluong.gamestoremanagement.service.InvoiceService;
import java.util.ArrayList;
import java.util.List;

public class InvoiceServiceTest {

    public static void main(String[] args) {
        InvoiceService invoiceService = new InvoiceService();

        System.out.println("=========================================");
        System.out.println("   BATT DAU CHAY KIEM THU TAO HOA DON     ");
        System.out.println("=========================================");

        // -------------------------------------------------------------
        // CA TEST 1: Tạo hóa đơn thành công với giỏ hàng hợp lệ
        // -------------------------------------------------------------
        try {
            int maKH = 1;       
            int diemThucDung = 0;

            List<CartItem> cart = new ArrayList<>();

            cart.add(new CartItem(1, -1, 1, 1, "Game Adventure ROM", "ROM", "ROM_1", 150000.0));

            String result = invoiceService.createInvoice(maKH, diemThucDung, cart);

            if (result != null && result.startsWith("OK:")) {
                System.out.println("[ 🟢 PASSED ] Ca 1: Tao hoa don thanh cong! Ket qua: " + result);
            } else {
                System.out.println("[ 🔴 FAILED ] Ca 1 that bai: " + result);
            }

        } catch (Throwable e) {
            System.out.println("[ 🔴 FAILED ] Ca 1 dinh ngoai le loi: " + e.getMessage());
        }

        // -------------------------------------------------------------
        // CA TEST 2: Chặn tạo hóa đơn khi giỏ hàng trống
        // -------------------------------------------------------------
        try {
            int maKH = 1;
            int diemThucDung = 0;
            List<CartItem> emptyCart = new ArrayList<>();

            String result = invoiceService.createInvoice(maKH, diemThucDung, emptyCart);

            if (result != null && result.contains("Giỏ hàng trống")) {
                System.out.println("[ 🟢 PASSED ] Ca 2: He thong chan thanh cong khi gio hang trong.");
            } else {
                System.out.println("[ 🔴 FAILED ] Ca 2 loi, khong chan duoc gio hang trong.");
            }
        } catch (Throwable e) {
            System.out.println("[ 🔴 FAILED ] Ca 2 dinh loi: " + e.getMessage());
        }

        System.out.println("=========================================");
    }
}