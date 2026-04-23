package otkhongluong.gamestoremanagement.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PhieuThue {
    private int MaPT;
    private int MaKH;
    private LocalDateTime NgayThue;
    private LocalDateTime NgayTraDuKien;
    private LocalDateTime NgayTraThucTe;
    private double TienCoc;
    private double TienPhat;
    private List<CTPhieuThue> DanhSachChiTiet;

    public PhieuThue() {
        this.DanhSachChiTiet = new ArrayList<>();
    }

    public PhieuThue(int MaPT, int MaKH, LocalDateTime NgayThue, LocalDateTime NgayTraDuKien, LocalDateTime NgayTraThucTe, double TienCoc, double TienPhat) {
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
    
    public LocalDateTime getNgayThue() { return NgayThue; }
    public void setNgayThue(LocalDateTime NgayThue) { this.NgayThue = NgayThue; }
    
    public LocalDateTime getNgayTraDuKien() { return NgayTraDuKien; }
    public void setNgayTraDuKien(LocalDateTime NgayTraDuKien) { this.NgayTraDuKien = NgayTraDuKien; }
    
    public LocalDateTime getNgayTraThucTe() { return NgayTraThucTe; }
    public void setNgayTraThucTe(LocalDateTime NgayTraThucTe) { this.NgayTraThucTe = NgayTraThucTe; }

    public double getTienCoc() { return TienCoc; }
    public void setTienCoc(double TienCoc) { this.TienCoc = TienCoc; }
    
    public double getTienPhat() { return TienPhat; }
    public void setTienPhat(double TienPhat) { this.TienPhat = TienPhat; }

    public List<CTPhieuThue> getDanhSachChiTiet() { return DanhSachChiTiet; }
    public void setDanhSachChiTiet(List<CTPhieuThue> DanhSachChiTiet) { this.DanhSachChiTiet = DanhSachChiTiet; }

    public static class CTPhieuThue {
        private int MaSP;
        public CTPhieuThue() {}
        public CTPhieuThue(int MaSP) { this.MaSP = MaSP; }
        public int getMaSP() { return MaSP; }
        public void setMaSP(int MaSP) { this.MaSP = MaSP; }
    }
}
