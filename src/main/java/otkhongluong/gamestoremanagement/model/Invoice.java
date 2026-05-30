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

    // ★ Bổ sung — từ cột DiemSuDung trong HOADON
    private int diemSuDung;

    // ★ Bổ sung — từ cột TienGiam trong HOADON
    private double tienGiam;

    // ★ Bổ sung — từ JOIN NHANVIEN (alias TenNV trong DAO)
    private String tenNhanVien;

    // ================= GET SET =================
    public int getMaHD() { return maHD; }
    public void setMaHD(int maHD) { this.maHD = maHD; }

    public int getMaKH() { return maKH; }
    public void setMaKH(int maKH) { this.maKH = maKH; }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public LocalDateTime getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDateTime ngayLap) { this.ngayLap = ngayLap; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public List<ChiTietHoaDon> getDanhSachChiTiet() { return danhSachChiTiet; }
    public void setDanhSachChiTiet(List<ChiTietHoaDon> list) { this.danhSachChiTiet = list; }

    // ★ Getter/setter mới
    public int getDiemSuDung() { return diemSuDung; }
    public void setDiemSuDung(int diemSuDung) { this.diemSuDung = diemSuDung; }

    public double getTienGiam() { return tienGiam; }
    public void setTienGiam(double tienGiam) { this.tienGiam = tienGiam; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }
}