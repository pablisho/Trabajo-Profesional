package ar.uba.fi.prm.arbuy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.UUID;

import ar.uba.fi.prm.arbuy.pojo.Publication;
import ar.uba.fi.prm.arbuy.pojo.Response;
import ar.uba.fi.prm.arbuy.tango.OcclusionActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by pablo on 30/11/16.
 */
public class PublicationActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
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
    private TextView mTitle;

    private Button mArButton;
    private Button mBuyButton;

    private String mPubId;

    private String objFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publication);

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mToolbar.setTitle("Publication");

        mCollapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        mCollapsingToolbar.setTitle("Publication");

        mPhoto = (ImageView) findViewById(R.id.bgheader);
        mPrice = (TextView) findViewById(R.id.price);
        mCant = (TextView) findViewById(R.id.cant);
        mSales = (TextView) findViewById(R.id.sales);
        mDescription = (TextView) findViewById(R.id.description);
        mTitle = (TextView) findViewById(R.id.title);

        mArButton = (Button) findViewById(R.id.btn_viewar);
        mBuyButton = (Button) findViewById(R.id.btn_buy);

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();

        retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        restAPI = retrofit.create(RestAPI.class);

        SharedPreferences preferences = getSharedPreferences("token", MODE_PRIVATE);
        mToken = preferences.getString("token", null);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            mPubId = b.getString("pubId");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "Publication id" + mPubId);
        final Call<Publication> pubCall = restAPI.getPublication(mToken, mPubId);
        pubCall.enqueue(new Callback<Publication>() {
            @Override
            public void onResponse(Call<Publication> call, retrofit2.Response<Publication> response) {
                if (response.code() == 200) {
                    Publication publication = response.body();

                    mCollapsingToolbar.setTitle(publication.getTitle());
                    String url = MainActivity.BASE_URL + "api/getResource?file=" + publication.getImage();
                    Picasso.with(PublicationActivity.this)
                            .load(url)
                            .placeholder(R.drawable.progress_animation)
                            .into(mPhoto);
                    Typeface roboto = Typeface.createFromAsset(getAssets(),
                            "font/Roboto-Regular.ttf");
                    mPrice.setTypeface(roboto);
                    mPrice.setText(String.valueOf(publication.getPrice()) + "$");
                    mCant.setText("Qty: " + publication.getCant());
                    mSales.setText("Sold: " + publication.getSells());
                    mDescription.setTypeface(roboto);
                    mDescription.setText(publication.getSummary());

                    final UUID uuid = UUID.randomUUID();
                    for (final String[] pair : publication.getAr_obj()) {
                        Log.d(TAG, "AR file: path:" + pair[0] + "name" + pair[1]);
                        final Call<ResponseBody> file = restAPI.downloadFile(pair[0]);
                        file.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                                if (response.code() == 200) {
                                    if(pair[1].contains("obj")){
                                        objFile = uuid.toString() + File.separator + pair[1];
                                    }
                                    saveFile(response.body(), pair[1], uuid.toString());
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.d(TAG, "Request failure");
                                t.printStackTrace();
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<Publication> call, Throwable t) {
                Log.d(TAG, "Request failure");
                t.printStackTrace();
            }
        });
    }

    public void buy(View view) {
        final Call<Response> pubCall = restAPI.buy(mToken, mPubId);
        pubCall.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                if (response.code() == 200) {
                    Response resp = response.body();
                    if (resp.getStatus()) {
                        Toast.makeText(PublicationActivity.this, "Transaction completed succesfully", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(PublicationActivity.this, "Transaction could not be completed", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Log.d(TAG, "Request failure");
                t.printStackTrace();
            }
        });
    }

    public void viewAr(View view) {
        Intent intent = new Intent(getApplicationContext(), OcclusionActivity.class);
        Bundle b = new Bundle();
        b.putString("objfile", objFile); //Your id
        intent.putExtras(b);
        startActivity(intent);
    }

    public boolean saveFile(ResponseBody body, String fileName, String folder) {
        try {
            // todo change the file location/name according to your needs
            File folderfile = new File(getExternalFilesDir(null) + File.separator + folder);
            folderfile.mkdirs();
            File futureStudioIconFile = new File(folderfile + File.separator + fileName);

            Log.d(TAG, "Saving file to " + futureStudioIconFile.toString());

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                Log.d(TAG, "Exception saving file");
                e.printStackTrace();
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            Log.d(TAG, "Exception saving file");
            e.printStackTrace();
            return false;

        }
    }
}
