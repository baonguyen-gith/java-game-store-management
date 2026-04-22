package otkhongluong.gamestoremanagement.model;

public class User {
    private int MaUser;
    private String Username;
    private String Password;
    private int MaRole;

    public User() {}

    public User(int MaUser, String Username, String Password, int MaRole) {
        this.MaUser = MaUser;
        this.Username = Username;
        this.Password = Password;
        this.MaRole = MaRole;
    }

    public int getMaUser() { return MaUser; }
    public void setMaUser(int MaUser) { this.MaUser = MaUser; }

    public String getUsername() { return Username; }
    public void setUsername(String Username) { this.Username = Username; }

    public String getPassword() { return Password; }
    public void setPassword(String Password) { this.Password = Password; }

    public int getMaRole() { return MaRole; }
    public void setMaRole(int MaRole) { this.MaRole = MaRole; }
}
