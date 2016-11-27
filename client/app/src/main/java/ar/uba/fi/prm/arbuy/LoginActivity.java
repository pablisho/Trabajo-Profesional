package ar.uba.fi.prm.arbuy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ar.uba.fi.prm.arbuy.pojo.Login;
import ar.uba.fi.prm.arbuy.pojo.Response;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by pablo on 26/11/16.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    public static final String BASE_URL = "http://192.168.0.101:3000/";
    private Retrofit retrofit;
    private RestAPI restAPI;

    private EditText mUsernameText;
    private EditText mPasswordText;
    private Button mLoginButton;
    private TextView mSignupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mUsernameText = (EditText) findViewById(R.id.input_username);
        mPasswordText = (EditText) findViewById(R.id.input_password);
        mLoginButton = (Button) findViewById(R.id.btn_login);
        mSignupLink = (TextView) findViewById(R.id.link_signup);

        mLoginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        mSignupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        restAPI = retrofit.create(RestAPI.class);

        SharedPreferences preferences = getSharedPreferences("token", MODE_PRIVATE);
        String token = preferences.getString("token", null);
        if (token != null) {
            Log.d(TAG, "Saved user token " + token);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        mLoginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String username = mUsernameText.getText().toString();
        String password = mPasswordText.getText().toString();

        Login newLogin = new Login(username, password);

        final Call<Response> response = restAPI.authenticate(newLogin);
        response.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(retrofit.Response<Response> response, Retrofit retrofit) {
                Log.d(TAG, "Request success");
                if (response.body().getStatus()) {
                    Log.d(TAG, "Login success");
                    onLoginSuccess(response.body().getToken());
                    progressDialog.dismiss();
                } else {
                    mLoginButton.setEnabled(true);
                    Log.d(TAG, "Login error");
                    Log.d(TAG, "Error msg " + response.body().getMessage());
                    Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(TAG, "Request failure");
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess(String token) {
        mLoginButton.setEnabled(true);
        SharedPreferences sharedPref = getSharedPreferences("token", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("token", token);
        editor.commit();

        // Navigate
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        mLoginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = mUsernameText.getText().toString();
        String password = mPasswordText.getText().toString();

        /*if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mUsernameText.setError("enter a valid email address");
            mUsernameText.requestFocus();
            valid = false;
        } else {
            mUsernameText.setError(null);
        }*/

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mPasswordText.setError("between 4 and 10 alphanumeric characters");
            mPasswordText.requestFocus();
            valid = false;
        } else {
            mPasswordText.setError(null);
        }

        return valid;
    }
}
