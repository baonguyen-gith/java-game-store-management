package otkhongluong.gamestoremanagement.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import otkhongluong.gamestoremanagement.model.Invoice;
import otkhongluong.gamestoremanagement.model.CartItem;
import java.util.ArrayList;

class InvoiceServiceTest {
    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        invoiceService = new InvoiceService();
    }

    @Test
    void testCreateInvoice_Success() {
        // Given: Chuẩn bị dữ liệu hóa đơn hợp lệ
        Invoice invoice = new Invoice();
        invoice.setMaKhachHang(1); // Khách hàng mẫu ID = 1
        invoice.setMaNhanVien(1);  // Nhân viên lập đơn ID = 1
        
        // Thêm sản phẩm vào giỏ hàng
        ArrayList<CartItem> items = new ArrayList<>();
        items.add(new CartItem(1, "Đĩa CD Game LoL", 1, 150000.0)); // MaSP = 1, SL = 1
        invoice.setDanhSachItem(items);

        // When: Thực hiện gọi hàm tạo hóa đơn bán hàng
        boolean isSaved = invoiceService.createInvoice(invoice);

        // Then: Kết quả phải ghi nhận thành công dưới Database
        assertTrue(isSaved, "Hóa đơn bán hàng hợp lệ phải được lưu thành công");
    }
}