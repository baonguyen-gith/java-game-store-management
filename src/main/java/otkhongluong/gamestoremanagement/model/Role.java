package otkhongluong.gamestoremanagement.model;

public class Role {
    private int maRole;
    private String tenRole;

    public Role() {}

    public Role(int maRole, String tenRole) {
        this.maRole = maRole;
        this.tenRole = tenRole;
    }

    // Getters and Setters
    public int getMaRole() { return maRole; }
    public void setMaRole(int maRole) { this.maRole = maRole; }

    public String getTenRole() { return tenRole; }
    public void setTenRole(String tenRole) { this.tenRole = tenRole; }
}
