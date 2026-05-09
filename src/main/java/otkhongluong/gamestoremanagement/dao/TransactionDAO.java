package otkhongluong.gamestoremanagement.dao;
import otkhongluong.gamestoremanagement.util.DBConnection;
import java.sql.*;
import java.util.*;

public class TransactionDAO {

    public List<Object[]> findAll(){
        List<Object[]> list = new ArrayList<>();
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

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){
            while(rs.next()){
                list.add(new Object[]{
                    rs.getString("ID"),
                    rs.getString("Loai"),
                    rs.getInt("MaNV"),        // [2] int
                    rs.getString("TenKH"),    // [3] String
                    rs.getTimestamp("Ngay"),  // [4] Timestamp
                    rs.getDouble("Tien"),     // [5] Double
                    "Xem"                     // [6]
                });
            }
        }catch(Exception e){ e.printStackTrace(); }
        return list;
    }
}