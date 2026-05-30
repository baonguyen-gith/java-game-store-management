package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.CustomerDAO;
import otkhongluong.gamestoremanagement.dao.InvoiceDAO;
import otkhongluong.gamestoremanagement.model.ChiTietHoaDon;
import otkhongluong.gamestoremanagement.model.Customer;
import otkhongluong.gamestoremanagement.model.Invoice;
import otkhongluong.gamestoremanagement.model.SpRow;
import otkhongluong.gamestoremanagement.model.CartItem;
import otkhongluong.gamestoremanagement.model.RentDetailData;
import otkhongluong.gamestoremanagement.util.DBConnection;
import otkhongluong.gamestoremanagement.util.Session;
import otkhongluong.gamestoremanagement.util.FormatUtil;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * InvoiceService — xử lý nghiệp vụ hóa đơn.
 * Không chứa SQL trực tiếp; mọi truy vấn đều qua InvoiceDAO hoặc CustomerDAO.
 */
public class InvoiceService {

    private static final int    VND_PER_DIEM  = 100_000;
    private static final int    DIEM_TO_VND   = 5_000;
    private static final double DIEM_PER_DONG = 100_000.0;

    private final InvoiceDAO  dao   = new InvoiceDAO();
    private final CustomerDAO khDAO = new CustomerDAO();

    // ================================================================
    // CRUD CƠ BẢN
    // ================================================================

    public List<Invoice> getAllHoaDon() {
        return dao.getAllHoaDon();
    }

    public Invoice getHoaDonById(int maHD) {
        return dao.getHoaDonById(maHD);
    }

    public boolean deleteHoaDon(int maHD) {
        return dao.deleteHoaDon(maHD);
    }

    // ================================================================
    // CATALOG — dùng cho InvoiceAddDialog + InvoiceEditDialog
    // ================================================================

    /**
     * Danh sách game có sản phẩm bán được.
     * Mỗi Object[]: {MaGame, TenGame, TheLoai, NenTang}
     */
    public List<Object[]> loadGameCatalog() {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT g.MaGame, g.TenGame, g.TheLoai, g.NenTang " +
            "FROM GAME g " +
            "WHERE EXISTS (" +
            "  SELECT 1 FROM SANPHAM sp " +
            "  WHERE sp.MaGame=g.MaGame AND sp.GiaBan IS NOT NULL AND sp.GiaBan>0) " +
            "ORDER BY g.TenGame";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new Object[]{
                    rs.getInt("MaGame"),
                    rs.getString("TenGame"),
                    nvl(rs.getString("TheLoai")),
                    nvl(rs.getString("NenTang"))
                });
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    /**
     * SP (CD + ROM) của 1 game cho InvoiceAddDialog.
     * Mỗi Object[]: {MaSP, MaCD_or_-1, loaiSP, giaBan, thongTin, available, MaGame}
     */
    public List<Object[]> loadSpCatalog(int maGame) {
        List<Object[]> list = new ArrayList<>();

        String sqlCD =
            "SELECT sp.MaSP, cd.MaCD, sp.GiaBan, cd.TinhTrang " +
            "FROM SANPHAM sp JOIN CD cd ON sp.MaSP=cd.MaSP " +
            "WHERE sp.MaGame=? AND cd.TrangThai=N'SanSang' " +
            "  AND cd.TinhTrang != N'Hỏng' " +  // ← THÊM
            "  AND sp.GiaBan IS NOT NULL AND sp.GiaBan>0";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlCD)) {
            ps.setInt(1, maGame);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new Object[]{
                        rs.getInt("MaSP"), rs.getInt("MaCD"), "CD",
                        rs.getDouble("GiaBan"),
                        "Tình trạng: " + nvl(rs.getString("TinhTrang")),
                        true, maGame
                    });
            }
        } catch (SQLException ex) { ex.printStackTrace(); }

        String sqlHet =
            "SELECT " +
            "  SUM(CASE WHEN cd.TrangThai='DangThue' THEN 1 ELSE 0 END) AS DangThue," +
            "  SUM(CASE WHEN cd.TrangThai='DaBan'    THEN 1 ELSE 0 END) AS DaBan," +
            "  SUM(CASE WHEN cd.TrangThai='Hong'     THEN 1 ELSE 0 END) AS Hong " +
            "FROM CD cd JOIN SANPHAM sp ON cd.MaSP=sp.MaSP " +
            "WHERE sp.MaGame=? AND cd.TrangThai!=N'SanSang'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlHet)) {
            ps.setInt(1, maGame);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int dt = rs.getInt("DangThue"), db = rs.getInt("DaBan"), h = rs.getInt("Hong");
                    if (dt > 0) list.add(new Object[]{-1,-1,"CD",0.0,"🔒 "+dt+" CD đang được thuê",false,maGame});
                    if (db > 0) list.add(new Object[]{-1,-1,"CD",0.0,"💰 "+db+" CD đã bán",         false,maGame});
                    if (h  > 0) list.add(new Object[]{-1,-1,"CD",0.0,"⚠ " +h +" CD hỏng / mất",    false,maGame});
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }

        String sqlROM =
            "SELECT sp.MaSP, r.DungLuong, sp.GiaBan " +
            "FROM SANPHAM sp JOIN ROM r ON sp.MaSP=r.MaSP " +
            "WHERE sp.MaGame=? AND sp.GiaBan IS NOT NULL AND sp.GiaBan>0";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlROM)) {
            ps.setInt(1, maGame);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new Object[]{
                        rs.getInt("MaSP"), -1, "ROM",
                        rs.getDouble("GiaBan"),
                        "Tải về — " + nvl(rs.getString("DungLuong")),
                        true, maGame
                    });
            }
        } catch (SQLException ex) { ex.printStackTrace(); }

        return list;
    }

    /**
     * SP (CD + ROM) cho popup "Thêm SP" trong InvoiceEditDialog (kèm TenGame ở index 7).
     * Mỗi Object[]: {MaSP, MaCD_or_-1, loaiSP, giaBan, thongTin, available, MaGame, TenGame}
     */
    public List<Object[]> loadSpCatalogForEdit(int maGame, String tenGame) {
        List<Object[]> list = new ArrayList<>();
        String tn = tenGame == null ? "" : tenGame;

        String sqlCD =
            "SELECT sp.MaSP, cd.MaCD, sp.GiaBan, cd.TinhTrang " +
            "FROM SANPHAM sp JOIN CD cd ON sp.MaSP=cd.MaSP " +
            "WHERE sp.MaGame=? AND cd.TrangThai=N'SanSang' " +
            "  AND cd.TinhTrang != N'Hỏng' " +  // ← THÊM
            "  AND sp.GiaBan IS NOT NULL AND sp.GiaBan>0";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlCD)) {
            ps.setInt(1, maGame);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new Object[]{
                        rs.getInt("MaSP"), rs.getInt("MaCD"), "CD",
                        rs.getDouble("GiaBan"),
                        "CD" + rs.getInt("MaCD") + " — " + nvl(rs.getString("TinhTrang")),
                        true, maGame, tn
                    });
            }
        } catch (SQLException ex) { ex.printStackTrace(); }

        String sqlROM =
            "SELECT sp.MaSP, r.DungLuong, sp.GiaBan " +
            "FROM SANPHAM sp JOIN ROM r ON sp.MaSP=r.MaSP " +
            "WHERE sp.MaGame=? AND sp.GiaBan IS NOT NULL AND sp.GiaBan>0";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlROM)) {
            ps.setInt(1, maGame);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new Object[]{
                        rs.getInt("MaSP"), -1, "ROM",
                        rs.getDouble("GiaBan"),
                        "Tải về — " + nvl(rs.getString("DungLuong")),
                        true, maGame, tn
                    });
            }
        } catch (SQLException ex) { ex.printStackTrace(); }

        return list;
    }

    // ================================================================
    // TẠO HÓA ĐƠN
    // ================================================================
    public String createInvoice(int maKH, int diemThucDung, List<CartItem> cart) {
        if (cart == null || cart.isEmpty()) return "ERR:Giỏ hàng trống!";

        // ── Tính toán nghiệp vụ (không có SQL) ──────────────────────────
        double tongGoc  = cart.stream().mapToDouble(i -> i.donGia * i.soLuong).sum();

        // Reset sớm nếu khách vãng lai — để tongPhaiTra tính đúng
        if (maKH == -1) { diemThucDung = 0; }

        double giamTien    = Math.min((double) diemThucDung * DIEM_TO_VND, tongGoc);
        int    diemThuc    = (int) Math.floor(giamTien / DIEM_TO_VND);
        giamTien           = diemThuc * DIEM_TO_VND;
        double tongPhaiTra = Math.max(0, tongGoc - giamTien);
        int    diemCong    = (int) Math.floor(tongGoc / VND_PER_DIEM);

        // Gộp cart theo MaSP
        Map<Integer, double[]> spMap = new LinkedHashMap<>();
        for (CartItem item : cart) {
            spMap.merge(item.maSP,
                new double[]{item.soLuong, item.donGia},
                (old, v) -> new double[]{old[0] + v[0], v[1]});
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Kiểm tra race-condition CD
                for (CartItem item : cart) {
                    if ("CD".equals(item.loaiSP)) {
                        if (!dao.checkAndLockCD(con, item.maCD)) {
                            con.rollback();
                            return "ERR:CD" + item.maCD + " — \"" + item.tenGame +
                                   "\" vừa bị người khác mua!\nVui lòng chọn CD khác hoặc ROM.";
                        }
                    }
                }

                // 2. Tạo HOADON + CTHOADON qua DAO
                final int finalDiemThuc = diemThuc;
                final double finalGiamTien = giamTien;
                int maNV = Session.getMaNV();
                int maHD = dao.insertFull(con,
                    maKH > 0 ? maKH : null,
                    maNV  > 0 ? maNV  : null,
                    tongPhaiTra, finalDiemThuc, finalGiamTien,
                    spMap);

                // 3. Cập nhật CD / ROM qua DAO
                for (CartItem item : cart) {
                    if ("CD".equals(item.loaiSP)) {
                        dao.markCDDaBan(con, item.maCD);
                    } else {
                        dao.increaseROMSoLuotBan(con, item.maSP, item.soLuong);
                    }
                }

                // 4. Điểm KH qua DAO
                final int finalDiemCong = diemCong;
                if (maKH > 0) {
                    if (finalDiemThuc > 0) {
                        khDAO.updatePoint(con, maKH, -finalDiemThuc);  // ← thêm con
                        dao.logDiem(con, maKH, "TRU", finalDiemThuc,
                            "Dùng điểm mua game — HĐ" + maHD);
                    }
                    if (finalDiemCong > 0) {
                        khDAO.updatePoint(con, maKH, finalDiemCong);   // ← thêm con
                        dao.logDiem(con, maKH, "CONG", finalDiemCong,
                            "Mua game — HĐ" + maHD + String.format(" (tổng %,.0f VNĐ)", tongGoc));
                    }
                }

                con.commit();
                return "OK:HĐ" + maHD;

            } catch (Exception ex) {
                con.rollback();
                ex.printStackTrace();
                return "ERR:Thanh toán thất bại!\n" + ex.getMessage();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "ERR:Lỗi kết nối cơ sở dữ liệu: " + ex.getMessage();
        }
    }

    public String getROMLink(int maSP) {
        return dao.getROMLink(maSP);
    }


    /** Load danh sách SP trong HĐ kèm MaCD thực tế. */
    public List<SpRow> loadWorkingItems(int maHD) {
        List<SpRow> list = new ArrayList<>();
        Invoice hd = dao.getHoaDonById(maHD);
        if (hd == null || hd.getDanhSachChiTiet() == null) return list;

        try (Connection con = DBConnection.getConnection()) {
            for (ChiTietHoaDon ct : hd.getDanhSachChiTiet()) {
                int maCD = -1;
                if ("CD".equalsIgnoreCase(ct.getLoaiSanPham())) {
                    maCD = dao.getMaCDDaBan(con, maHD, ct.getMaSP());
                }
                list.add(new SpRow(ct.getMaSP(), maCD,
                    ct.getTenGame(), ct.getLoaiSanPham(),
                    ct.getDonGia(), ct.getSoLuong(), false));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    /**
     * Lưu toàn bộ thay đổi sửa hóa đơn trong 1 transaction.
     * @return "OK:..." nếu thành công, "ERR:..." nếu lỗi
     */
    public String saveEditInvoice(int maHD,
                                  Customer newKH,
                                  Integer  newMaNV,
                                  LocalDate newNgayLap,
                                  int maKHCu, double tongCu,
                                  List<SpRow> removedItems,
                                  List<SpRow> addedItems,
                                  List<SpRow> workingItems) {

        boolean khChanged   = newKH != null && newKH.getMaKH() != maKHCu;
        boolean nvChanged   = newMaNV != null;
        boolean ngayChanged = newNgayLap != null;
        boolean spChanged   = !removedItems.isEmpty() || !addedItems.isEmpty();

        if (!khChanged && !nvChanged && !ngayChanged && !spChanged)
            return "ERR:Không có thay đổi nào để lưu.";

        // ── Tính toán nghiệp vụ (không có SQL) ──────────────────────────
        double tongMoi = workingItems.stream().mapToDouble(SpRow::thanhTien).sum();
        int    diemCu  = (int) (tongCu  / DIEM_PER_DONG);
        int    diemMoi = (int) (tongMoi / DIEM_PER_DONG);
        int    maKHMoi = khChanged ? newKH.getMaKH() : maKHCu;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Cập nhật KH / NV
                if (khChanged || nvChanged) {
                    int curMaNV = nvChanged ? newMaNV : dao.getMaNVByHD(con, maHD);
                    dao.updateKHNV(con, maHD, maKHMoi, curMaNV);
                    if (khChanged) {
                        dao.adjustDiem(con, maKHCu,  -diemCu);
                        dao.adjustDiem(con, maKHMoi, +diemMoi);
                        dao.transferPointLogs(con, maHD, maKHCu, maKHMoi);
                    }
                }

                // 2. Cập nhật ngày lập
                if (ngayChanged) {
                    dao.updateNgayLap(con, maHD, newNgayLap);
                }

                // 3. Xóa SP
                for (SpRow sp : removedItems) {
                    dao.deleteCTHoaDon(con, maHD, sp.maSP);
                    if ("CD".equals(sp.loai) && sp.maCD > 0) {
                        dao.markCDSanSang(con, sp.maCD);
                    } else if ("ROM".equals(sp.loai)) {
                        dao.decreaseROMSoLuotBan(con, sp.maSP, sp.soLuong);
                    }
                }

                // 4. Thêm SP mới
                for (SpRow sp : addedItems) {
                    if ("CD".equals(sp.loai)) {
                        String tt = dao.getCDTrangThai(con, sp.maCD);
                        if (tt == null || !"SanSang".equals(tt)) {
                            con.rollback();
                            return "ERR:CD" + sp.maCD + " — \"" + sp.tenGame +
                                   "\" vừa bị bán mất!\nVui lòng chọn lại.";
                        }
                    }
                    dao.upsertCTHoaDon(con, maHD, sp.maSP, sp.soLuong, sp.donGia);
                    if ("CD".equals(sp.loai) && sp.maCD > 0) {
                        dao.markCDDaBan(con, sp.maCD);
                    } else if ("ROM".equals(sp.loai)) {
                        dao.increaseROMSoLuotBan(con, sp.maSP, sp.soLuong);
                    }
                }

                // 5. Cập nhật TongTien + điểm
                if (spChanged) {
                    dao.updateTongTien(con, maHD, tongMoi);
                    if (!khChanged && diemMoi != diemCu)
                        dao.adjustDiem(con, maKHCu, diemMoi - diemCu);
                }

                con.commit();
                return "OK:Cập nhật hóa đơn HD" + String.format("%03d", maHD) + " thành công!";

            } catch (Exception ex) {
                con.rollback();
                ex.printStackTrace();
                return "ERR:Lỗi khi lưu:\n" + ex.getMessage();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "ERR:Lỗi kết nối:\n" + ex.getMessage();
        }
    }

    
    // Chuẩn bị data từ Invoice + List<ChiTietHoaDon> thực tế
    public Object[] getInvoiceExportData(int maHD) {
        // getHoaDonById đã load cả danhSachChiTiet bên trong
        Invoice invoice = dao.getHoaDonById(maHD);
        if (invoice == null) throw new RuntimeException("Không tìm thấy hóa đơn #" + maHD);

        List<String[]> items = new ArrayList<>();
        double tongGoc = 0;
        for (ChiTietHoaDon ct : invoice.getDanhSachChiTiet()) {
            double thanhTien = ct.getSoLuong() * ct.getDonGia();
            tongGoc += thanhTien;
            items.add(new String[]{
                ct.getTenGame(),
                ct.getLoaiSanPham(),
                String.valueOf(ct.getSoLuong()),
                FormatUtil.formatTien(ct.getDonGia()),
                FormatUtil.formatTien(thanhTien)
            });
        }

        // Điểm & tiền giảm (lấy từ DB qua Invoice; nếu model chưa có getter thì fallback về 0)
        int    diemSuDung  = invoice.getDiemSuDung();          // getter mới trong Invoice
        double tienGiam    = invoice.getTienGiam();            // getter mới trong Invoice
        double tongPhaiTra = invoice.getTongTien();            // TongTien đã là sau-giảm

        // Nếu model chưa lưu TongGoc riêng, tính lại từ items
        // (tongGoc = sum donGia*soLuong, chưa trừ điểm)
        if (tienGiam <= 0 && diemSuDung > 0) {
            tienGiam = diemSuDung * (double) DIEM_TO_VND;
        }

        // Trả Object[] để tránh tạo thêm class mới
        // [0] Invoice  [1] items  [2] tongGoc  [3] diemSuDung  [4] tienGiam  [5] tongPhaiTra
        return new Object[]{invoice, items,
                            tongGoc, (double) diemSuDung, tienGiam, tongPhaiTra};
    }

    // ================================================================
    // PRIVATE HELPERS
    // ================================================================

    private String nvl(String s)              { return s == null ? "" : s; }
    private String nvl2(String s, String def) { return (s == null || s.isBlank()) ? def : s; }
}