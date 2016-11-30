package ar.uba.fi.prm.arbuy.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by pablo on 27/11/16.
 */
public class Transaction {
    @SerializedName("_id")
    private String id;
    @SerializedName("pub_id")
    private String publicationId;
    @SerializedName("buyer_id")
    private String buyerId;
    @SerializedName("seller_id")
    private String sellerId;
    @SerializedName("price")
    private String price;
    @SerializedName("title")
    private String title;
    @SerializedName("date")
    private Date date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
