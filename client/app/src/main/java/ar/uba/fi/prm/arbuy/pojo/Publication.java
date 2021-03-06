package ar.uba.fi.prm.arbuy.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * Created by pablo on 26/11/16.
 */
public class Publication {
    @SerializedName("_id")
    private String id;
    @SerializedName("title")
    private String title;
    @SerializedName("summary")
    private String summary;
    @SerializedName("price")
    private Integer price;
    @SerializedName("ar_obj")
    private List<String[]> ar_obj;
    @SerializedName("image")
    private String image;
    @SerializedName("date")
    private Date date;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("cant")
    private Integer cant;
    @SerializedName("sells")
    private Integer sells;

    public Publication(String id, String title, String summary, Integer price, List<String[]> ar_obj,
                       String image, Date date, String userId, Integer cant, Integer sells) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.price = price;
        this.ar_obj = ar_obj;
        this.image = image;
        this.date = date;
        this.userId = userId;
        this.cant = cant;
        this.sells = sells;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public List<String[]> getAr_obj() {
        return ar_obj;
    }

    public void setAr_obj(List<String[]> ar_obj) {
        this.ar_obj = ar_obj;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getCant() {
        return cant;
    }

    public void setCant(Integer cant) {
        this.cant = cant;
    }

    public Integer getSells() {
        return sells;
    }

    public void setSells(Integer sells) {
        this.sells = sells;
    }
}
