package otkhongluong.gamestoremanagement.model;

import java.time.LocalDateTime;

/**
 * Ánh xạ bảng DIEM_LICHSU trong database.
 */
public class DiemLichSu {

    private int    maLS;
    private int    maKH;
    private Integer maPT;          // nullable
    private String loai;           // "cong" | "tru"
    private int    soDiem;
    private LocalDateTime ngay;
    private String ghiChu;

    // ===== Constructor =====
    public DiemLichSu() {}

    public DiemLichSu(int maKH, String loai, int soDiem, String ghiChu) {
        this.maKH   = maKH;
        this.loai   = loai;
        this.soDiem = soDiem;
        this.ghiChu = ghiChu;
    }

    // ===== Getters / Setters =====
    public int getMaLS()               { return maLS; }
    public void setMaLS(int maLS)      { this.maLS = maLS; }

    public int getMaKH()               { return maKH; }
    public void setMaKH(int maKH)      { this.maKH = maKH; }

    public Integer getMaPT()           { return maPT; }
    public void setMaPT(Integer maPT)  { this.maPT = maPT; }

    public String getLoai()            { return loai; }
    public void setLoai(String loai)   { this.loai = loai; }

    public int getSoDiem()             { return soDiem; }
    public void setSoDiem(int soDiem)  { this.soDiem = soDiem; }

    public LocalDateTime getNgay()          { return ngay; }
    public void setNgay(LocalDateTime ngay) { this.ngay = ngay; }

    public String getGhiChu()               { return ghiChu; }
    public void setGhiChu(String ghiChu)    { this.ghiChu = ghiChu; }

    /** Nhãn hiển thị thân thiện cho cột "Loại" */
    public String getLoaiDisplay() {
        return "cong".equalsIgnoreCase(loai) ? "➕ Cộng" : "➖ Trừ";
    }
}