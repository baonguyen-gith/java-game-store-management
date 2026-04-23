package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.RevenueDTO;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {
    public List<RevenueDTO> getDoanhThuTheoNgay() {
        List<RevenueDTO> list = new ArrayList<>();
        String sql = "SELECT TO_CHAR(NgayLap, 'YYYY-MM-DD') AS ThoiGian, " +
                     "COUNT(MaHD) AS SoDonHang, SUM(TongTien) AS TongDoanhThu " +
                     "FROM HOADON GROUP BY TO_CHAR(NgayLap, 'YYYY-MM-DD') ORDER BY ThoiGian DESC";
        
        return executeReportQuery(sql);
    }

    public List<RevenueDTO> getDoanhThuTheoThang() {
        List<RevenueDTO> list = new ArrayList<>();
        String sql = "SELECT TO_CHAR(NgayLap, 'YYYY-MM') AS ThoiGian, " +
                     "COUNT(MaHD) AS SoDonHang, SUM(TongTien) AS TongDoanhThu " +
                     "FROM HOADON GROUP BY TO_CHAR(NgayLap, 'YYYY-MM') ORDER BY ThoiGian DESC";
        
        return executeReportQuery(sql);
    }

    public List<RevenueDTO> getDoanhThuTheoNam() {
        List<RevenueDTO> list = new ArrayList<>();
        String sql = "SELECT TO_CHAR(NgayLap, 'YYYY') AS ThoiGian, " +
                     "COUNT(MaHD) AS SoDonHang, SUM(TongTien) AS TongDoanhThu " +
                     "FROM HOADON GROUP BY TO_CHAR(NgayLap, 'YYYY') ORDER BY ThoiGian DESC";
        
        return executeReportQuery(sql);
    }

    private List<RevenueDTO> executeReportQuery(String sql) {
        List<RevenueDTO> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                RevenueDTO dto = new RevenueDTO();
                dto.setThoiGian(rs.getString("ThoiGian"));
                dto.setSoDonHang(rs.getInt("SoDonHang"));
                dto.setTongDoanhThu(rs.getDouble("TongDoanhThu"));
                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}