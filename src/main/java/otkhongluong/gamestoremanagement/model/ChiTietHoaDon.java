package otkhongluong.gamestoremanagement.model;

// ✅ File riêng — không còn là inner class của Invoice
public class ChiTietHoaDon {

    private int    maSP;
    private int    soLuong;
    private double donGia;
    private String tenGame;
    private String loaiSanPham;  // "CD" hoặc "ROM"

    public ChiTietHoaDon() {}

    public ChiTietHoaDon(String tenGame, String loaiSanPham, int soLuong, double donGia) {
        this.tenGame    = tenGame;
        this.loaiSanPham = loaiSanPham;
        this.soLuong    = soLuong;
        this.donGia     = donGia;
    }

    public int    getMaSP()                          { return maSP; }
    public void   setMaSP(int maSP)                  { this.maSP = maSP; }
    public int    getSoLuong()                       { return soLuong; }
    public void   setSoLuong(int soLuong)            { this.soLuong = soLuong; }
    public double getDonGia()                        { return donGia; }
    public void   setDonGia(double donGia)           { this.donGia = donGia; }
    public String getTenGame()                       { return tenGame; }
    public void   setTenGame(String tenGame)         { this.tenGame = tenGame; }
    public String getLoaiSanPham()                   { return loaiSanPham; }
    public void   setLoaiSanPham(String loaiSanPham) { this.loaiSanPham = loaiSanPham; }
}