package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.PhieuThue;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhieuThueDAO {
    public boolean insert(PhieuThue pt) {
        String sqlPT = "INSERT INTO PHIEUTHUE (MaKH, NgayTraDuKien, TienCoc) VALUES (?, ?, ?)";
        String sqlCTPT = "INSERT INTO CTPHIEUTHUE (MaPT, MaSP) VALUES (?, ?)";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement psPT = conn.prepareStatement(sqlPT, new String[]{"MaPT"});
            psPT.setInt(1, pt.getMaKH());
            psPT.setTimestamp(2, Timestamp.valueOf(pt.getNgayTraDuKien()));
            psPT.setDouble(3, pt.getTienCoc());
            psPT.executeUpdate();
            
            ResultSet rs = psPT.getGeneratedKeys();
            int generatedMaPT = 0;
            if (rs.next()) {
                generatedMaPT = rs.getInt(1);
            }
            
            if (generatedMaPT > 0 && pt.getDanhSachChiTiet() != null) {
                PreparedStatement psCT = conn.prepareStatement(sqlCTPT);
                for (PhieuThue.CTPhieuThue ct : pt.getDanhSachChiTiet()) {
                    psCT.setInt(1, generatedMaPT);
                    psCT.setInt(2, ct.getMaSP());
                    psCT.addBatch();
                }
                psCT.executeBatch();
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

    public boolean updateNgayTra(int maPT, Timestamp ngayTraThucTe) {
        String sql = "UPDATE PHIEUTHUE SET NgayTraThucTe = ? WHERE MaPT = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setTimestamp(1, ngayTraThucTe);
            ps.setInt(2, maPT);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int maPT) {
        String sqlCT = "DELETE FROM CTPHIEUTHUE WHERE MaPT = ?";
        String sqlPT = "DELETE FROM PHIEUTHUE WHERE MaPT = ?";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            try (PreparedStatement psCT = conn.prepareStatement(sqlCT)) {
                psCT.setInt(1, maPT);
                psCT.executeUpdate();
            }
            try (PreparedStatement psPT = conn.prepareStatement(sqlPT)) {
                psPT.setInt(1, maPT);
                psPT.executeUpdate();
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

    public PhieuThue findById(int maPT) {
        String sql = "SELECT * FROM PHIEUTHUE WHERE MaPT = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPT);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<PhieuThue> findAll() {
        List<PhieuThue> list = new ArrayList<>();
        String sql = "SELECT * FROM PHIEUTHUE ORDER BY MaPT DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private PhieuThue mapResultSet(ResultSet rs) throws SQLException {
        PhieuThue pt = new PhieuThue();
        pt.setMaPT(rs.getInt("MaPT"));
        pt.setMaKH(rs.getInt("MaKH"));
        if(rs.getTimestamp("NgayThue") != null) pt.setNgayThue(rs.getTimestamp("NgayThue").toLocalDateTime());
        if(rs.getTimestamp("NgayTraDuKien") != null) pt.setNgayTraDuKien(rs.getTimestamp("NgayTraDuKien").toLocalDateTime());
        if(rs.getTimestamp("NgayTraThucTe") != null) pt.setNgayTraThucTe(rs.getTimestamp("NgayTraThucTe").toLocalDateTime());
        pt.setTienCoc(rs.getDouble("TienCoc"));
        pt.setTienPhat(rs.getDouble("TienPhat"));
        return pt;
    }
}