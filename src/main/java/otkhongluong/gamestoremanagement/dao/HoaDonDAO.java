package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.HoaDon;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {
    public boolean insert(HoaDon hd) {
        String sqlHD = "INSERT INTO HOADON (MaKH, TongTien) VALUES (?, ?)";
        String sqlCTHD = "INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement psHD = null;
        PreparedStatement psCTHD = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); 
            
            psHD = conn.prepareStatement(sqlHD, new String[]{"MaHD"});
            psHD.setInt(1, hd.getMaKH());
            psHD.setDouble(2, hd.getTongTien());
            psHD.executeUpdate();
            
            int generatedMaHD = 0;
            rs = psHD.getGeneratedKeys();
            if (rs.next()) {
                generatedMaHD = rs.getInt(1);
            }
            
            if (generatedMaHD > 0 && hd.getDanhSachChiTiet() != null) {
                psCTHD = conn.prepareStatement(sqlCTHD);
                for (HoaDon.ChiTietHoaDon ct : hd.getDanhSachChiTiet()) {
                    psCTHD.setInt(1, generatedMaHD);
                    psCTHD.setInt(2, ct.getMaSP());
                    psCTHD.setInt(3, ct.getSoLuong());
                    psCTHD.setDouble(4, ct.getDonGia());
                    psCTHD.addBatch(); // Đưa vào hàng chờ Batch
                }
                psCTHD.executeBatch(); 
            }
            
            conn.commit(); 
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); 
                    System.err.println("Lỗi Insert Hóa Đơn. Đã Rollback dữ liệu!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (psCTHD != null) psCTHD.close(); } catch (SQLException e) {}
            try { if (psHD != null) psHD.close(); } catch (SQLException e) {}
            DBConnection.closeConnection(conn);
        }
        return false;
    }

    public boolean update(HoaDon hd) {
        String sql = "UPDATE HOADON SET MaKH = ?, TongTien = ? WHERE MaHD = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, hd.getMaKH());
            ps.setDouble(2, hd.getTongTien());
            ps.setInt(3, hd.getMaHD());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int maHD) {
        String sqlCTHD = "DELETE FROM CTHOADON WHERE MaHD = ?";
        String sqlHD = "DELETE FROM HOADON WHERE MaHD = ?";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            try (PreparedStatement psCT = conn.prepareStatement(sqlCTHD)) {
                psCT.setInt(1, maHD);
                psCT.executeUpdate();
            }
            
            try (PreparedStatement psHD = conn.prepareStatement(sqlHD)) {
                psHD.setInt(1, maHD);
                psHD.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
        return false;
    }

    public HoaDon findById(int maHD) {
        String sql = "SELECT * FROM HOADON WHERE MaHD = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToHoaDon(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<HoaDon> findAll() {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM HOADON ORDER BY MaHD DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToHoaDon(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<HoaDon> filterByDate(String fromDate, String toDate) {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM HOADON WHERE NgayLap >= TO_DATE(?, 'YYYY-MM-DD') " +
                     "AND NgayLap < TO_DATE(?, 'YYYY-MM-DD') + 1 ORDER BY NgayLap DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, fromDate);
            ps.setString(2, toDate);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToHoaDon(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private HoaDon mapResultSetToHoaDon(ResultSet rs) throws SQLException {
        HoaDon hd = new HoaDon();
        hd.setMaHD(rs.getInt("MaHD"));
        hd.setMaKH(rs.getInt("MaKH"));
        
        Timestamp ts = rs.getTimestamp("NgayLap");
        if (ts != null) {
            hd.setNgayLap(ts.toLocalDateTime());
        }
        
        hd.setTongTien(rs.getDouble("TongTien"));
        return hd;
    }
}