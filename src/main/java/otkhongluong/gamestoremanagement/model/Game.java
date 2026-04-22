package otkhongluong.gamestoremanagement.model;

public class Game {
    private int MaGame;
    private String TenGame;
    private String TheLoai;
    private String NenTang;
    private double GiaBan;
    private double GiaThueNgay;

    public Game() {}

    public Game(int MaGame, String TenGame, String TheLoai, String NenTang, double GiaBan, double GiaThueNgay) {
        this.MaGame = MaGame;
        this.TenGame = TenGame;
        this.TheLoai = TheLoai;
        this.NenTang = NenTang;
        this.GiaBan = GiaBan;
        this.GiaThueNgay = GiaThueNgay;
    }

    public int getMaGame() { return MaGame; }
    public void setMaGame(int MaGame) { this.MaGame = MaGame; }

    public String getTenGame() { return TenGame; }
    public void setTenGame(String TenGame) { this.TenGame = TenGame; }

    public String getTheLoai() { return TheLoai; }
    public void setTheLoai(String TheLoai) { this.TheLoai = TheLoai; }

    public String getNenTang() { return NenTang; }
    public void setNenTang(String NenTang) { this.NenTang = NenTang; }

    public double getGiaBan() { return GiaBan; }
    public void setGiaBan(double GiaBan) { this.GiaBan = GiaBan; }

    public double getGiaThueNgay() { return GiaThueNgay; }
    public void setGiaThueNgay(double GiaThueNgay) { this.GiaThueNgay = GiaThueNgay; }

    @Override
    public String toString() {
        return "Game{" + "MaGame=" + MaGame + ", TenGame='" + TenGame + '\'' + '}';
    }
}
