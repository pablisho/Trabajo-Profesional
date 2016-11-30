package ar.uba.fi.prm.arbuy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import ar.uba.fi.prm.arbuy.pojo.Publication;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by pablo on 30/11/16.
 */
public class PublicationActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String BASE_URL = "http://192.168.0.101:3000/";
    private Retrofit retrofit;
    private RestAPI restAPI;
    private String mToken;

    private Toolbar mToolbar;
    private CollapsingToolbarLayout mCollapsingToolbar;
    private ImageView mPhoto;
    private TextView mPrice;
    private TextView mCant;
    private TextView mSales;
    private TextView mDescription;

    private Button mArButton;
    private Button mBuyButton;

    private String mPubId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publication);

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mCollapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        mCollapsingToolbar.setTitle("Publication");

        mPhoto = (ImageView) findViewById(R.id.bgheader);
        mPrice = (TextView) findViewById(R.id.price);
        mCant = (TextView) findViewById(R.id.cant);
        mSales = (TextView) findViewById(R.id.sales);
        mDescription = (TextView) findViewById(R.id.description);

        mArButton = (Button) findViewById(R.id.btn_viewar);
        mBuyButton = (Button) findViewById(R.id.btn_buy);

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        restAPI = retrofit.create(RestAPI.class);

        SharedPreferences preferences = getSharedPreferences("token", MODE_PRIVATE);
        mToken = preferences.getString("token", null);

        Bundle b = getIntent().getExtras();
        if(b != null) {
            mPubId = b.getString("pubId");
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        Log.d(TAG, "Publication id" + mPubId);
        final Call<Publication> pubCall = restAPI.getPublication(mToken, mPubId);
        pubCall.enqueue(new Callback<Publication>() {
            @Override
            public void onResponse(Call<Publication> call, Response<Publication> response) {
                if (response.code() == 200) {
                    Publication publication = response.body();

                    mCollapsingToolbar.setTitle(publication.getTitle());
                    String url = BASE_URL + "api/getResource?file=" + publication.getImage();
                    Picasso.with(PublicationActivity.this)
                            .load(url)
                            .into(mPhoto);
                    mPrice.setText(String.valueOf(publication.getPrice()) + "$");
                    mCant.setText("Qty: " + publication.getCant());
                    mSales.setText("Sold: " + publication.getSells());
                    mDescription.setText(publication.getSummary());
                }
            }

            @Override
            public void onFailure(Call<Publication> call, Throwable t) {
                Log.d(TAG, "Request failure");
                t.printStackTrace();
            }
        });
    }

}