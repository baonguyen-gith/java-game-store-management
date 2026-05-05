package otkhongluong.gamestoremanagement.dao;
import otkhongluong.gamestoremanagement.util.DBConnection;
import java.sql.*;
import java.util.*;

public class TransactionDAO {

    public List<Object[]> findAll(){

        List<Object[]> list = new ArrayList<>();

        String sql =
            "SELECT CONCAT('HD', MaHD) AS ID, N'Hóa đơn' AS Loai, NgayLap AS Ngay, TongTien AS Tien FROM HOADON " +
            "UNION ALL " +
            "SELECT CONCAT('PT', MaPT), N'Phiếu thuê', NgayThue, TienCoc FROM PHIEUTHUE";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){

            while(rs.next()){

                list.add(new Object[]{
                        rs.getString("ID"),
                        rs.getString("Loai"),
                        rs.getTimestamp("Ngay"),
                        rs.getDouble("Tien"),
                        "Xem"
                });
            }

        }catch(Exception e){ e.printStackTrace(); }

        return list;
    }
}