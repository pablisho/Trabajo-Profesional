package ar.uba.fi.prm.arbuy;

import java.util.List;

import ar.uba.fi.prm.arbuy.pojo.Login;
import ar.uba.fi.prm.arbuy.pojo.Publication;
import ar.uba.fi.prm.arbuy.pojo.Response;
import ar.uba.fi.prm.arbuy.pojo.Transaction;
import ar.uba.fi.prm.arbuy.pojo.User;
import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by pablo on 26/11/16.
 */
public interface RestAPI {
    @POST("/api/signup")
    Call<Response> signup(@Body User user);

    @POST("/api/authenticate")
    Call<Response> authenticate(@Body Login login);

    @GET("/api/publications")
    Call<List<Publication>> getPublications(@Header("Authorization") String token,
                                            @Query("pag") Integer pag);

    @GET("/api/purchases")
    Call<List<Transaction>> getPurchases(@Header("Authorization") String token);

    @POST("/api/publish")
    Call<Response> publish(@Header("Authorization") String token, @Body Publication publication);
}
