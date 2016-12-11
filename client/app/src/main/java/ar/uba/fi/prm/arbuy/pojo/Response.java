package ar.uba.fi.prm.arbuy.pojo;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pablo on 26/11/16.
 */
public class Response {
    @SerializedName("success")
    private Boolean status;
    @SerializedName("msg")
    private String message;
    @SerializedName("token")
    private String token;
    @SerializedName("name")
    private String name;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
