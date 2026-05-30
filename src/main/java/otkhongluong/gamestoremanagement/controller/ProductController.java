package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.Product;
import otkhongluong.gamestoremanagement.service.ProductService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ProductController {

    private final ProductService service;

    // ✅ Bỏ JComponent view
    public ProductController() {
        this.service = new ProductService();
    }

    // ==================== LOAD ====================

    public List<Product> loadAll() {
        return service.getAllSanPham();
    }

    public Product getById(int maSP) {
        return service.getSanPhamById(maSP);
    }

    // ==================== SEARCH / FILTER ====================

    public List<Product> filter(List<Product> source, String keyword) {
        if (source == null) return Collections.emptyList();
        if (keyword == null || keyword.trim().isEmpty()) return source;

        String kw = keyword.trim().toLowerCase();
        return source.stream()
                .filter(sp -> {
                    String maSPStr   = "sp" + String.format("%03d", sp.getMaSP());
                    String maSPRaw   = String.valueOf(sp.getMaSP());
                    String maGameStr = "g"  + String.format("%03d", sp.getMaGame());
                    String maGameRaw = String.valueOf(sp.getMaGame());
                    String giaBan    = String.valueOf(sp.getGiaBan());
                    String giaThue   = String.valueOf(sp.getGiaThueNgay());

                    return maSPStr.contains(kw)   || maSPRaw.contains(kw)
                        || maGameStr.contains(kw)  || maGameRaw.contains(kw)
                        || giaBan.contains(kw)     || giaThue.contains(kw);
                })
                .collect(Collectors.toList());
    }

    // ==================== SORT ====================

    public void sort(List<Product> source, String type, boolean ascending) {
        if (source == null || source.isEmpty()) return;
        source.sort((s1, s2) -> {
            int res = 0;
            switch (type) {
                case "MaSP":    res = Integer.compare(s1.getMaSP(),       s2.getMaSP());        break;
                case "MaGame":  res = Integer.compare(s1.getMaGame(),     s2.getMaGame());      break;
                case "GiaBan":  res = Double.compare(s1.getGiaBan(),      s2.getGiaBan());      break;
                case "GiaThue": res = Double.compare(s1.getGiaThueNgay(), s2.getGiaThueNgay()); break;
            }
            return ascending ? res : -res;
        });
    }

    // ==================== ADD ====================
    public ActionResult handleAdd(String loai, String maGameStr,
                                   String giaBanStr, String giaThueStr) {
        if (maGameStr == null || maGameStr.trim().isEmpty())
            return ActionResult.fail("Vui lòng nhập Mã Game!");
        try {
            Product sp = new Product();
            sp.setMaGame(Integer.parseInt(maGameStr.trim()));

            if ("ROM".equals(loai)) {
                sp.setGiaBan(Double.parseDouble(giaBanStr.trim()));
                sp.setGiaThueNgay(0);
            } else { // CD
                double giaBan  = giaBanStr  == null || giaBanStr.trim().isEmpty()  ? 0 : Double.parseDouble(giaBanStr.trim());
                double giaThue = giaThueStr == null || giaThueStr.trim().isEmpty() ? 0 : Double.parseDouble(giaThueStr.trim());
                if (giaBan == 0 && giaThue == 0)
                    return ActionResult.fail("Vui lòng nhập ít nhất Giá Bán hoặc Giá Thuê cho CD!");
                sp.setGiaBan(giaBan);
                sp.setGiaThueNgay(giaThue);
            }

            boolean ok = service.addSanPham(sp);
            return ok ? ActionResult.ok("Thêm sản phẩm thành công!")
                      : ActionResult.fail("Lỗi: Không tìm thấy Mã Game hoặc lỗi kết nối!");
        } catch (NumberFormatException ex) {
            return ActionResult.fail("Mã Game và Giá tiền phải là con số!");
        } catch (Exception ex) {
            return ActionResult.fail("Lỗi hệ thống: " + ex.getMessage());
        }
    }

    // ==================== UPDATE ====================

    public ActionResult handleUpdate(Product sp, String maGameStr,
                                     String giaBanStr, String giaThueStr) {
        try {
            sp.setMaGame(Integer.parseInt(maGameStr.trim()));
            sp.setGiaBan(Double.parseDouble(giaBanStr.trim()));
            sp.setGiaThueNgay(Double.parseDouble(giaThueStr.trim()));

            boolean ok = service.updateSanPham(sp);
            return ok
                ? ActionResult.ok("Cập nhật thành công!")
                : ActionResult.fail("Cập nhật thất bại! Vui lòng kiểm tra lại Mã Game.");
        } catch (NumberFormatException ex) {
            return ActionResult.fail("Dữ liệu nhập vào phải là con số hợp lệ!");
        } catch (Exception ex) {
            return ActionResult.fail("Lỗi: " + ex.getMessage());
        }
    }

    // ==================== DELETE ====================

    // ✅ Không hỏi confirm — View tự hỏi trước khi gọi
    public ActionResult handleDelete(int maSP) {
        boolean ok = service.deleteSanPham(maSP);
        return ok
            ? ActionResult.ok("Xóa thành công!")
            : ActionResult.fail("Lỗi: Không thể xóa sản phẩm này " +
              "(có thể nó đang nằm trong một hóa đơn cũ).");
    }

    // ==================== STOCK ====================

    public List<Product> filterLowStock(List<Product> source, int threshold) {
        if (source == null) return Collections.emptyList();
        return source.stream()
                .filter(sp -> sp.getSoLuongCD() <= threshold)
                .collect(Collectors.toList());
    }

    // ==================== PAGINATION ====================

    public static class PageResult {
        public final List<Product> data;
        public final int currentPage;
        public final int totalPages;

        public PageResult(List<Product> data, int currentPage, int totalPages) {
            this.data = data;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
        }
    }

    public PageResult getPage(List<Product> all, String keyword, int page, int pageSize) {
        List<Product> filtered = filter(all, keyword);
        int total    = Math.max(1, (int) Math.ceil((double) filtered.size() / pageSize));
        int safePage = Math.min(Math.max(1, page), total);
        int from     = (safePage - 1) * pageSize;
        int to       = Math.min(from + pageSize, filtered.size());
        return new PageResult(filtered.subList(from, to), safePage, total);
    }

    // ==================== INNER: ActionResult ====================

    public static class ActionResult {
        public final boolean success;
        public final String  message;

        private ActionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static ActionResult ok(String msg)   { return new ActionResult(true,  msg); }
        public static ActionResult fail(String msg) { return new ActionResult(false, msg); }
    }
}

