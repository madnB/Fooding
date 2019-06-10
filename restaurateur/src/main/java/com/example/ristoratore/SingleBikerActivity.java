package com.example.ristoratore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ristoratore.menu.Dish;
import com.example.ristoratore.menu.Order;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class SingleBikerActivity extends AppCompatActivity{
    private static final int RESULT_STATUS_CHANGE = 46;
    private static final int RESULT_STATUS_ABORT = 49;
    private Order order;
    private TextView name_tv;
    private TextView distance_tv;
    private Button abort_btn;
    private Button change_btn;
    private DatabaseReference database;
    private StorageReference storage;
    private StorageReference photoref;
    private ImageView photo_iv;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_biker);

        Intent i = getIntent();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        database= FirebaseDatabase.getInstance().getReference();
        order = (Order)i.getSerializableExtra("order");
        storage= FirebaseStorage.getInstance().getReference();
        photoref=storage.child(order.getBikerId()+"/photo.jpg");
        uid= FirebaseAuth.getInstance().getCurrentUser().getUid();

        name_tv=findViewById(R.id.name_tv);
        distance_tv=findViewById(R.id.distance_tv);
        abort_btn=findViewById(R.id.abort_btn);
        change_btn=findViewById(R.id.change_btn);
        photo_iv=findViewById(R.id.photo_iv);

        database.child("biker").child(order.getBikerId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("status").getValue().toString().equals("False")){
                    name_tv.setText(dataSnapshot.child("name").getValue().toString());
                    distance_tv.setText(dataSnapshot.child("work_area").getValue().toString());
                }
                else{
                    Toast.makeText(SingleBikerActivity.this, "Order was already delivered", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        photoref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(photo_iv);
            }
        });

        abort_btn.setOnClickListener(e->{
            final CharSequence[] items = { "Yes", "No"};
            AlertDialog.Builder builder = new AlertDialog.Builder(SingleBikerActivity.this);
            builder.setTitle("Do you want to cancel the delivery?");
            builder.setItems(items, (dialog, item) -> {
                if(items[item].equals("No"))
                    dialog.dismiss();
                if(items[item].equals("Yes")){

                    database.child("biker").child(order.getBikerId()).child("currentOrder").removeValue();
                    database.child("biker").child(order.getBikerId()).child("status").setValue("True");
                    database.child("restaurateur").child(uid).child("orders").child(order.getOrderId()).child("status").setValue("0");
                    database.child("customer").child(order.getCustId()).child("currentOrder").child("status").setValue("0");
                    order.setStatus(0);
                    order.setBikerId(null);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("order", order);
                    setResult(RESULT_STATUS_ABORT, returnIntent);
                    finish();

                }
            });
            builder.show();
        });

        change_btn.setOnClickListener(e->{
            final CharSequence[] items = { "Yes", "No"};
            AlertDialog.Builder builder = new AlertDialog.Builder(SingleBikerActivity.this);
            builder.setTitle("Abort delivery and change biker?");
            builder.setItems(items, (dialog, item) -> {
                if(items[item].equals("No"))
                    dialog.dismiss();
                if(items[item].equals("Yes")){

                    database.child("biker").child(order.getBikerId()).child("currentOrder").removeValue();
                    database.child("biker").child(order.getBikerId()).child("status").setValue("True");
                    database.child("restaurateur").child(uid).child("orders").child(order.getOrderId()).child("status").setValue("0");
                    database.child("customer").child(order.getCustId()).child("currentOrder").child("status").setValue("0");
                    order.setStatus(0);
                    order.setBikerId(null);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("order", order);
                    setResult(RESULT_STATUS_CHANGE, returnIntent);
                    finish();

                }
            });
            builder.show();
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
