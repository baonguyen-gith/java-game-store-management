package otkhongluong.gamestoremanagement.dao;
import otkhongluong.gamestoremanagement.util.DBConnection;
import otkhongluong.gamestoremanagement.model.TransactionDTO;
import java.sql.*;
import java.util.*;

public class TransactionDAO {

    public List<TransactionDTO> findAll() {
      List<TransactionDTO> list = new ArrayList<>();
        String sql =
            "SELECT CONCAT('HD', hd.MaHD) AS ID, N'Hóa đơn' AS Loai, " +
            "       hd.MaNV, kh.HoTen AS TenKH, " +
            "       hd.NgayLap AS Ngay, hd.TongTien AS Tien " +
            "FROM HOADON hd " +
            "LEFT JOIN KHACHHANG kh ON hd.MaKH = kh.MaKH " +
            "UNION ALL " +
            "SELECT CONCAT('PT', pt.MaPT), N'Phiếu thuê', " +
            "       MIN(ct.MaNV), kh.HoTen, " +
            "       pt.NgayThue, pt.TienCoc " +
            "FROM PHIEUTHUE pt " +
            "LEFT JOIN KHACHHANG kh ON pt.MaKH = kh.MaKH " +
            "LEFT JOIN CTPHIEUTHUE ct ON pt.MaPT = ct.MaPT " +
            "GROUP BY pt.MaPT, kh.HoTen, pt.NgayThue, pt.TienCoc";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TransactionDTO dto = new TransactionDTO();
                dto.setId(rs.getString("ID"));
                dto.setLoai(rs.getString("Loai"));
                dto.setMaNV(rs.getInt("MaNV"));
                dto.setTenKhachHang(rs.getString("TenKH"));
                Timestamp ts = rs.getTimestamp("Ngay");
                if (ts != null) dto.setNgay(ts.toLocalDateTime());
                dto.setTien(rs.getDouble("Tien"));
                list.add(dto);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}