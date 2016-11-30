package ar.uba.fi.prm.arbuy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import ar.uba.fi.prm.arbuy.adapters.PublicationsAdapter;
import ar.uba.fi.prm.arbuy.pojo.Publication;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_NEWPUB = 33;
    private static final String TAG = "MainActivity";
    public static final String BASE_URL = "http://192.168.0.101:3000/";
    private Retrofit retrofit;
    private RestAPI restAPI;
    private String mToken;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private StaggeredGridLayoutManager gaggeredGridLayoutManager;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;

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

        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        gaggeredGridLayoutManager = new StaggeredGridLayoutManager(2, 1);
        mRecyclerView.setLayoutManager(gaggeredGridLayoutManager);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        int id = menuItem.getItemId();
                        Intent intent = null;
                        switch (id){
                            case R.id.nav_compras:
                                intent = new Intent(getApplicationContext(),OrdersActivity.class);
                                break;
                            case R.id.nav_publications:
                                intent = new Intent(getApplicationContext(),YourPublicationsActivity.class);
                                break;
                            case R.id.nav_ventas:
                                intent = new Intent(getApplicationContext(),SalesActivity.class);
                                break;
                            case R.id.nav_logout:
                                SharedPreferences preferences = getSharedPreferences("token", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("token", null);
                                editor.commit();
                                finish();
                                break;
                        }
                        if(intent != null) {
                            startActivity(intent);
                        }
                        return true;
                    }
                });


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
            public void onResponse(Call<List<Publication>> call, Response<List<Publication>> response) {
                if (response.code() == 200) {
                    List<Publication> publications = response.body();
                    Log.d(TAG, "Received " + publications.size() + " publications");
                    for (Publication publication : publications) {
                        Log.d(TAG, "Publication: " + publication.getTitle());
                    }

                    PublicationsAdapter rcAdapter = new PublicationsAdapter(MainActivity.this, publications);
                    mRecyclerView.swapAdapter(rcAdapter,false);
                    mRecyclerView.invalidate();
                }
            }

            @Override
            public void onFailure(Call<List<Publication>> call, Throwable t) {
                Log.d(TAG, "Request failure");
                t.printStackTrace();
            }
        });
    }

    public void newPublication(View view){
        // Start the Signup activity
        Intent intent = new Intent(getApplicationContext(), NewPublicationActivity.class);
        startActivityForResult(intent, REQUEST_NEWPUB);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NEWPUB) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this,"Publication succesful", Toast.LENGTH_LONG).show();
            }
        }
    }
}
