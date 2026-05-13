package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.Product;
import otkhongluong.gamestoremanagement.service.ProductService;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller trung gian giữa ProductPanel (View) và ProductService.
 * - View KHÔNG gọi Service trực tiếp.
 * - Controller KHÔNG biết chi tiết UI (JTable, JTextField, v.v.).
 * - Mọi thông báo lỗi/xác nhận UI được uỷ quyền lại cho View qua
 *   interface Callback, hoặc Controller nhận JComponent làm "parent"
 *   để hiện JOptionPane (chấp nhận được trong Swing MVC thuần).
 */
public class ProductController {

    private final ProductService service;
    private final JComponent view; // parent component để hiện dialog

    public ProductController(JComponent view) {
        this.service = new ProductService();
        this.view    = view;
    }

    // ==================== LOAD ====================

    /** Trả về toàn bộ danh sách sản phẩm từ DB. */
    public List<Product> loadAll() {
        return service.getAllSanPham();
    }

    /** Trả về 1 sản phẩm theo mã, hoặc null nếu không tìm thấy. */
    public Product getById(int maSP) {
        return service.getSanPhamById(maSP);
    }

    // ==================== SEARCH / FILTER ====================

    /**
     * Lọc danh sách theo từ khóa.
     * Hỗ trợ: mã SP ("sp001"/"1"), mã Game ("g005"/"5"), giá bán, giá thuê.
     *
     * @param source  danh sách nguồn (allData của View)
     * @param keyword từ khóa nhập từ ô tìm kiếm
     * @return danh sách đã lọc (không thay đổi source)
     */
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

    /**
     * Sắp xếp danh sách in-place.
     * View gọi renderPage() sau khi sort xong.
     *
     * @param source    danh sách gốc (allData)
     * @param type      "MaSP" | "MaGame" | "GiaBan" | "GiaThue"
     * @param ascending true = thấp → cao
     */
    public void sort(List<Product> source, String type, boolean ascending) {
        if (source == null || source.isEmpty()) return;
        source.sort((s1, s2) -> {
            int res = 0;
            switch (type) {
                case "MaSP":    res = Integer.compare(s1.getMaSP(),       s2.getMaSP());        break;
                case "MaGame":  res = Integer.compare(s1.getMaGame(),     s2.getMaGame());      break;
                case "GiaBan":  res = Double.compare(s1.getGiaBan(),      s2.getGiaBan());      break;
                case "GiaThue": res = Double.compare(s1.getGiaThueNgay(), s2.getGiaThueNgay()); break;
                default: break;
            }
            return ascending ? res : -res;
        });
    }

    // ==================== ADD ====================

    /**
     * Xử lý thêm sản phẩm mới.
     * Validate dữ liệu đầu vào, gọi Service, thông báo kết quả.
     *
     * @param maGameStr  chuỗi nhập từ ô Mã Game
     * @param giaBanStr  chuỗi nhập từ ô Giá Bán
     * @param giaThueStr chuỗi nhập từ ô Giá Thuê
     * @return true nếu thêm thành công
     */
    public boolean handleAdd(String maGameStr, String giaBanStr, String giaThueStr) {
        if (maGameStr == null || maGameStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Vui lòng nhập Mã Game!");
            return false;
        }
        try {
            Product sp = new Product();
            sp.setMaGame(Integer.parseInt(maGameStr.trim()));
            sp.setGiaBan(Double.parseDouble(giaBanStr.trim()));
            sp.setGiaThueNgay(Double.parseDouble(giaThueStr.trim()));

            boolean ok = service.addSanPham(sp);
            if (ok) {
                JOptionPane.showMessageDialog(view, "Thêm sản phẩm thành công!");
            } else {
                JOptionPane.showMessageDialog(view,
                        "Lỗi: Không tìm thấy Mã Game này hoặc lỗi kết nối!");
            }
            return ok;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Mã Game và Giá tiền phải là con số!");
            return false;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Lỗi hệ thống: " + ex.getMessage());
            return false;
        }
    }

    // ==================== UPDATE ====================

    /**
     * Xử lý cập nhật sản phẩm.
     *
     * @param sp         đối tượng Product đang chỉnh sửa
     * @param maGameStr  chuỗi nhập từ ô Mã Game
     * @param giaBanStr  chuỗi nhập từ ô Giá Bán
     * @param giaThueStr chuỗi nhập từ ô Giá Thuê
     * @return true nếu cập nhật thành công
     */
    public boolean handleUpdate(Product sp, String maGameStr,
                                String giaBanStr, String giaThueStr) {
        try {
            sp.setMaGame(Integer.parseInt(maGameStr.trim()));
            sp.setGiaBan(Double.parseDouble(giaBanStr.trim()));
            sp.setGiaThueNgay(Double.parseDouble(giaThueStr.trim()));

            boolean ok = service.updateSanPham(sp);
            if (ok) {
                JOptionPane.showMessageDialog(view, "Cập nhật thành công!");
            } else {
                JOptionPane.showMessageDialog(view,
                        "Cập nhật thất bại! Vui lòng kiểm tra lại Mã Game.");
            }
            return ok;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view,
                    "Dữ liệu nhập vào phải là con số hợp lệ!",
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Lỗi: " + ex.getMessage());
            return false;
        }
    }

    // ==================== DELETE ====================

    /**
     * Xử lý xóa sản phẩm (bao gồm confirm dialog).
     *
     * @param sp sản phẩm cần xóa
     * @return true nếu xóa thành công
     */
    public boolean handleDelete(Product sp) {
        int confirm = JOptionPane.showConfirmDialog(view,
                "Xác nhận xóa sản phẩm mã: SP" + String.format("%03d", sp.getMaSP()) + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return false;

        boolean ok = service.deleteSanPham(sp.getMaSP());
        if (ok) {
            JOptionPane.showMessageDialog(view, "Xóa thành công!");
        } else {
            JOptionPane.showMessageDialog(view,
                    "Lỗi: Không thể xóa sản phẩm này " +
                    "(có thể nó đang nằm trong một hóa đơn cũ).");
        }
        return ok;
    }

    // ==================== STOCK ====================

    /**
     * Lọc sản phẩm có số lượng CD thấp hơn hoặc bằng ngưỡng cho trước.
     * Dùng cho StockPanel cảnh báo hàng sắp hết.
     */
    public List<Product> filterLowStock(List<Product> source, int threshold) {
        if (source == null) return Collections.emptyList();
        return source.stream()
                .filter(sp -> sp.getSoLuongCD() <= threshold)
                .collect(Collectors.toList());
    }
}