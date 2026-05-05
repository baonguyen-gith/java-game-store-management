package otkhongluong.gamestoremanagement.model;

import java.time.LocalDate;

public class NhanVien {

    private int maNV;
    private String hoTen;
    private String sdt;
    private LocalDate ngaySinh;
    private String cccd;
    private LocalDate ngayVaoLam;

    public NhanVien() {}

    public NhanVien(int maNV, String hoTen, String sdt,
                    LocalDate ngaySinh, String cccd,
                    LocalDate ngayVaoLam) {

        this.maNV = maNV;
        this.hoTen = hoTen;
        this.sdt = sdt;
        this.ngaySinh = ngaySinh;
        this.cccd = cccd;
        this.ngayVaoLam = ngayVaoLam;
    }

    // ===== GETTER / SETTER =====

    public int getMaNV() {
        return maNV;
    }

    public void setMaNV(int maNV) {
        this.maNV = maNV;
    }
    
    public String getMaNVFormatted() {
        return "NV" + maNV;
    }


    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public LocalDate getNgayVaoLam() {
        return ngayVaoLam;
    }

    public void setNgayVaoLam(LocalDate ngayVaoLam) {
        this.ngayVaoLam = ngayVaoLam;
    }

    @Override
    public String toString() {
        return "NhanVien{" +
                "maNV=" + maNV +
                ", hoTen='" + hoTen + '\'' +
                ", sdt='" + sdt + '\'' +
                '}';
    }
}