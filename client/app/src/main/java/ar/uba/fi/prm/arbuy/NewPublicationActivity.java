package ar.uba.fi.prm.arbuy;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ar.uba.fi.prm.arbuy.pojo.Publication;
import ar.uba.fi.prm.arbuy.pojo.Response;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by pablo on 30/11/16.
 */
public class NewPublicationActivity extends AppCompatActivity{
    private static final String TAG = "NewPublicationActivity";
    public static final String BASE_URL = "http://192.168.0.101:3000/";
    private Retrofit retrofit;
    private RestAPI restAPI;
    private String mToken;

    private EditText mTitleText;
    private EditText mDescriptionText;
    private EditText mPriceText;
    private EditText mCantText;
    private TextView mPhoto;
    private TextView mArModel;
    private Button mPublishButton;
    private Toolbar mToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newpublication);
        mTitleText = (EditText) findViewById(R.id.input_title);
        mDescriptionText = (EditText) findViewById(R.id.input_summary);
        mPriceText = (EditText) findViewById(R.id.input_price);
        mCantText = (EditText) findViewById(R.id.input_quantity);
        mPhoto = (TextView) findViewById(R.id.input_image);
        mArModel = (TextView) findViewById(R.id.input_armodel);
        mPublishButton = (Button) findViewById(R.id.btn_signup);
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        mToolbar.setTitle("New publication");

        mPublishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        restAPI = retrofit.create(RestAPI.class);

        SharedPreferences preferences = getSharedPreferences("token", MODE_PRIVATE);
        mToken = preferences.getString("token", null);
    }

    public void signup() {
        Log.d(TAG, "Publish");

        if (!validate()) {
            onPublishFailed();
            return;
        }

        mPublishButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(NewPublicationActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String title = mTitleText.getText().toString();
        String description = mDescriptionText.getText().toString();
        String price = mPriceText.getText().toString();
        String cant = mCantText.getText().toString();
        String photoUri = mPhoto.getText().toString();
        String arModelUri = mArModel.getText().toString();

        List<String> photos = new ArrayList<>();
        photos.add(photoUri);
        Publication newPublication = new Publication(null,title,description,Integer.valueOf(price), arModelUri,photos,null,null,Integer.valueOf(cant),0);

        final Call<Response> response = restAPI.publish(mToken, newPublication);
        response.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(retrofit.Response<Response> response, Retrofit retrofit) {
                Log.d(TAG, "Request success");
                if(response.body().getStatus()) {
                    Log.d(TAG, "Publish success");
                    onPublishSuccess();
                    progressDialog.dismiss();
                }else{
                    Log.d(TAG, "Publish error");
                    Log.d(TAG, "Error msg " + response.body().getMessage());
                    Toast.makeText(getBaseContext(), "Publish up failed", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    mPublishButton.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(TAG, "Request failure");
            }
        });
    }


    public void onPublishSuccess() {
        mPublishButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onPublishFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        mPublishButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String title = mTitleText.getText().toString();
        String description = mDescriptionText.getText().toString();
        String price = mPriceText.getText().toString();

        if (title.isEmpty() || title.length() < 3) {
            mTitleText.setError("at least 3 characters");
            valid = false;
        } else {
            mTitleText.setError(null);
        }

        if (description.isEmpty() || description.length() < 3) {
            mDescriptionText.setError("at least 3 characters");
            valid = false;
        } else {
            mDescriptionText.setError(null);
        }

        return valid;
    }
}
