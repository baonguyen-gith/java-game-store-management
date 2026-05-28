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
     * Cộng điểm vào KHACHHANG + ghi DIEM_LICHSU trong 1 transaction.
     * @return true nếu thành công
     */
    public boolean congDiem(int maKH, int diem, String ghiChu) {
        String note = (ghiChu != null && !ghiChu.isBlank()) ? ghiChu : "Cộng điểm thủ công";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE KHACHHANG SET DiemTichLuy = DiemTichLuy + ? WHERE MaKH = ?")) {
                    ps.setInt(1, diem);
                    ps.setInt(2, maKH);
                    if (ps.executeUpdate() == 0) { conn.rollback(); return false; }
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO DIEM_LICHSU (MaKH, MaPT, Loai, SoDiem, Ngay, GhiChu) " +
                        "VALUES (?, NULL, N'CONG', ?, GETDATE(), ?)")) {
                    ps.setInt(1, maKH);
                    ps.setInt(2, diem);
                    ps.setString(3, note);
                    ps.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("congDiem error: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("congDiem error: " + e.getMessage());
            return false;
        }
    }

    // ==================== TRỪ ĐIỂM ====================

    /**
     * Trừ điểm khỏi KHACHHANG (không để âm) + ghi DIEM_LICHSU trong 1 transaction.
     * @return null nếu thành công, thông báo lỗi nếu thất bại
     */
    public String truDiem(int maKH, int diem, String ghiChu) {
        String note = (ghiChu != null && !ghiChu.isBlank()) ? ghiChu : "Trừ điểm thủ công";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int diemHienCo = 0;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT DiemTichLuy FROM KHACHHANG WHERE MaKH = ?")) {
                    ps.setInt(1, maKH);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { conn.rollback(); return "Không tìm thấy KH MaKH=" + maKH; }
                        diemHienCo = rs.getInt("DiemTichLuy");
                    }
                }
                if (diemHienCo < diem) {
                    conn.rollback();
                    return "Điểm không đủ! Hiện có: " + diemHienCo + ", cần trừ: " + diem;
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE KHACHHANG SET DiemTichLuy = DiemTichLuy - ? WHERE MaKH = ?")) {
                    ps.setInt(1, diem);
                    ps.setInt(2, maKH);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO DIEM_LICHSU (MaKH, MaPT, Loai, SoDiem, Ngay, GhiChu) " +
                        "VALUES (?, NULL, N'TRU', ?, GETDATE(), ?)")) {
                    ps.setInt(1, maKH);
                    ps.setInt(2, diem);
                    ps.setString(3, note);
                    ps.executeUpdate();
                }
                conn.commit();
                return null;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("truDiem error: " + e.getMessage());
                return e.getMessage();
            }
        } catch (SQLException e) {
            System.err.println("truDiem error: " + e.getMessage());
            return e.getMessage();
        }
    }

    // ==================== TRỪ ĐIỂM (có MaPT — khi tạo phiếu thuê) ====================

    /**
     * Trừ điểm khi tạo phiếu thuê — ghi MaPT vào DIEM_LICHSU.
     * @return null nếu thành công, thông báo lỗi nếu thất bại
     */
    public String truDiemVoiMaPT(int maKH, int diem, int maPT, String ghiChu) {
        String note = (ghiChu != null && !ghiChu.isBlank()) ? ghiChu : "Trừ điểm thủ công";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int diemHienCo = 0;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT DiemTichLuy FROM KHACHHANG WHERE MaKH = ?")) {
                    ps.setInt(1, maKH);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { conn.rollback(); return "Không tìm thấy KH MaKH=" + maKH; }
                        diemHienCo = rs.getInt("DiemTichLuy");
                    }
                }
                if (diemHienCo < diem) {
                    conn.rollback();
                    return "Điểm không đủ! Hiện có: " + diemHienCo + ", cần trừ: " + diem;
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE KHACHHANG SET DiemTichLuy = DiemTichLuy - ? WHERE MaKH = ?")) {
                    ps.setInt(1, diem);
                    ps.setInt(2, maKH);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO DIEM_LICHSU (MaKH, MaPT, Loai, SoDiem, Ngay, GhiChu) " +
                        "VALUES (?, ?, N'TRU', ?, GETDATE(), ?)")) {
                    ps.setInt(1, maKH);
                    ps.setInt(2, maPT);
                    ps.setInt(3, diem);
                    ps.setString(4, note);
                    ps.executeUpdate();
                }
                conn.commit();
                return null;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("truDiemVoiMaPT error: " + e.getMessage());
                return e.getMessage();
            }
        } catch (SQLException e) {
            System.err.println("truDiemVoiMaPT error: " + e.getMessage());
            return e.getMessage();
        }
    }

    // ==================== SỬA ĐIỂM ====================

    /**
     * Set điểm về giá trị mới + ghi DIEM_LICHSU chênh lệch trong 1 transaction.
     * @return null nếu thành công, thông báo lỗi nếu thất bại
     */
    public String suaDiem(int maKH, int diemMoi, String ghiChu) {
        String note = (ghiChu != null && !ghiChu.isBlank()) ? ghiChu : "Điều chỉnh điểm thủ công";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int diemCu = 0;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT DiemTichLuy FROM KHACHHANG WHERE MaKH = ?")) {
                    ps.setInt(1, maKH);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { conn.rollback(); return "Không tìm thấy KH MaKH=" + maKH; }
                        diemCu = rs.getInt("DiemTichLuy");
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE KHACHHANG SET DiemTichLuy = ? WHERE MaKH = ?")) {
                    ps.setInt(1, diemMoi);
                    ps.setInt(2, maKH);
                    ps.executeUpdate();
                }
                int chenh = diemMoi - diemCu;
                String loai = chenh >= 0 ? "CONG" : "TRU";
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO DIEM_LICHSU (MaKH, MaPT, Loai, SoDiem, Ngay, GhiChu) " +
                        "VALUES (?, NULL, ?, ?, GETDATE(), ?)")) {
                    ps.setInt(1, maKH);
                    ps.setString(2, loai);
                    ps.setInt(3, Math.abs(chenh));
                    ps.setString(4, note + " (" + diemCu + " → " + diemMoi + ")");
                    ps.executeUpdate();
                }
                conn.commit();
                return null;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("suaDiem error: " + e.getMessage());
                return e.getMessage();
            }
        } catch (SQLException e) {
            System.err.println("suaDiem error: " + e.getMessage());
            return e.getMessage();
        }
    }

    // ==================== XÓA 1 BẢN GHI LỊCH SỬ ====================

    /**
     * Xóa bản ghi DIEM_LICHSU + hoàn tác điểm tương ứng trong 1 transaction.
     * @return null nếu thành công, thông báo lỗi nếu thất bại
     */
    public String xoaLichSu(int maLS) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int maKH = 0, soDiem = 0;
                String loai = "";
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT MaKH, Loai, SoDiem FROM DIEM_LICHSU WHERE MaLS = ?")) {
                    ps.setInt(1, maLS);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { conn.rollback(); return "Không tìm thấy bản ghi MaLS=" + maLS; }
                        maKH   = rs.getInt("MaKH");
                        loai   = rs.getString("Loai");
                        soDiem = rs.getInt("SoDiem");
                    }
                }
                // Hoàn tác: CONG → trừ lại; TRU → cộng lại
                String updateSql = "CONG".equalsIgnoreCase(loai)
                    ? "UPDATE KHACHHANG SET DiemTichLuy = DiemTichLuy - ? WHERE MaKH = ?"
                    : "UPDATE KHACHHANG SET DiemTichLuy = DiemTichLuy + ? WHERE MaKH = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, soDiem);
                    ps.setInt(2, maKH);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM DIEM_LICHSU WHERE MaLS = ?")) {
                    ps.setInt(1, maLS);
                    ps.executeUpdate();
                }
                conn.commit();
                return null;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("xoaLichSu error: " + e.getMessage());
                return e.getMessage();
            }
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