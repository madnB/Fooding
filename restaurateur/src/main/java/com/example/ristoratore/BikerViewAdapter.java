package com.example.ristoratore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ristoratore.menu.Biker;
import com.example.ristoratore.menu.Order;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;


public class BikerViewAdapter  extends RecyclerView.Adapter<BikerViewAdapter.ViewHolder>  {
    private List<Biker> itemList;
    private LayoutInflater layInflater;
    private ItemClickListener clkListener;
    private Typeface robotoRegular, robotoBold;

    BikerViewAdapter(Context context, List<Biker> data) {
        this.layInflater = LayoutInflater.from(context);
        this.itemList = data;
        robotoRegular = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
        robotoBold = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-BoldItalic.ttf");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layInflater.inflate(R.layout.biker_recycler, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        Biker biker=itemList.get(pos);
        holder.bikerName.setTypeface(robotoRegular);
        holder.bikerName.setText(biker.getName());

        holder.bikerWorkArea.setTypeface(robotoBold);
        if(biker.getDist()<0){
            holder.bikerWorkArea.setText("Position not available");
        }
        else{
            holder.bikerWorkArea.setText(biker.getDist().toString()+" kms");
        }

        holder.bikerWorkHours.setTypeface(robotoBold);
        holder.bikerWorkHours.setText(biker.getWork_hours());

        StorageReference photoRef= FirebaseStorage.getInstance().getReference().child(biker.getPhotoUri());
        photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(holder.bikerPhoto);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView bikerPhoto;
        TextView bikerName;
        TextView bikerWorkHours;
        TextView bikerWorkArea;

        ViewHolder(View itemView) {
            super(itemView);
            bikerPhoto = itemView.findViewById(R.id.photo_iv);
            bikerName = itemView.findViewById(R.id.name_tv);
            bikerWorkHours = itemView.findViewById(R.id.hours_tv);
            bikerWorkArea= itemView.findViewById(R.id.area_tv);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clkListener != null) clkListener.onItemClick(view, getAdapterPosition());
        }
    }
    Biker getItem(int id) {
        return itemList.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.clkListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}


