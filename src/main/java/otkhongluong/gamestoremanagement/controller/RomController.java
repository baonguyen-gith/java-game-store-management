package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.ROM;
import otkhongluong.gamestoremanagement.service.RomService;

public class RomController {

    private final RomService service;

    public RomController() {
        this.service = new RomService();
    }

    public ROM loadRom(int maSP) {
        try {
            return service.getByMaSP(maSP);
        } catch (Exception e) {
            return null;
        }
    }

    public ProductController.ActionResult handleSave(int maSP,
                                                     String dungLuongStr,
                                                     String linkLuuTru,
                                                     String soLuotBanStr) {
        try {
            if (dungLuongStr == null || dungLuongStr.trim().isEmpty())
                return ProductController.ActionResult.fail("Vui lòng nhập Dung lượng!");
            if (linkLuuTru == null || linkLuuTru.trim().isEmpty())
                return ProductController.ActionResult.fail("Vui lòng nhập Link lưu trữ!");

            int soLuotBan = 0;
            if (soLuotBanStr != null && !soLuotBanStr.trim().isEmpty())
                soLuotBan = Integer.parseInt(soLuotBanStr.trim());

            ROM rom = new ROM(maSP, dungLuongStr.trim(), linkLuuTru.trim(), soLuotBan);
            boolean ok = service.save(rom);
            return ok
                    ? ProductController.ActionResult.ok("Lưu thông tin ROM thành công!")
                    : ProductController.ActionResult.fail("Lỗi: Không thể lưu. Kiểm tra Mã SP có tồn tại không.");
        } catch (NumberFormatException ex) {
            return ProductController.ActionResult.fail("Số lượt bán phải là số nguyên!");
        } catch (Exception ex) {
            return ProductController.ActionResult.fail("Lỗi: " + ex.getMessage());
        }
    }

    public ProductController.ActionResult handleDelete(int maSP) {
        try {
            boolean ok = service.delete(maSP);
            return ok
                    ? ProductController.ActionResult.ok("Đã xóa thông tin ROM!")
                    : ProductController.ActionResult.fail("Không tìm thấy ROM để xóa.");
        } catch (Exception ex) {
            return ProductController.ActionResult.fail("Lỗi: " + ex.getMessage());
        }
    }
}