package otkhongluong.gamestoremanagement.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDon {
    private int MaHD;
    private int MaKH;
    private LocalDateTime NgayLap; 
    private double TongTien;
    private List<ChiTietHoaDon> DanhSachChiTiet;

    public HoaDon() {
        this.DanhSachChiTiet = new ArrayList<>();
    }

    public HoaDon(int MaHD, int MaKH, LocalDateTime NgayLap, double TongTien) {
        this.MaHD = MaHD;
        this.MaKH = MaKH;
        this.NgayLap = NgayLap;
        this.TongTien = TongTien;
        this.DanhSachChiTiet = new ArrayList<>();
    }

    public int getMaHD() { return MaHD; }
    public void setMaHD(int MaHD) { this.MaHD = MaHD; }

    public int getMaKH() { return MaKH; }
    public void setMaKH(int MaKH) { this.MaKH = MaKH; }

    public LocalDateTime getNgayLap() { return NgayLap; }
    public void setNgayLap(LocalDateTime NgayLap) { this.NgayLap = NgayLap; }

    public double getTongTien() { return TongTien; }
    public void setTongTien(double TongTien) { this.TongTien = TongTien; }

    public List<ChiTietHoaDon> getDanhSachChiTiet() { return DanhSachChiTiet; }
    public void setDanhSachChiTiet(List<ChiTietHoaDon> DanhSachChiTiet) { this.DanhSachChiTiet = DanhSachChiTiet; }

    @Override
    public String toString() {
        return "HoaDon{" + "MaHD=" + MaHD + ", NgayLap=" + NgayLap + ", TongTien=" + TongTien + '}';
    }

    public static class ChiTietHoaDon {
        private int MaSP;
        private int SoLuong;
        private double DonGia;

        public ChiTietHoaDon() {}
        public ChiTietHoaDon(int MaSP, int SoLuong, double DonGia) {
            this.MaSP = MaSP;
            this.SoLuong = SoLuong;
            this.DonGia = DonGia;
        }

        public int getMaSP() { return MaSP; }
        public void setMaSP(int MaSP) { this.MaSP = MaSP; }
        public int getSoLuong() { return SoLuong; }
        public void setSoLuong(int SoLuong) { this.SoLuong = SoLuong; }
        public double getDonGia() { return DonGia; }
        public void setDonGia(double DonGia) { this.DonGia = DonGia; }
    }
}
