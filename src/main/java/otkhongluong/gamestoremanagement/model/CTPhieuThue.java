package otkhongluong.gamestoremanagement.model;

/**
 * FIX: xóa field tinhTrang thừa.
 * Trước đây có cả tinhTrang lẫn trangThai, cả hai đều được set
 * từ cùng cột TrangThai trong RentalOrderDAO.getChiTiet().
 * Giữ lại trangThai (khớp với tên cột DB), thêm alias getter
 * getTinhTrang() để không break caller cũ.
 */
public class CTPhieuThue {

    private int    maCD;
    private int    maNV;
    private String tenGame;
    private String trangThai;   // cột DB: TrangThai (SanSang / DangThue / Hong)
    private double donGiaThue;
    private double giaThueNgay;

    public CTPhieuThue() {}

    public CTPhieuThue(int maCD, String tenGame, double donGiaThue, String trangThai) {
        this.maCD       = maCD;
        this.tenGame    = tenGame;
        this.donGiaThue = donGiaThue;
        this.trangThai  = trangThai;
    }

    // ── Getters / Setters ──────────────────────────────
    public int    getMaCD()                        { return maCD; }
    public void   setMaCD(int maCD)                { this.maCD = maCD; }

    public int    getMaNV()                        { return maNV; }
    public void   setMaNV(int maNV)                { this.maNV = maNV; }

    public String getTenGame()                     { return tenGame; }
    public void   setTenGame(String tenGame)       { this.tenGame = tenGame; }

    public String getTrangThai()                   { return trangThai; }
    public void   setTrangThai(String trangThai)   { this.trangThai = trangThai; }

    /**
     * Alias của getTrangThai() — giữ để không break caller cũ.
     * @deprecated dùng {@link #getTrangThai()} thay thế
     */
    @Deprecated
    public String getTinhTrang()                   { return trangThai; }

    /**
     * Alias của setTrangThai() — giữ để không break caller cũ.
     * @deprecated dùng {@link #setTrangThai(String)} thay thế
     */
    @Deprecated
    public void   setTinhTrang(String v)           { this.trangThai = v; }

    public double getDonGiaThue()                  { return donGiaThue; }
    public void   setDonGiaThue(double donGiaThue) { this.donGiaThue = donGiaThue; }

    public double getGiaThueNgay()                 { return giaThueNgay; }
    public void   setGiaThueNgay(double v)         { this.giaThueNgay = v; }
}