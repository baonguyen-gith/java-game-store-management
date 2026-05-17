package otkhongluong.gamestoremanagement.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Invoice {

     private int maHD;
    private int maKH;
    private int maNV;
    private LocalDateTime ngayLap;
    private double tongTien;
    private String trangThai;
    private String tenKhachHang;
    private String soDienThoai;

    private List<ChiTietHoaDon> danhSachChiTiet = new ArrayList<>();

    // ================= GET SET =================

    public int getMaHD() { return maHD; }
    public void setMaHD(int maHD) { this.maHD = maHD; }

    public int getMaKH() { return maKH; }
    public void setMaKH(int maKH) { this.maKH = maKH; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public LocalDateTime getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDateTime ngayLap) {
        this.ngayLap = ngayLap;
    }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    // ⭐ VIEW DATA
    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public List<ChiTietHoaDon> getDanhSachChiTiet() {
        return danhSachChiTiet;
    }

    public void setDanhSachChiTiet(List<ChiTietHoaDon> list) {
        this.danhSachChiTiet = list;
    }
}