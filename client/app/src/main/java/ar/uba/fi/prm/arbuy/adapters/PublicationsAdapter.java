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

import ar.uba.fi.prm.arbuy.R;
import ar.uba.fi.prm.arbuy.pojo.Publication;

/**
 * Created by pablo on 27/11/16.
 */
public class PublicationsAdapter extends RecyclerView.Adapter<PublicationsAdapter.PublicationViewHolder> {
    public static final String BASE_URL = "http://192.168.0.101:3000/";

    private List<Publication> itemList;
    private Context context;

    public PublicationsAdapter(Context context, List<Publication> itemList) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public PublicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.publication_list, null);
        PublicationViewHolder rcv = new PublicationViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(PublicationViewHolder holder, int position) {
        holder.title.setText(itemList.get(position).getTitle());
        // Set Image.
        String url = BASE_URL + "api/getResource?file=" + itemList.get(position).getImage();
        Log.d("PublicationsAdapter", "Image url " + url);
        Picasso.with(context)
                .load(url)
                .into(holder.photo);
        //holder.photo.setImageResource(itemList.get(position).getImages());
        holder.price.setText(String.valueOf(itemList.get(position).getPrice()));
        holder.description.setText(itemList.get(position).getSummary());
    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

    class PublicationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView photo;
        public TextView title;
        public TextView description;
        public TextView price;


        public PublicationViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = (TextView) itemView.findViewById(R.id.title);
            photo = (ImageView) itemView.findViewById(R.id.photo);
            price = (TextView) itemView.findViewById(R.id.price);
            description = (TextView) itemView.findViewById(R.id.description);
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(view.getContext(), "Clicked Position = " + getPosition(), Toast.LENGTH_SHORT).show();
        }
    }

}
