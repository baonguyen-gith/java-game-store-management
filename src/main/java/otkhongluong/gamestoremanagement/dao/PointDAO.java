package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.Point;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO thao tác bảng DIEM_LICHSU và KHACHHANG (phần điểm).
 * Gọi Stored Procedure đã tạo trong diem_lichsu_procedures.sql.
 */
public class PointDAO {

    // ==================== LẤY LỊCH SỬ THEO MÃ KH ====================

    public List<Point> findByMaKH(int maKH) {
        List<Point> list = new ArrayList<>();
        String sql = "SELECT * FROM DIEM_LICHSU WHERE MaKH = ? ORDER BY Ngay DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maKH);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ==================== CỘNG ĐIỂM ====================

    /**
     * Gọi SP_CONG_DIEM: cộng điểm vào KHACHHANG + ghi DIEM_LICHSU.
     * @return true nếu thành công
     */
    public boolean congDiem(int maKH, int diem, String ghiChu) {
        String sql = "{CALL SP_CONG_DIEM(?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, maKH);
            cs.setInt(2, diem);
            cs.setString(3, ghiChu != null && !ghiChu.isBlank() ? ghiChu : "Cộng điểm thủ công");
            cs.execute();
            return true;

        } catch (SQLException e) {
            System.err.println("congDiem error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ==================== TRỪ ĐIỂM ====================

    /**
     * Gọi SP_TRU_DIEM: trừ điểm (không để âm) + ghi DIEM_LICHSU.
     * @return thông báo lỗi nếu có, null nếu thành công
     */
    public String truDiem(int maKH, int diem, String ghiChu) {
        String sql = "{CALL SP_TRU_DIEM(?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, maKH);
            cs.setInt(2, diem);
            cs.setString(3, ghiChu != null && !ghiChu.isBlank() ? ghiChu : "Trừ điểm thủ công");
            cs.execute();
            return null; // thành công

        } catch (SQLException e) {
            // SQL Server ném RAISERROR → bắt ở đây
            System.err.println("truDiem error: " + e.getMessage());
            return e.getMessage();
        }
    }

    // ==================== SỬA ĐIỂM ====================

    /**
     * Gọi SP_SUA_DIEM: set điểm về giá trị mới + ghi DIEM_LICHSU chênh lệch.
     * @return thông báo lỗi nếu có, null nếu thành công
     */
    public String suaDiem(int maKH, int diemMoi, String ghiChu) {
        String sql = "{CALL SP_SUA_DIEM(?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, maKH);
            cs.setInt(2, diemMoi);
            cs.setString(3, ghiChu != null && !ghiChu.isBlank() ? ghiChu : "Điều chỉnh điểm thủ công");
            cs.execute();
            return null;

        } catch (SQLException e) {
            System.err.println("suaDiem error: " + e.getMessage());
            return e.getMessage();
        }
    }

    // ==================== XÓA 1 BẢN GHI LỊCH SỬ ====================

    /**
     * Gọi SP_XOA_LICHSU_DIEM: xóa bản ghi + hoàn tác điểm tương ứng.
     * @return thông báo lỗi nếu có, null nếu thành công
     */
    public String xoaLichSu(int maLS) {
        String sql = "{CALL SP_XOA_LICHSU_DIEM(?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setInt(1, maLS);
            cs.execute();
            return null;

        } catch (SQLException e) {
            System.err.println("xoaLichSu error: " + e.getMessage());
            return e.getMessage();
        }
    }

    // ==================== MAPPER ====================

    private Point map(ResultSet rs) throws SQLException {
        Point d = new Point();
        d.setMaLS(rs.getInt("MaLS"));
        d.setMaKH(rs.getInt("MaKH"));

        int maPT = rs.getInt("MaPT");
        d.setMaPT(rs.wasNull() ? null : maPT);

        d.setLoai(rs.getString("Loai"));
        d.setSoDiem(rs.getInt("SoDiem"));
        d.setGhiChu(rs.getString("GhiChu"));

        Timestamp ts = rs.getTimestamp("Ngay");
        if (ts != null) d.setNgay(ts.toLocalDateTime());

        return d;
    }
    
    public int sumDiemTruByMaPT(int maPT) {
        String sql = "SELECT COALESCE(SUM(SoDiem), 0) FROM DIEM_LICHSU WHERE MaPT = ? AND Loai = N'TRU'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPT);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}