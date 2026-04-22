package otkhongluong.gamestoremanagement.model;

public class Role {
    private int MaRole;
    private String TenRole;

    public Role() {}

    public Role(int MaRole, String TenRole) {
        this.MaRole = MaRole;
        this.TenRole = TenRole;
    }

    public int getMaRole() { return MaRole; }
    public void setMaRole(int MaRole) { this.MaRole = MaRole; }

    public String getTenRole() { return TenRole; }
    public void setTenRole(String TenRole) { this.TenRole = TenRole; }
}
