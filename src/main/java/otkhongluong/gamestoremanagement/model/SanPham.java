package otkhongluong.gamestoremanagement.model;

public class SanPham {
    private int MaSP;
    private int MaGame;
    private double GiaBan;
    private String TrangThai;

    public SanPham() {}

    public SanPham(int MaSP, int MaGame, double GiaBan, String TrangThai) {
        this.MaSP = MaSP;
        this.MaGame = MaGame;
        this.GiaBan = GiaBan;
        this.TrangThai = TrangThai;
    }

    public int getMaSP() { return MaSP; }
    public void setMaSP(int MaSP) { this.MaSP = MaSP; }

    public int getMaGame() { return MaGame; }
    public void setMaGame(int MaGame) { this.MaGame = MaGame; }

    public double getGiaBan() { return GiaBan; }
    public void setGiaBan(double GiaBan) { this.GiaBan = GiaBan; }

    public String getTrangThai() { return TrangThai; }
    public void setTrangThai(String TrangThai) { this.TrangThai = TrangThai; }

    @Override
    public String toString() {
        return "SanPham{" + "MaSP=" + MaSP + ", MaGame=" + MaGame + ", GiaBan=" + GiaBan + ", TrangThai=" + TrangThai + '}';
    }
}
