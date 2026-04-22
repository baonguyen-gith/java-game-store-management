package otkhongluong.gamestoremanagement.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PhieuThue {
    private int MaPT;
    private int MaKH;
    private Date NgayThue;
    private Date NgayTraDuKien;
    private Date NgayTraThucTe;
    private double TienCoc;
    private double TienPhat;

    private List<CTPhieuThue> DanhSachChiTiet;

    public PhieuThue() {
        this.DanhSachChiTiet = new ArrayList<>();
    }

    public PhieuThue(int MaPT, int MaKH, Date NgayThue, Date NgayTraDuKien, Date NgayTraThucTe, double TienCoc, double TienPhat) {
        this.MaPT = MaPT;
        this.MaKH = MaKH;
        this.NgayThue = NgayThue;
        this.NgayTraDuKien = NgayTraDuKien;
        this.NgayTraThucTe = NgayTraThucTe;
        this.TienCoc = TienCoc;
        this.TienPhat = TienPhat;
        this.DanhSachChiTiet = new ArrayList<>();
    }

    public int getMaPT() { return MaPT; }
    public void setMaPT(int MaPT) { this.MaPT = MaPT; }
    public int getMaKH() { return MaKH; }
    public void setMaKH(int MaKH) { this.MaKH = MaKH; }
    public Date getNgayThue() { return NgayThue; }
    public void setNgayThue(Date NgayThue) { this.NgayThue = NgayThue; }
    public Date getNgayTraDuKien() { return NgayTraDuKien; }
    public void setNgayTraDuKien(Date NgayTraDuKien) { this.NgayTraDuKien = NgayTraDuKien; }
    public Date getNgayTraThucTe() { return NgayTraThucTe; }
    public void setNgayTraThucTe(Date NgayTraThucTe) { this.NgayTraThucTe = NgayTraThucTe; }
    public double getTienCoc() { return TienCoc; }
    public void setTienCoc(double TienCoc) { this.TienCoc = TienCoc; }
    public double getTienPhat() { return TienPhat; }
    public void setTienPhat(double TienPhat) { this.TienPhat = TienPhat; }
    public List<CTPhieuThue> getDanhSachChiTiet() { return DanhSachChiTiet; }
    public void setDanhSachChiTiet(List<CTPhieuThue> DanhSachChiTiet) { this.DanhSachChiTiet = DanhSachChiTiet; }

    public void themSanPhamThue(CTPhieuThue ct) {
        this.DanhSachChiTiet.add(ct);
    }

    @Override
    public String toString() {
        return "PhieuThue{" + "MaPT=" + MaPT + ", MaKH=" + MaKH + ", SoSPThue=" + DanhSachChiTiet.size() + ", TienPhat=" + TienPhat + '}';
    }

    public static class CTPhieuThue {
        private int MaSP;
        public CTPhieuThue() {}
        public CTPhieuThue(int MaSP) {
            this.MaSP = MaSP;
        }

        public int getMaSP() { return MaSP; }
        public void setMaSP(int MaSP) { this.MaSP = MaSP; }

        @Override
        public String toString() {
            return "CTPhieuThue{" + "MaSP=" + MaSP + '}';
        }
    }
}
