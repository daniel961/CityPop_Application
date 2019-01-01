package com.citypop.hit.citypop;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecyclerViewAdapter extends  RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<cityUpdateObject> updatesObjects = new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdapter(Context mContext,ArrayList<cityUpdateObject> updatesObjects) {
        this.updatesObjects = updatesObjects;
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_recycler_view_list_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder: " + position);
        Log.i(TAG, "onBindViewHolder: " + updatesObjects.get(position).imageUrl);
        holder.date_tv.setText(updatesObjects.get(position).date);
        holder.time_tv.setText(updatesObjects.get(position).time);
        holder.city_tv.setText(updatesObjects.get(position).city);
        holder.title_tv.setText(updatesObjects.get(position).subject);
        holder.text_tv.setText(updatesObjects.get(position).text);
        holder.author_tv.setText(updatesObjects.get(position).author);



        Picasso.get().load(updatesObjects.get(position).imageUrl).error(R.drawable.citypop_logo).into(holder.Iv_update_Image);



    }

    @Override
    public int getItemCount() {
        return updatesObjects.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout parentLayout;
        TextView date_tv;
        TextView time_tv;
        TextView city_tv;
        TextView title_tv;
        TextView text_tv;
        ImageView Iv_update_Image;
        TextView author_tv;


        public ViewHolder(View itemView) {
            super(itemView);
            //refs
            parentLayout = (LinearLayout) itemView.findViewById(R.id.parentLayout);
            date_tv = (TextView) itemView.findViewById(R.id.date_tv);
            time_tv = (TextView) itemView.findViewById(R.id.time_tv);
            city_tv = (TextView) itemView.findViewById(R.id.city_tv);
            title_tv = (TextView) itemView.findViewById(R.id.title_tv);
            text_tv = (TextView) itemView.findViewById(R.id.text_tv);
            author_tv = (TextView) itemView.findViewById(R.id.author_tv);
            Iv_update_Image = (ImageView) itemView.findViewById(R.id.Iv_update_Image);

        }
    }

}
