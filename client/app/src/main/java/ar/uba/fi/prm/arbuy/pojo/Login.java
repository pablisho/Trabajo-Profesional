package ar.uba.fi.prm.arbuy.pojo;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pablo on 26/11/16.
 */
public class Login {
    @SerializedName("username")
    private String username;
    @SerializedName("password")
    private String password;

    public Login(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
