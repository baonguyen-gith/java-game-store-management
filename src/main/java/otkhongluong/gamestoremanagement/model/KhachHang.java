package otkhongluong.gamestoremanagement.model;

public class KhachHang {

    private int maKH;
    private String hoTen;
    private String sdt;
    private String cccd;
    private String email;
    private String diaChi;
    private int diemTichLuy;

    public KhachHang() {}

    public KhachHang(int maKH, String hoTen, String sdt,
                     String cccd, String email,
                     String diaChi, int diemTichLuy) {

        this.maKH = maKH;
        this.hoTen = hoTen;
        this.sdt = sdt;
        this.cccd = cccd;
        this.email = email;
        this.diaChi = diaChi;
        this.diemTichLuy = diemTichLuy;
    }

    // ===== GETTER / SETTER =====

    public int getMaKH() {
        return maKH;
    }

    public void setMaKH(int maKH) {
        this.maKH = maKH;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public int getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(int diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
    }

    @Override
    public String toString() {
        return "KhachHang{" +
                "maKH=" + maKH +
                ", hoTen='" + hoTen + '\'' +
                ", sdt='" + sdt + '\'' +
                '}';
    }
}