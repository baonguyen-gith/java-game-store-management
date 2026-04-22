package otkhongluong.gamestoremanagement.model;

public class KhachHang {
    private int MaKH;
    private String HoTen;
    private String SDT;
    private String Email;
    private String DiaChi;

    public KhachHang() {}

    public KhachHang(int MaKH, String HoTen, String SDT, String Email, String DiaChi) {
        this.MaKH = MaKH;
        this.HoTen = HoTen;
        this.SDT = SDT;
        this.Email = Email;
        this.DiaChi = DiaChi;
    }

    public int getMaKH() { return MaKH; }
    public void setMaKH(int MaKH) { this.MaKH = MaKH; }

    public String getHoTen() { return HoTen; }
    public void setHoTen(String HoTen) { this.HoTen = HoTen; }

    public String getSDT() { return SDT; }
    public void setSDT(String SDT) { this.SDT = SDT; }

    public String getEmail() { return Email; }
    public void setEmail(String Email) { this.Email = Email; }

    public String getDiaChi() { return DiaChi; }
    public void setDiaChi(String DiaChi) { this.DiaChi = DiaChi; }

    @Override
    public String toString() {
        return "KhachHang{" + "MaKH=" + MaKH + ", HoTen='" + HoTen + '\'' + ", SDT='" + SDT + '\'' + '}';
    }
}
