package ar.uba.fi.prm.arbuy;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ar.uba.fi.prm.arbuy.pojo.Response;
import ar.uba.fi.prm.arbuy.pojo.User;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by pablo on 26/11/16.
 */
public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    public static final String BASE_URL = "http://192.168.0.101:3000/";
    private Retrofit retrofit;
    private RestAPI restAPI;

    private EditText mUsernameText;
    private EditText mEmailText;
    private EditText mPasswordText;
    private EditText mFirstName;
    private EditText mLastName;
    private EditText mAddress;
    private EditText mCity;
    private Button mSignupButton;
    private TextView mLoginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newpublication);
        mUsernameText = (EditText) findViewById(R.id.input_title);
        mEmailText = (EditText) findViewById(R.id.input_summary);
        mPasswordText = (EditText) findViewById(R.id.input_price);
        mFirstName = (EditText) findViewById(R.id.input_quantity);
        mLastName = (EditText) findViewById(R.id.input_image);
        mAddress = (EditText) findViewById(R.id.input_address);
        mCity = (EditText) findViewById(R.id.input_city);
        mSignupButton = (Button) findViewById(R.id.btn_signup);
        mLoginLink = (TextView) findViewById(R.id.link_login);

        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        mLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        restAPI = retrofit.create(RestAPI.class);
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        mSignupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String username = mUsernameText.getText().toString();
        String email = mEmailText.getText().toString();
        String password = mPasswordText.getText().toString();
        String firstName = mFirstName.getText().toString();
        String lastName = mLastName.getText().toString();
        String address = mAddress.getText().toString();
        String city = mCity.getText().toString();

        User newUser = new User(username,password,email,firstName,lastName,address,city);

        final Call<Response> response = restAPI.signup(newUser);
        response.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(retrofit.Response<Response> response, Retrofit retrofit) {
                Log.d(TAG, "Request success");
                if(response.body().getStatus()) {
                    Log.d(TAG, "Signup success");
                    onSignupSuccess();
                    progressDialog.dismiss();
                }else{
                    Log.d(TAG, "Signup error");
                    Log.d(TAG, "Error msg " + response.body().getMessage());
                    Toast.makeText(getBaseContext(), "Sign up failed", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    mSignupButton.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(TAG, "Request failure");
            }
        });
    }


    public void onSignupSuccess() {
        mSignupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        mSignupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = mUsernameText.getText().toString();
        String email = mEmailText.getText().toString();
        String password = mPasswordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            mUsernameText.setError("at least 3 characters");
            valid = false;
        } else {
            mUsernameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailText.setError("enter a valid email address");
            valid = false;
        } else {
            mEmailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mPasswordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            mPasswordText.setError(null);
        }

        return valid;
    }
}