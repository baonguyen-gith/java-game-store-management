package otkhongluong.gamestoremanagement.model;

public class Product {
    private int maSP;
    private int maGame;
    private double giaBan;
    private double giaThueNgay;
    private int soLuongCD; // số lượng đĩa CD tồn kho

    public Product() {}

    public Product(int maSP, int maGame, double giaBan, double giaThueNgay) {
        this.maSP        = maSP;
        this.maGame      = maGame;
        this.giaBan      = giaBan;
        this.giaThueNgay = giaThueNgay;
    }

    public Product(int maSP, int maGame, double giaBan, double giaThueNgay, int soLuongCD) {
        this.maSP        = maSP;
        this.maGame      = maGame;
        this.giaBan      = giaBan;
        this.giaThueNgay = giaThueNgay;
        this.soLuongCD   = soLuongCD;
    }

    // ===== GETTER / SETTER =====

    public int getMaSP() { return maSP; }
    public void setMaSP(int maSP) { this.maSP = maSP; }

    public int getMaGame() { return maGame; }
    public void setMaGame(int maGame) { this.maGame = maGame; }

    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    public double getGiaThueNgay() { return giaThueNgay; }
    public void setGiaThueNgay(double giaThueNgay) { this.giaThueNgay = giaThueNgay; }

    public int getSoLuongCD() { return soLuongCD; }
    public void setSoLuongCD(int soLuongCD) { this.soLuongCD = soLuongCD; }

    @Override
    public String toString() {
        return "Product{" +
                "maSP="        + maSP        +
                ", maGame="    + maGame       +
                ", giaBan="    + giaBan       +
                ", giaThueNgay=" + giaThueNgay +
                ", soLuongCD=" + soLuongCD    +
                '}';
    }
}