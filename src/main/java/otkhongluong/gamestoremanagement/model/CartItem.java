package otkhongluong.gamestoremanagement.model;

/**
 * DTO đại diện 1 mục trong giỏ hàng của InvoiceAddDialog.
 * Nằm ở tầng Model — View, Controller, Service đều được phép import.
 * Không chứa business logic.
 */
public class CartItem {
    public final int    maSP;
    public final int    maCD;
    public final int    maGame;
    public       int    soLuong;
    public final String tenGame;
    public final String loaiSP;
    public final String cartKey;
    public final double donGia;

    public CartItem(int maSP, int maCD, int maGame, int soLuong,
                    String tenGame, String loaiSP, String cartKey, double donGia) {
        this.maSP    = maSP;
        this.maCD    = maCD;
        this.maGame  = maGame;
        this.soLuong = soLuong;
        this.tenGame = tenGame;
        this.loaiSP  = loaiSP;
        this.cartKey = cartKey;
        this.donGia  = donGia;
    }
}