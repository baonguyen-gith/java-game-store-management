package otkhongluong.gamestoremanagement.model;

import java.time.LocalDateTime;

public class DiemLichSu {

    private int maLS;
    private int maKH;
    private String loai; // CONG / TRU
    private int soDiem;
    private LocalDateTime ngay;
    private String ghiChu;

    public DiemLichSu() {}

    public DiemLichSu(int maLS, int maKH, String loai,
                      int soDiem, LocalDateTime ngay, String ghiChu) {
        this.maLS = maLS;
        this.maKH = maKH;
        this.loai = loai;
        this.soDiem = soDiem;
        this.ngay = ngay;
        this.ghiChu = ghiChu;
    }

    // ===== GETTER / SETTER =====

    public int getMaLS() {
        return maLS;
    }

    public void setMaLS(int maLS) {
        this.maLS = maLS;
    }

    public int getMaKH() {
        return maKH;
    }

    public void setMaKH(int maKH) {
        this.maKH = maKH;
    }

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    public int getSoDiem() {
        return soDiem;
    }

    public void setSoDiem(int soDiem) {
        this.soDiem = soDiem;
    }

    public LocalDateTime getNgay() {
        return ngay;
    }

    public void setNgay(LocalDateTime ngay) {
        this.ngay = ngay;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    @Override
    public String toString() {
        return "DiemLichSu{" +
                "maKH=" + maKH +
                ", loai='" + loai + '\'' +
                ", soDiem=" + soDiem +
                '}';
    }
}