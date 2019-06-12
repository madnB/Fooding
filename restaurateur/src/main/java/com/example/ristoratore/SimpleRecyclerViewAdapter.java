package com.example.ristoratore;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ristoratore.menu.Dish;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;
/*
Used to create cards for the dishes in the order.
See item_recycler_2.xml for more info.
 */
public class SimpleRecyclerViewAdapter extends RecyclerView.Adapter<SimpleRecyclerViewAdapter.ViewHolder> {

    private List<Dish> itemList;
    private LayoutInflater layInflater;
    private Typeface robotoRegular, robotoBold;

    SimpleRecyclerViewAdapter(Context context, List<Dish> data) {
        this.layInflater = LayoutInflater.from(context);
        this.itemList = data;
        robotoRegular = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
        robotoBold = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-BoldItalic.ttf");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layInflater.inflate(R.layout.item_recycler_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        Dish dish = itemList.get(pos);

        holder.dishName.setTypeface(robotoRegular);
        holder.dishName.setText(dish.getName());

        holder.dishQtySel.setTypeface(robotoBold);
        holder.dishQtySel.setText("Ordered: "+dish.getQtySel());

        StorageReference photoRef= FirebaseStorage.getInstance().getReference().child(dish.getPhotoUri());
        photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(holder.dishPhoto);
            }
        });
    }

    @Override
    public int getItemCount() {
        int arr = 0;
        try{
            if(itemList.size()==0){
                arr = 0;
            }
            else{
                arr=itemList.size();
            }
        }catch (Exception e){}
        return arr;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder /*implements View.OnClickListener, View.OnLongClickListener*/ {

        ImageView dishPhoto;
        TextView dishName;
        TextView dishDesc;
        TextView dishPrice;
        TextView dishQtySel;

        final View cardView = itemView.findViewById(R.id.card_view);
        ValueAnimator slideAnimator = ValueAnimator
                .ofInt(cardView.getLayoutParams().height, 700)
                .setDuration(300);

        ViewHolder(View itemView) {
            super(itemView);
            dishPhoto = itemView.findViewById(R.id.dish_photo_rec_iv);
            dishName = itemView.findViewById(R.id.dish_name_tv);
            dishPrice = itemView.findViewById(R.id.dish_price_tv);
            dishDesc = itemView.findViewById(R.id.dish_desc_tv);
            dishQtySel = itemView.findViewById(R.id.dish_qtySel_tv);
        }
    }

    Dish getItem(int id) {
        return itemList.get(id);
    }
}