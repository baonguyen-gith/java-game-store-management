package otkhongluong.gamestoremanagement.model;

/**
 * DTO đại diện 1 dòng sản phẩm trong working list của InvoiceEditDialog.
 * Nằm ở tầng Model — View, Controller, Service đều được phép import.
 * Không chứa business logic.
 */
public class SpRow {
    public final int     maSP;
    public final int     maCD;
    public final String  tenGame;
    public final String  loai;
    public final double  donGia;
    public       int     soLuong;
    public final boolean isNew;

    public SpRow(int maSP, int maCD, String tenGame,
                 String loai, double donGia, int soLuong, boolean isNew) {
        this.maSP    = maSP;
        this.maCD    = maCD;
        this.tenGame = tenGame;
        this.loai    = loai;
        this.donGia  = donGia;
        this.soLuong = soLuong;
        this.isNew   = isNew;
    }

    public double thanhTien() { return donGia * soLuong; }

    public String key() {
        return "CD".equals(loai) ? "CD_" + maCD : "ROM_" + maSP;
    }
}