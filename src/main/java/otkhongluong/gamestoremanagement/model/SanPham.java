package otkhongluong.gamestoremanagement.model;

public class SanPham {

    private int maSP;
    private int maGame;
    private double giaBan;
    private double giaThueNgay;

    public SanPham() {}

    public SanPham(int maSP, int maGame, double giaBan, double giaThueNgay) {
        this.maSP = maSP;
        this.maGame = maGame;
        this.giaBan = giaBan;
        this.giaThueNgay = giaThueNgay;
    }

    // ===== GETTER / SETTER =====

    public int getMaSP() {
        return maSP;
    }

    public void setMaSP(int maSP) {
        this.maSP = maSP;
    }

    public int getMaGame() {
        return maGame;
    }

    public void setMaGame(int maGame) {
        this.maGame = maGame;
    }

    public double getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(double giaBan) {
        this.giaBan = giaBan;
    }

    public double getGiaThueNgay() {
        return giaThueNgay;
    }

    public void setGiaThueNgay(double giaThueNgay) {
        this.giaThueNgay = giaThueNgay;
    }

    @Override
    public String toString() {
        return "SanPham{" +
                "maSP=" + maSP +
                ", maGame=" + maGame +
                ", giaBan=" + giaBan +
                ", giaThueNgay=" + giaThueNgay +
                '}';
    }
}