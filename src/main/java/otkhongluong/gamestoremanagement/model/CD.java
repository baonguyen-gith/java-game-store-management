package otkhongluong.gamestoremanagement.model;

public class CD {

    private int maCD;
    private int maSP;
    private String tinhTrang;
    private String trangThai; 
    // SAN SANG / DANG THUE / HONG

    public CD() {}

    public CD(int maCD, int maSP, String tinhTrang, String trangThai) {
        this.maCD = maCD;
        this.maSP = maSP;
        this.tinhTrang = tinhTrang;
        this.trangThai = trangThai;
    }

    // ===== GETTER / SETTER =====

    public int getMaCD() {
        return maCD;
    }

    public void setMaCD(int maCD) {
        this.maCD = maCD;
    }

    public int getMaSP() {
        return maSP;
    }

    public void setMaSP(int maSP) {
        this.maSP = maSP;
    }

    public String getTinhTrang() {
        return tinhTrang;
    }

    public void setTinhTrang(String tinhTrang) {
        this.tinhTrang = tinhTrang;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return "CD{" +
                "maCD=" + maCD +
                ", maSP=" + maSP +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}