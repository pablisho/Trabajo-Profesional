package ar.uba.fi.prm.arbuy;


import java.util.List;

import ar.uba.fi.prm.arbuy.pojo.Login;
import ar.uba.fi.prm.arbuy.pojo.Publication;
import ar.uba.fi.prm.arbuy.pojo.Response;
import ar.uba.fi.prm.arbuy.pojo.Transaction;
import ar.uba.fi.prm.arbuy.pojo.User;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    @GET("/api/publication/{id}")
    Call<Publication> getPublication(@Header("Authorization") String token, @Path("id") String pubId);

    @GET("/api/purchases")
    Call<List<Transaction>> getPurchases(@Header("Authorization") String token);

    @POST("/api/publish")
    Call<Response> publish(@Header("Authorization") String token, @Body Publication publication);

    @Multipart
    @POST("/api/upload")
    Call<Response> upload(@Header("Authorization") String token, @Part MultipartBody.Part userFile);

}
