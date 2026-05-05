package otkhongluong.gamestoremanagement.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDon {

    private int maHD;
    private int maKH;
    private int maNV;
    private LocalDateTime ngayLap;
    private double tongTien;
    private String trangThai;

    // ⭐ thêm để HIỂN THỊ TABLE
    private String tenKhachHang;
    private String soDienThoai;

    private List<ChiTietHoaDon> danhSachChiTiet =
            new ArrayList<>();

    // ================= GET SET =================

    public int getMaHD() { return maHD; }
    public void setMaHD(int maHD) { this.maHD = maHD; }
    
    public String getMaHDFormatted() {
        return "HD" + maHD;
    }
    public String getMaNVFormatted() {
        return "NV" + maNV;
    }

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

    // =====================================================
    // ================= CHI TIẾT HÓA ĐƠN ==================
    // =====================================================

    public static class ChiTietHoaDon {

        private int maSP;
        private int soLuong;
        private double donGia;

        // ⭐ ADD FIELD HIỂN THỊ (KHÔNG ẢNH HƯỞNG DB)
        private String tenGame;
        private String loaiSanPham; // CD / ROM
        
        public ChiTietHoaDon(String tenGame, String loaiSanPham, int soLuong, double donGia) {
        this.tenGame = tenGame;
        this.loaiSanPham = loaiSanPham;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }
        

        public int getMaSP() { return maSP; }
        public void setMaSP(int maSP) {
            this.maSP = maSP;
        }

        public int getSoLuong() { return soLuong; }
        public void setSoLuong(int soLuong) {
            this.soLuong = soLuong;
        }

        public double getDonGia() { return donGia; }
        public void setDonGia(double donGia) {
            this.donGia = donGia;
        }

        // ⭐ VIEW ONLY
        public String getTenGame() {
            return tenGame;
        }

        public void setTenGame(String tenGame) {
            this.tenGame = tenGame;
        }

        public String getLoaiSanPham() {
            return loaiSanPham;
        }

        public void setLoaiSanPham(String loaiSanPham) {
            this.loaiSanPham = loaiSanPham;
        }
    }
}