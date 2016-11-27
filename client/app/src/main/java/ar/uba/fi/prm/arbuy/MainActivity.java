package ar.uba.fi.prm.arbuy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import ar.uba.fi.prm.arbuy.pojo.Publication;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String BASE_URL = "http://192.168.0.101:3000/";
    private Retrofit retrofit;
    private RestAPI restAPI;
    private String mToken;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();

        /*final Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        collapsingToolbar.setTitle("My Toolbar Tittle");

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        restAPI = retrofit.create(RestAPI.class);

        SharedPreferences preferences = getSharedPreferences("token", MODE_PRIVATE);
        mToken = preferences.getString("token", null);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Call<List<Publication>> pubCall = restAPI.getPublications(mToken, 0);
        pubCall.enqueue(new Callback<List<Publication>>() {
            @Override
            public void onResponse(Response<List<Publication>> response, Retrofit retrofit) {
                if (response.code() == 200) {
                    List<Publication> publications = response.body();
                    Log.d(TAG, "Received " + publications.size() + " publications");
                    for (Publication publication : publications) {
                        Log.d(TAG, "Publication: " + publication.getTitle());
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(TAG, "Request failure");
                t.printStackTrace();
            }
        });
    }
}
