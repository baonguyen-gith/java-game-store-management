package otkhongluong.gamestoremanagement.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RentalOrder {

    private int maPT;
    private int maKH;
    private int maNV;
    private LocalDateTime ngayThue;
    private LocalDateTime ngayTraDuKien;
    private LocalDateTime ngayTraThucTe;
    private double tienCoc;
    private double tienPhat;
    private String trangThai;
    private String tenKhachHang;
    private String tenNhanVien;
    private String soDienThoai;

    private List<CTPhieuThue> danhSachChiTiet = new ArrayList<>();

    /* ================= GET SET ================= */

    public int getMaPT() { return maPT; }
    public void setMaPT(int maPT) { this.maPT = maPT; }
    
    public int getMaKH() { return maKH; }
    public void setMaKH(int maKH) { this.maKH = maKH; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public LocalDateTime getNgayThue() { return ngayThue; }
    public void setNgayThue(LocalDateTime ngayThue) { this.ngayThue = ngayThue; }

    public LocalDateTime getNgayTraDuKien() { return ngayTraDuKien; }
    public void setNgayTraDuKien(LocalDateTime ngayTraDuKien) { this.ngayTraDuKien = ngayTraDuKien; }

    public LocalDateTime getNgayTraThucTe() { return ngayTraThucTe; }
    public void setNgayTraThucTe(LocalDateTime ngayTraThucTe) { this.ngayTraThucTe = ngayTraThucTe; }

    public double getTienCoc() { return tienCoc; }
    public void setTienCoc(double tienCoc) { this.tienCoc = tienCoc; }

    public double getTienPhat() { return tienPhat; }
    public void setTienPhat(double tienPhat) { this.tienPhat = tienPhat; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    
    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public List<CTPhieuThue> getDanhSachChiTiet() {
        return danhSachChiTiet;
    }

    public void setDanhSachChiTiet(List<CTPhieuThue> danhSachChiTiet) {
        this.danhSachChiTiet = danhSachChiTiet;
    }
}