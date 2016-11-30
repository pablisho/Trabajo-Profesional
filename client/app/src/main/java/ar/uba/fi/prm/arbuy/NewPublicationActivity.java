package ar.uba.fi.prm.arbuy;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ar.uba.fi.prm.arbuy.pojo.Publication;
import ar.uba.fi.prm.arbuy.pojo.Response;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by pablo on 30/11/16.
 */
public class NewPublicationActivity extends AppCompatActivity{
    private static final int REQ_IMAGE = 123;
    private static final int REQ_ARMODEL = 12;
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
    private Uri mPhotoUri;
    private ImageView mImageView;
    private TextView mArModel;
    private Uri mArmodelUri;
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
        mImageView = (ImageView) findViewById(R.id.photo);
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

        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent()
                        .setType("image/*")
                        .setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select a file"), REQ_IMAGE);
            }
        });

        mArModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent()
                        .setType("*/*")
                        .setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select a file"), REQ_ARMODEL);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode==REQ_IMAGE) {
                Uri selectedfile = data.getData(); //The uri with the location of the file
                mPhotoUri = selectedfile;
                mPhoto.setText(selectedfile.toString());
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedfile);
                    mImageView.setImageBitmap(bitmap);
                    mImageView.invalidate();

                    uploadFile(new File(getPath(this,selectedfile)));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if(requestCode == REQ_ARMODEL){
                Uri selectedfile = data.getData(); //The uri with the location of the file
                mArmodelUri = selectedfile;
                mArModel.setText(selectedfile.toString());
            }
        }
    }

    private void uploadFile(File file){
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part fbody = MultipartBody.Part.createFormData("userFile", file.getName(), requestFile);
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), file.getName());
        Call<Response> call = restAPI.upload(mToken, fbody);
        call.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                if (response.code() == 200) {
                    Log.d(TAG, "Request OK");
                    Response r = response.body();
                    Log.d(TAG, "File path in server " + r.getMessage());
                }
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Log.d(TAG, "Request Failure");
                t.printStackTrace();
            }
        });
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
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
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
            public void onFailure(Call<Response> call, Throwable t) {
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

    public static String getPath(Context context, Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else
            if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
