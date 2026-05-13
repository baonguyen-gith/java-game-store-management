package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.Invoice;
import otkhongluong.gamestoremanagement.model.Invoice.ChiTietHoaDon;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {

    /* =====================================================
        INSERT
     ===================================================== */
    public boolean insert(Invoice hd) {
        String sql =
            "INSERT INTO HOADON(MaKH,MaNV,NgayLap,TongTien,TrangThai) " +
            "VALUES(?,?,?,?,?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, hd.getMaKH());
            ps.setInt(2, hd.getMaNV());
            ps.setTimestamp(3, Timestamp.valueOf(hd.getNgayLap()));
            ps.setDouble(4, hd.getTongTien());
            ps.setString(5, hd.getTrangThai());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) hd.setMaHD(rs.getInt(1));

            insertChiTiet(conn, hd);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /* =====================================================
        INSERT DETAIL
     ===================================================== */
    private void insertChiTiet(Connection conn, Invoice hd) throws SQLException {
        String sqlCT =
            "INSERT INTO CTHOADON(MaHD,MaSP,SoLuong,DonGia) " +
            "VALUES(?,?,?,?)";

        // Thêm query cập nhật trạng thái CD
        String sqlUpdateCD =
            "UPDATE CD SET TrangThai = N'DaBan' " +
            "WHERE MaSP = ? AND TrangThai = N'SanSang'";

        PreparedStatement ps   = conn.prepareStatement(sqlCT);
        PreparedStatement psCD = conn.prepareStatement(sqlUpdateCD);

        for (ChiTietHoaDon ct : hd.getDanhSachChiTiet()) {
            // Insert chi tiết hóa đơn
            ps.setInt(1, hd.getMaHD());
            ps.setInt(2, ct.getMaSP());
            ps.setInt(3, ct.getSoLuong());
            ps.setDouble(4, ct.getDonGia());
            ps.addBatch();

            // Cập nhật CD → DaBan (chỉ ảnh hưởng loại CD, ROM không có dòng nào khớp)
            psCD.setInt(1, ct.getMaSP());
            psCD.addBatch();
        }

        ps.executeBatch();
        psCD.executeBatch();
    }

    /* =====================================================
        FIND ALL
     ===================================================== */
    public List<Invoice> findAll() {
        List<Invoice> list = new ArrayList<>();

        String sql =
            "SELECT hd.MaHD, hd.MaKH, hd.MaNV, hd.NgayLap, hd.TongTien, " +
            "       kh.HoTen, kh.SDT " +
            "FROM HOADON hd " +
            "JOIN KHACHHANG kh ON hd.MaKH = kh.MaKH " +
            "ORDER BY hd.MaHD DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Invoice hd = new Invoice();
                hd.setMaHD(rs.getInt("MaHD"));
                hd.setMaKH(rs.getInt("MaKH"));
                hd.setMaNV(rs.getInt("MaNV"));
                hd.setTongTien(rs.getDouble("TongTien"));
                hd.setTenKhachHang(rs.getString("HoTen"));
                hd.setSoDienThoai(rs.getString("SDT"));
                Timestamp ts = rs.getTimestamp("NgayLap");
                if (ts != null) hd.setNgayLap(ts.toLocalDateTime());
                list.add(hd);
            }

        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }

    /* =====================================================
        FIND BY ID + DETAIL
     ===================================================== */
    public Invoice findById(int maHD) {
        String sql = "SELECT * FROM HOADON WHERE MaHD=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maHD);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Invoice hd = mapHoaDon(rs);
                hd.setDanhSachChiTiet(getChiTiet(conn, maHD));
                return hd;
            }

        } catch (Exception e) { e.printStackTrace(); }

        return null;
    }

    /* =====================================================
        LOAD DETAIL
     ===================================================== */
    private List<ChiTietHoaDon> getChiTiet(Connection conn, int maHD) {
        List<ChiTietHoaDon> list = new ArrayList<>();

        String sql =
            "SELECT ct.MaSP, g.TenGame, " +
            "    CASE " +
            "        WHEN EXISTS (SELECT 1 FROM CD  WHERE CD.MaSP  = sp.MaSP) THEN N'CD' " +
            "        WHEN EXISTS (SELECT 1 FROM ROM WHERE ROM.MaSP = sp.MaSP) THEN N'ROM' " +
            "        ELSE N'Không rõ' " +
            "    END AS LoaiSanPham, " +
            "    ct.SoLuong, ct.DonGia " +
            "FROM CTHOADON ct " +
            "JOIN SANPHAM sp ON ct.MaSP  = sp.MaSP " +
            "JOIN GAME    g  ON sp.MaGame = g.MaGame " +
            "WHERE ct.MaHD = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHD);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ChiTietHoaDon ct = new ChiTietHoaDon(
                    rs.getString("TenGame"),
                    rs.getString("LoaiSanPham"),
                    rs.getInt("SoLuong"),
                    rs.getDouble("DonGia")
                );
                ct.setMaSP(rs.getInt("MaSP"));
                list.add(ct);
            }

        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }

    /* =====================================================
        UPDATE TRANG THAI
     ===================================================== */
    public boolean updateTrangThai(Invoice hd) {
        String sql = "UPDATE HOADON SET TrangThai=? WHERE MaHD=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hd.getTrangThai());
            ps.setInt(2, hd.getMaHD());
            return ps.executeUpdate() > 0;

        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    /* =====================================================
        UPDATE TONG TIEN
     ===================================================== */
    public boolean update(Invoice hd) {
        String sql = "UPDATE HOADON SET TongTien=? WHERE MaHD=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, hd.getTongTien());
            ps.setInt(2, hd.getMaHD());
            return ps.executeUpdate() > 0;

        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    /* =====================================================
        DELETE WITH ROLLBACK
     ===================================================== */
    public boolean deleteWithRollback(int maHD) {
        Invoice hd = findById(maHD);
        if (hd == null) return false;

        String sqlGetCT =
            "SELECT ct.MaSP, ct.SoLuong, " +
            "       CASE WHEN cd.MaCD IS NOT NULL THEN 'CD' " +
            "            WHEN r.MaSP  IS NOT NULL THEN 'ROM' " +
            "            ELSE 'UNKNOWN' END AS LoaiSP, " +
            "       cd.MaCD " +
            "FROM CTHOADON ct " +
            "LEFT JOIN CD  cd ON cd.MaSP = ct.MaSP AND cd.TrangThai = N'DaBan' " +
            "LEFT JOIN ROM r  ON r.MaSP  = ct.MaSP " +
            "WHERE ct.MaHD = ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Đọc chi tiết
                List<int[]> cdDaBan   = new ArrayList<>();
                List<int[]> romUpdate = new ArrayList<>();

                try (PreparedStatement ps = con.prepareStatement(sqlGetCT)) {
                    ps.setInt(1, maHD);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        String loai = rs.getString("LoaiSP");
                        if ("CD".equals(loai)) {
                            int maCD = rs.getInt("MaCD");
                            if (maCD > 0) cdDaBan.add(new int[]{maCD});
                        } else if ("ROM".equals(loai)) {
                            romUpdate.add(new int[]{rs.getInt("MaSP"), rs.getInt("SoLuong")});
                        }
                    }
                }

                // 2. Rollback CD → SanSang
                for (int[] cd : cdDaBan) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE CD SET TrangThai = N'SanSang' WHERE MaCD = ?")) {
                        ps.setInt(1, cd[0]);
                        ps.executeUpdate();
                    }
                }

                // 3. Rollback ROM → trừ SoLuotBan
                for (int[] rom : romUpdate) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE ROM SET SoLuotBan = " +
                            "CASE WHEN SoLuotBan-?<0 THEN 0 ELSE SoLuotBan-? END " +
                            "WHERE MaSP = ?")) {
                        ps.setInt(1, rom[1]);
                        ps.setInt(2, rom[1]);
                        ps.setInt(3, rom[0]);
                        ps.executeUpdate();
                    }
                }

                // 4. Rollback điểm KH
                int maKH = hd.getMaKH();
                if (maKH > 0) {
                    int diemDaTru = 0, diemDaCong = 0;

                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT COALESCE(SUM(SoDiem),0) FROM DIEM_LICHSU " +
                            "WHERE MaKH=? AND MaPT IS NULL " +
                            "  AND GhiChu LIKE N'%HD" + maHD + "%' AND Loai=N'TRU'")) {
                        ps.setInt(1, maKH);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) diemDaTru = rs.getInt(1);
                    }
                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT COALESCE(SUM(SoDiem),0) FROM DIEM_LICHSU " +
                            "WHERE MaKH=? AND MaPT IS NULL " +
                            "  AND GhiChu LIKE N'%HD" + maHD + "%' AND Loai=N'CONG'")) {
                        ps.setInt(1, maKH);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) diemDaCong = rs.getInt(1);
                    }

                    // delta dương = cộng vào KH (hoàn điểm đã trừ), âm = trừ (rút điểm đã cộng)
                    int delta = diemDaTru - diemDaCong;
                    if (delta != 0) {
                        try (PreparedStatement ps = con.prepareStatement(
                                "UPDATE KHACHHANG SET DiemTichLuy = " +
                                "CASE WHEN DiemTichLuy+?<0 THEN 0 ELSE DiemTichLuy+? END " +
                                "WHERE MaKH = ?")) {
                            ps.setInt(1, delta);
                            ps.setInt(2, delta);
                            ps.setInt(3, maKH);
                            ps.executeUpdate();
                        }
                    }

                    // 5. Xóa log điểm liên quan HĐ
                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM DIEM_LICHSU " +
                            "WHERE MaKH=? AND MaPT IS NULL " +
                            "  AND GhiChu LIKE N'%HD" + maHD + "%'")) {
                        ps.setInt(1, maKH);
                        ps.executeUpdate();
                    }
                }

                // 6. Xóa CTHOADON
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM CTHOADON WHERE MaHD = ?")) {
                    ps.setInt(1, maHD);
                    ps.executeUpdate();
                }

                // 7. Xóa HOADON
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM HOADON WHERE MaHD = ?")) {
                    ps.setInt(1, maHD);
                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
                    }
                }

                con.commit();
                return true;

            } catch (Exception ex) {
                con.rollback();
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /* =====================================================
        MAP OBJECT
     ===================================================== */
    private Invoice mapHoaDon(ResultSet rs) throws SQLException {
        Invoice hd = new Invoice();
        hd.setMaHD(rs.getInt("MaHD"));
        hd.setMaKH(rs.getInt("MaKH"));
        hd.setMaNV(rs.getInt("MaNV"));
        Timestamp ts = rs.getTimestamp("NgayLap");
        if (ts != null) hd.setNgayLap(ts.toLocalDateTime());
        hd.setTongTien(rs.getDouble("TongTien"));
        hd.setTrangThai(rs.getString("TrangThai"));
        return hd;
    }
}