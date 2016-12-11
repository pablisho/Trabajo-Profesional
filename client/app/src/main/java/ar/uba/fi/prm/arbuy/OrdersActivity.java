package ar.uba.fi.prm.arbuy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import ar.uba.fi.prm.arbuy.adapters.OrdersAdapter;
import ar.uba.fi.prm.arbuy.pojo.Transaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by pablo on 27/11/16.
 */
public class OrdersActivity extends AppCompatActivity {
    private static final String TAG = "OrdersActivity";
    private Retrofit retrofit;
    private RestAPI restAPI;
    private String mToken;

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);


        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();

        retrofit = new Retrofit.Builder()
                .baseUrl(MainActivity.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        restAPI = retrofit.create(RestAPI.class);

        SharedPreferences preferences = getSharedPreferences("token", MODE_PRIVATE);
        mToken = preferences.getString("token", null);
    }

    @Override
    public void onResume(){
        super.onResume();
        Call<List<Transaction>> purchasesCall = restAPI.getPurchases(mToken);
        purchasesCall.enqueue(new Callback<List<Transaction>>() {

            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.code() == 200) {
                    List<Transaction> orders = response.body();
                    Log.d(TAG, "Received " + orders.size() + " orders");
                    for (Transaction order : orders) {
                        Log.d(TAG, "Order title: " + order.getTitle());
                    }

                    OrdersAdapter rcAdapter = new OrdersAdapter(OrdersActivity.this, orders);
                    mRecyclerView.setAdapter(rcAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                Log.d(TAG, "Request failure");
                t.printStackTrace();
            }
        });
    }
}
