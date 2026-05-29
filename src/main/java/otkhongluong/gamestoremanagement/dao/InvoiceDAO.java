package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.ChiTietHoaDon;
import otkhongluong.gamestoremanagement.model.Invoice;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InvoiceDAO {

    /* =====================================================
        INSERT ĐƠN GIẢN (giữ để tương thích)
     ===================================================== */
    public boolean insert(Invoice hd) {
        String sqlHD =
            "INSERT INTO HOADON(MaKH,MaNV,NgayLap,TongTien,TrangThai) " +
            "VALUES(?,?,?,?,?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        sqlHD, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, hd.getMaKH());
                    ps.setInt(2, hd.getMaNV());
                    ps.setTimestamp(3, Timestamp.valueOf(hd.getNgayLap()));
                    ps.setDouble(4, hd.getTongTien());
                    ps.setString(5, hd.getTrangThai());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) hd.setMaHD(rs.getInt(1));
                    }
                }
                insertChiTietBatch(conn, hd);
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* =====================================================
        INSERT ĐẦY ĐỦ — dùng cho createInvoice (có điểm, có race-check CD)
        Trả về maHD vừa tạo, hoặc -1 nếu lỗi.
        Caller truyền conn đang trong transaction để DAO không tự commit.
     ===================================================== */
    public int insertFull(Connection conn,
                          Integer maKH, Integer maNV,
                          double tongPhaiTra, int diemThuc, double giamTien,
                          Map<Integer, double[]> spMap) throws SQLException {

        String insHD =
            "INSERT INTO HOADON (MaKH,MaNV,NgayLap,TongTien,DiemSuDung,TienGiam,TrangThai) " +
            "VALUES (?,?,GETDATE(),?,?,?,N'DaThanhToan')";

        int maHD;
        try (PreparedStatement ps = conn.prepareStatement(insHD, Statement.RETURN_GENERATED_KEYS)) {
            if (maKH != null && maKH > 0) ps.setInt(1, maKH); else ps.setNull(1, Types.INTEGER);
            if (maNV  != null && maNV  > 0) ps.setInt(2, maNV);  else ps.setNull(2, Types.INTEGER);
            ps.setDouble(3, tongPhaiTra);
            ps.setInt(4, diemThuc);
            ps.setDouble(5, giamTien);
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (!gk.next()) throw new SQLException("Không tạo được hóa đơn!");
                maHD = gk.getInt(1);
            }
        }

        String insCT = "INSERT INTO CTHOADON (MaHD,MaSP,SoLuong,DonGia) VALUES (?,?,?,?)";
        for (Map.Entry<Integer, double[]> e : spMap.entrySet()) {
            try (PreparedStatement ps = conn.prepareStatement(insCT)) {
                ps.setInt(1, maHD);
                ps.setInt(2, e.getKey());
                ps.setInt(3, (int) e.getValue()[0]);
                ps.setDouble(4, e.getValue()[1]);
                ps.executeUpdate();
            }
        }
        return maHD;
    }

    /* =====================================================
        KIỂM TRA TRẠNG THÁI CD (race-condition check)
     ===================================================== */
    public String getCDTrangThai(Connection conn, int maCD) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT TrangThai FROM CD WHERE MaCD=?")) {
            ps.setInt(1, maCD);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("TrangThai") : null;
            }
        }
    }
    
    public boolean checkAndLockCD(Connection conn, int maCD) throws SQLException {
        String sql = "SELECT TrangThai FROM CD WITH (UPDLOCK, ROWLOCK) WHERE MaCD = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maCD);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                return "SanSang".equals(rs.getString("TrangThai"));
            }
        }
    }


    /* =====================================================
        CẬP NHẬT CD / ROM SAU KHI BÁN
     ===================================================== */
    public void markCDDaBan(Connection conn, int maCD) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE CD SET TrangThai=N'DaBan' WHERE MaCD=?")) {
            ps.setInt(1, maCD);
            ps.executeUpdate();
        }
    }

    public void increaseROMSoLuotBan(Connection conn, int maSP, int soLuong) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE ROM SET SoLuotBan=SoLuotBan+? WHERE MaSP=?")) {
            ps.setInt(1, soLuong);
            ps.setInt(2, maSP);
            ps.executeUpdate();
        }
    }

    /* =====================================================
        GHI LOG ĐIỂM
     ===================================================== */
    public void logDiem(Connection conn, int maKH, String loai, int soDiem, String ghiChu)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO DIEM_LICHSU (MaKH,Loai,SoDiem,GhiChu) VALUES (?,?,?,?)")) {
            ps.setInt(1, maKH);
            ps.setNString(2, loai);
            ps.setInt(3, soDiem);
            ps.setNString(4, ghiChu);
            ps.executeUpdate();
        }
    }

    /* =====================================================
        CẬP NHẬT ĐIỂM KHÁCH HÀNG
     ===================================================== */
    public void adjustDiem(Connection conn, int maKH, int delta) throws SQLException {
        if (delta == 0 || maKH <= 0) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE KHACHHANG SET DiemTichLuy=" +
                "CASE WHEN DiemTichLuy+?<0 THEN 0 ELSE DiemTichLuy+? END WHERE MaKH=?")) {
            ps.setInt(1, delta);
            ps.setInt(2, delta);
            ps.setInt(3, maKH);
            ps.executeUpdate();
        }
    }

    /* =====================================================
        LẤY MaNV HIỆN TẠI CỦA HĐ
     ===================================================== */
    public int getMaNVByHD(Connection conn, int maHD) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT MaNV FROM HOADON WHERE MaHD=?")) {
            ps.setInt(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("MaNV") : 0;
            }
        }
    }

    /* =====================================================
        CHUYỂN LOG ĐIỂM SANG KH MỚI (khi sửa HĐ đổi KH)
     ===================================================== */
    public void transferPointLogs(Connection conn, int maHD, int maKHCu, int maKHMoi)
            throws SQLException {
        String like = "%HĐ" + maHD + "%";
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE DIEM_LICHSU SET MaKH=? " +
                "WHERE MaKH=? AND MaPT IS NULL AND GhiChu LIKE N?")) {
            ps.setInt(1, maKHMoi);
            ps.setInt(2, maKHCu);
            ps.setNString(3, like);
            ps.executeUpdate();
        }
    }

    /* =====================================================
        FIND ALL
     ===================================================== */
    public List<Invoice> getAllHoaDon() {
        List<Invoice> list = new ArrayList<>();
        String sql =
            "SELECT hd.MaHD, hd.MaKH, hd.MaNV, hd.NgayLap, hd.TongTien, " +
            "       kh.HoTen, kh.SDT " +
            "FROM HOADON hd " +
            "LEFT JOIN KHACHHANG kh ON hd.MaKH = kh.MaKH " +
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
        FIND BY ID + DETAIL  (tên cũ: findById)
     ===================================================== */
    public Invoice getHoaDonById(int maHD) {
        String sql =
            "SELECT hd.*, kh.HoTen, kh.SDT " +
            "FROM HOADON hd " +
            "LEFT JOIN KHACHHANG kh ON hd.MaKH = kh.MaKH " +
            "WHERE hd.MaHD=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Invoice hd = mapHoaDon(rs);
                    hd.setDanhSachChiTiet(getChiTiet(conn, maHD));
                    return hd;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /* =====================================================
        LẤY MaCD ĐÃ BÁN CỦA 1 SP TRONG HĐ
     ===================================================== */
    public int getMaCDDaBan(Connection conn, int maHD, int maSP) throws SQLException {
        String sql =
            "SELECT cd.MaCD FROM CTHOADON ct " +
            "JOIN CD cd ON cd.MaSP=ct.MaSP " +
            "WHERE ct.MaHD=? AND ct.MaSP=? AND cd.TrangThai=N'DaBan' " +
            "ORDER BY cd.MaCD ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHD);
            ps.setInt(2, maSP);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("MaCD") : -1;
            }
        }
    }

    /* =====================================================
        SỬA HĐ — cập nhật KH / NV
     ===================================================== */
    public void updateKHNV(Connection conn, int maHD, int maKH, int maNV) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE HOADON SET MaKH=?,MaNV=? WHERE MaHD=?")) {
            ps.setInt(1, maKH);
            ps.setInt(2, maNV);
            ps.setInt(3, maHD);
            ps.executeUpdate();
        }
    }

    /* =====================================================
        SỬA HĐ — cập nhật ngày lập
     ===================================================== */
    public void updateNgayLap(Connection conn, int maHD, LocalDate ngay) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE HOADON SET NgayLap=? WHERE MaHD=?")) {
            ps.setTimestamp(1, Timestamp.valueOf(ngay.atStartOfDay()));
            ps.setInt(2, maHD);
            ps.executeUpdate();
        }
    }

    /* =====================================================
        SỬA HĐ — cập nhật tổng tiền
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

    public void updateTongTien(Connection conn, int maHD, double tongTien) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE HOADON SET TongTien=? WHERE MaHD=?")) {
            ps.setDouble(1, tongTien);
            ps.setInt(2, maHD);
            ps.executeUpdate();
        }
    }

    /* =====================================================
        SỬA HĐ — cập nhật trạng thái
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
        SỬA HĐ — xóa / thêm / upsert chi tiết
     ===================================================== */
    public void deleteCTHoaDon(Connection conn, int maHD, int maSP) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM CTHOADON WHERE MaHD=? AND MaSP=?")) {
            ps.setInt(1, maHD);
            ps.setInt(2, maSP);
            ps.executeUpdate();
        }
    }

    public void upsertCTHoaDon(Connection conn, int maHD, int maSP,
                                int soLuong, double donGia) throws SQLException {
        String sql =
            "IF EXISTS (SELECT 1 FROM CTHOADON WHERE MaHD=? AND MaSP=?) " +
            "  UPDATE CTHOADON SET SoLuong=SoLuong+?,DonGia=? WHERE MaHD=? AND MaSP=? " +
            "ELSE " +
            "  INSERT INTO CTHOADON(MaHD,MaSP,SoLuong,DonGia) VALUES(?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHD); ps.setInt(2, maSP);
            ps.setInt(3, soLuong); ps.setDouble(4, donGia);
            ps.setInt(5, maHD);   ps.setInt(6, maSP);
            ps.setInt(7, maHD);   ps.setInt(8, maSP);
            ps.setInt(9, soLuong); ps.setDouble(10, donGia);
            ps.executeUpdate();
        }
    }

    /* =====================================================
        SỬA HĐ — rollback CD / ROM khi xóa SP khỏi HĐ
     ===================================================== */
    public void markCDSanSang(Connection conn, int maCD) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE CD SET TrangThai=N'SanSang' WHERE MaCD=?")) {
            ps.setInt(1, maCD);
            ps.executeUpdate();
        }
    }

    public void decreaseROMSoLuotBan(Connection conn, int maSP, int soLuong) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE ROM SET SoLuotBan=" +
                "CASE WHEN SoLuotBan-?<0 THEN 0 ELSE SoLuotBan-? END WHERE MaSP=?")) {
            ps.setInt(1, soLuong);
            ps.setInt(2, soLuong);
            ps.setInt(3, maSP);
            ps.executeUpdate();
        }
    }

    /* =====================================================
        ROM LINK
     ===================================================== */
    public String getROMLink(int maSP) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT LinkLuuTru FROM ROM WHERE MaSP=?")) {
            ps.setInt(1, maSP);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("LinkLuuTru") : null;
            }
        } catch (SQLException ignored) { }
        return null;
    }

    /* =====================================================
        ĐIỂM GIẢM TỪ PHIẾU THUÊ
     ===================================================== */
    public double getGiamDiemByPT(Connection conn, int maPT) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COALESCE(SUM(SoDiem),0) FROM DIEM_LICHSU " +
                "WHERE MaPT=? AND Loai='TRU'")) {
            ps.setInt(1, maPT);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /* =====================================================
        DELETE WITH ROLLBACK  (tên cũ: deleteWithRollback)
     ===================================================== */
    public boolean deleteHoaDon(int maHD) {
        Invoice hd = getHoaDonById(maHD);
        if (hd == null) return false;

        String sqlGetCT =
            "SELECT ct.MaSP, ct.SoLuong, " +
            "       CASE WHEN cd.MaCD IS NOT NULL THEN 'CD' " +
            "            WHEN r.MaSP  IS NOT NULL THEN 'ROM' " +
            "            ELSE 'UNKNOWN' END AS LoaiSP, " +
            "       cd.MaCD " +
            "FROM CTHOADON ct " +
            "LEFT JOIN CD  cd ON cd.MaSP=ct.MaSP AND cd.TrangThai=N'DaBan' " +
            "LEFT JOIN ROM r  ON r.MaSP=ct.MaSP " +
            "WHERE ct.MaHD=?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                List<int[]> cdDaBan   = new ArrayList<>();
                List<int[]> romUpdate = new ArrayList<>();

                try (PreparedStatement ps = con.prepareStatement(sqlGetCT)) {
                    ps.setInt(1, maHD);
                    try (ResultSet rs = ps.executeQuery()) {
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
                }

                for (int[] cd : cdDaBan)  markCDSanSang(con, cd[0]);
                for (int[] rom : romUpdate) decreaseROMSoLuotBan(con, rom[0], rom[1]);

                // Rollback điểm KH
                int maKH = hd.getMaKH();
                if (maKH > 0) {
                    String like = "%HD" + maHD + "%";
                    int diemTru  = querySum(con, maKH, like, "TRU");
                    int diemCong = querySum(con, maKH, like, "CONG");
                    int delta    = diemTru - diemCong;
                    if (delta != 0) adjustDiem(con, maKH, delta);

                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM DIEM_LICHSU WHERE MaKH=? AND MaPT IS NULL AND GhiChu LIKE ?")) {
                        ps.setInt(1, maKH);
                        ps.setString(2, like);
                        ps.executeUpdate();
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM CTHOADON WHERE MaHD=?")) {
                    ps.setInt(1, maHD);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM HOADON WHERE MaHD=?")) {
                    ps.setInt(1, maHD);
                    if (ps.executeUpdate() == 0) { con.rollback(); return false; }
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
        // THÊM:
        hd.setTenKhachHang(rs.getString("HoTen"));
        hd.setSoDienThoai(rs.getString("SDT"));
        return hd;
    }

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
            "WHERE ct.MaHD=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
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
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private void insertChiTietBatch(Connection conn, Invoice hd) throws SQLException {
        String sqlCT = "INSERT INTO CTHOADON(MaHD,MaSP,SoLuong,DonGia) VALUES(?,?,?,?)";
        String sqlCD = "UPDATE CD SET TrangThai=N'DaBan' WHERE MaSP=? AND TrangThai=N'SanSang'";
        try (PreparedStatement ps   = conn.prepareStatement(sqlCT);
             PreparedStatement psCD = conn.prepareStatement(sqlCD)) {
            for (ChiTietHoaDon ct : hd.getDanhSachChiTiet()) {
                ps.setInt(1, hd.getMaHD()); ps.setInt(2, ct.getMaSP());
                ps.setInt(3, ct.getSoLuong()); ps.setDouble(4, ct.getDonGia());
                ps.addBatch();
                psCD.setInt(1, ct.getMaSP());
                psCD.addBatch();
            }
            ps.executeBatch();
            psCD.executeBatch();
        }
    }

    private int querySum(Connection con, int maKH, String like, String loai) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COALESCE(SUM(SoDiem),0) FROM DIEM_LICHSU " +
                "WHERE MaKH=? AND MaPT IS NULL AND GhiChu LIKE ? AND Loai=?")) {
            ps.setInt(1, maKH);
            ps.setString(2, like);
            ps.setNString(3, loai);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}