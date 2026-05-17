package otkhongluong.gamestoremanagement.model;

import java.time.LocalDateTime;

public class TransactionDTO {
    private String        id;            // "HD12", "PT5"
    private String        loai;          // "Hóa đơn", "Phiếu thuê"
    private int           maNV;
    private String        tenKhachHang;
    private LocalDateTime ngay;
    private double        tien;

    public TransactionDTO() {}

    public TransactionDTO(String id, String loai, int maNV,
                          String tenKhachHang, LocalDateTime ngay, double tien) {
        this.id            = id;
        this.loai          = loai;
        this.maNV          = maNV;
        this.tenKhachHang  = tenKhachHang;
        this.ngay          = ngay;
        this.tien          = tien;
    }

    public String        getId()             { return id; }
    public void          setId(String id)    { this.id = id; }
    public String        getLoai()           { return loai; }
    public void          setLoai(String v)   { this.loai = v; }
    public int           getMaNV()           { return maNV; }
    public void          setMaNV(int v)      { this.maNV = v; }
    public String        getTenKhachHang()   { return tenKhachHang; }
    public void          setTenKhachHang(String v) { this.tenKhachHang = v; }
    public LocalDateTime getNgay()           { return ngay; }
    public void          setNgay(LocalDateTime v)  { this.ngay = v; }
    public double        getTien()           { return tien; }
    public void          setTien(double v)   { this.tien = v; }
}