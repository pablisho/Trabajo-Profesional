package ar.uba.fi.prm.arbuy.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import ar.uba.fi.prm.arbuy.MainActivity;
import ar.uba.fi.prm.arbuy.R;
import ar.uba.fi.prm.arbuy.pojo.Transaction;

/**
 * Created by pablo on 27/11/16.
 */
public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private List<Transaction> itemList;
    private Context context;

    public OrdersAdapter(Context context, List<Transaction> itemList) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_list, parent, false);
        OrderViewHolder rcv = new OrderViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {
        holder.title.setText(itemList.get(position).getTitle());
        // Set Image.

        String url = MainActivity.BASE_URL + "api/getResource?file=" + itemList.get(position).getImage();
        Log.d("OrdersAdapter", "Image url " + url);
        Picasso.with(context)
                .load(url)
                .into(holder.photo);
        Log.d("OrdersAdapter", "Price " + String.valueOf(itemList.get(position).getPrice()));
        holder.price.setText("Price " +String.valueOf(itemList.get(position).getPrice()) + "$");
        holder.date.setText(itemList.get(position).getDate().toString());
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView photo;
        public TextView title;
        public TextView price;
        public TextView date;


        public OrderViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = (TextView) itemView.findViewById(R.id.title);
            photo = (ImageView) itemView.findViewById(R.id.photo);
            price = (TextView) itemView.findViewById(R.id.price);
            date = (TextView) itemView.findViewById(R.id.date);
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(view.getContext(), "Clicked Position = " + getPosition(), Toast.LENGTH_SHORT).show();
        }
    }

}
