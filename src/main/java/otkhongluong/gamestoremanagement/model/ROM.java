package otkhongluong.gamestoremanagement.model;

public class ROM {

    private int maSP;
    private String dungLuong;
    private String linkLuuTru;
    private int soLuotBan;

    public ROM() {}

    public ROM(int maSP, String dungLuong, String linkLuuTru, int soLuotBan) {
        this.maSP = maSP;
        this.dungLuong = dungLuong;
        this.linkLuuTru = linkLuuTru;
        this.soLuotBan = soLuotBan;
    }

    // ===== GETTER / SETTER =====

    public int getMaSP() {
        return maSP;
    }

    public void setMaSP(int maSP) {
        this.maSP = maSP;
    }

    public String getDungLuong() {
        return dungLuong;
    }

    public void setDungLuong(String dungLuong) {
        this.dungLuong = dungLuong;
    }

    public String getLinkLuuTru() {
        return linkLuuTru;
    }

    public void setLinkLuuTru(String linkLuuTru) {
        this.linkLuuTru = linkLuuTru;
    }

    public int getSoLuotBan() {
        return soLuotBan;
    }

    public void setSoLuotBan(int soLuotBan) {
        this.soLuotBan = soLuotBan;
    }

    @Override
    public String toString() {
        return "ROM{" +
                "maSP=" + maSP +
                ", dungLuong='" + dungLuong + '\'' +
                '}';
    }
}