package otkhongluong.gamestoremanagement.dao;

import otkhongluong.gamestoremanagement.model.HoaDon;
import otkhongluong.gamestoremanagement.model.HoaDon.ChiTietHoaDon;
import otkhongluong.gamestoremanagement.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {

    /* =====================================================
        INSERT
     ===================================================== */
    public boolean insert(HoaDon hd) {

        String sql =
        "INSERT INTO HOADON(MaKH,MaNV,NgayLap,TongTien,TrangThai) "
      + "VALUES(?,?,?,?,?)";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps =
                    conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, hd.getMaKH());
            ps.setInt(2, hd.getMaNV());
            ps.setTimestamp(3,
                    Timestamp.valueOf(hd.getNgayLap()));
            ps.setDouble(4, hd.getTongTien());
            ps.setString(5, hd.getTrangThai());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next())
                hd.setMaHD(rs.getInt(1));

            insertChiTiet(conn, hd);

            return true;

        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    /* =====================================================
        INSERT DETAIL
     ===================================================== */
    private void insertChiTiet(Connection conn, HoaDon hd)
            throws SQLException {

        String sql =
        "INSERT INTO CHITIETHOADON(MaHD,MaSP,SoLuong,DonGia) "
      + "VALUES(?,?,?,?)";

        PreparedStatement ps = conn.prepareStatement(sql);

        for(ChiTietHoaDon ct : hd.getDanhSachChiTiet()){

            ps.setInt(1, hd.getMaHD());
            ps.setInt(2, ct.getMaSP());
            ps.setInt(3, ct.getSoLuong());
            ps.setDouble(4, ct.getDonGia());

            ps.addBatch();
        }

        ps.executeBatch();
    }

    /* =====================================================
        FIND ALL
     ===================================================== */
    public List<HoaDon> findAll(){

        List<HoaDon> list = new ArrayList<>();

        String sql =
            "SELECT hd.MaHD,hd.MaNV,hd.NgayLap,hd.TongTien,"
          + "kh.HoTen,kh.SDT "
          + "FROM HOADON hd "
          + "JOIN KHACHHANG kh ON hd.MaKH=kh.MaKH "
          + "ORDER BY hd.MaHD DESC";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){

            while(rs.next()){

                HoaDon hd = new HoaDon();

                hd.setMaHD(rs.getInt("MaHD"));
                hd.setMaNV(rs.getInt("MaNV"));
                hd.setTongTien(rs.getDouble("TongTien"));

                hd.setTenKhachHang(rs.getString("HoTen"));
                hd.setSoDienThoai(rs.getString("SDT"));

                Timestamp ts = rs.getTimestamp("NgayLap");
                if(ts != null)
                    hd.setNgayLap(ts.toLocalDateTime());

                list.add(hd);
            }

        }catch(Exception e){e.printStackTrace();}

        return list;
    }

    /* =====================================================
        FIND BY ID + DETAIL ⭐
     ===================================================== */
    public HoaDon findById(int maHD){

        String sql = "SELECT * FROM HOADON WHERE MaHD=?";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setInt(1,maHD);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){

                HoaDon hd = mapHoaDon(rs);

                hd.setDanhSachChiTiet(
                        getChiTiet(conn, maHD)
                );

                return hd;
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    /* =====================================================
        LOAD DETAIL
     ===================================================== */
    private List<ChiTietHoaDon> getChiTiet(Connection conn, int maHD){

        List<ChiTietHoaDon> list = new ArrayList<>();

        String sql =
            "SELECT " +
            "    g.TenGame, " +
            "    CASE " +
            "        WHEN EXISTS (SELECT 1 FROM CD WHERE CD.MaSP = sp.MaSP) THEN N'CD' " +
            "        WHEN EXISTS (SELECT 1 FROM ROM WHERE ROM.MaSP = sp.MaSP) THEN N'ROM' " +
            "        ELSE N'Không rõ' " +
            "    END AS LoaiSanPham, " +
            "    ct.SoLuong, " +
            "    ct.DonGia " +
            "FROM CTHOADON ct " +
            "JOIN SANPHAM sp ON ct.MaSP = sp.MaSP " +
            "JOIN GAME g ON sp.MaGame = g.MaGame " +
            "WHERE ct.MaHD = ?";

        try(PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setInt(1, maHD);

            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                list.add(new ChiTietHoaDon(
                        rs.getString("TenGame"),
                        rs.getString("LoaiSanPham"),
                        rs.getInt("SoLuong"),
                        rs.getDouble("DonGia")
                ));
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return list;
    }

    /* =====================================================
        UPDATE STATUS
     ===================================================== */
    public boolean updateTrangThai(HoaDon hd){

        String sql =
                "UPDATE HOADON SET TrangThai=? WHERE MaHD=?";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1, hd.getTrangThai());
            ps.setInt(2, hd.getMaHD());

            return ps.executeUpdate()>0;

        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    /* =====================================================
        UPDATE
     ===================================================== */
    public boolean update(HoaDon hd){

        String sql =
                "UPDATE HOADON SET TongTien=? WHERE MaHD=?";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setDouble(1, hd.getTongTien());
            ps.setInt(2, hd.getMaHD());

            return ps.executeUpdate()>0;

        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    /* =====================================================
        DELETE
     ===================================================== */
    public boolean delete(int maHD){

        try(Connection conn = DBConnection.getConnection()){

            PreparedStatement ct =
                    conn.prepareStatement(
                            "DELETE FROM CHITIETHOADON WHERE MaHD=?");
            ct.setInt(1,maHD);
            ct.executeUpdate();

            PreparedStatement hd =
                    conn.prepareStatement(
                            "DELETE FROM HOADON WHERE MaHD=?");
            hd.setInt(1,maHD);

            return hd.executeUpdate()>0;

        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    /* =====================================================
        MAP OBJECT ⭐ FIX NULL TIMESTAMP
     ===================================================== */
    private HoaDon mapHoaDon(ResultSet rs)
            throws SQLException {

        HoaDon hd = new HoaDon();

        hd.setMaHD(rs.getInt("MaHD"));
        hd.setMaKH(rs.getInt("MaKH"));
        hd.setMaNV(rs.getInt("MaNV"));

        Timestamp ts = rs.getTimestamp("NgayLap");
        if(ts!=null)
            hd.setNgayLap(ts.toLocalDateTime());

        hd.setTongTien(rs.getDouble("TongTien"));
        hd.setTrangThai(rs.getString("TrangThai"));

        return hd;
    }
}