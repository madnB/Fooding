package com.example.ristoratore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ristoratore.menu.Biker;
import com.example.ristoratore.menu.Dish;
import com.example.ristoratore.menu.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

public class BikerActivity extends AppCompatActivity implements BikerViewAdapter.ItemClickListener{
    private static final int RESULT_STATUS_CHANGE = 46;
    private RecyclerView rView;
    private BikerViewAdapter adapter;
    private RecyclerView.LayoutManager rLayoutManager;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm");
    ArrayList<Biker> bikers = new ArrayList<Biker>();
    private DatabaseReference database;
    private String uid;
    private String restname;
    private String restaddr;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i=getIntent();
        order = (Order)i.getSerializableExtra("order");
        setContentView(R.layout.activity_biker);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        database=FirebaseDatabase.getInstance().getReference();
        uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        database.child("restaurateur").child(uid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(dataSnapshot.getValue()==null))
                    restname=dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        database.child("restaurateur").child(uid).child("address").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(dataSnapshot.getValue()==null))
                    restaddr=dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        loadData();
    }

    private void loadData() {
        database.child("biker").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    if(dataSnapshot1.child("status").getValue().toString().equals("True")){
                        Biker fire= new Biker();
                        if(dataSnapshot1.child("name").getValue()!=null)
                            fire.setName(dataSnapshot1.child("name").getValue().toString());
                        if(dataSnapshot1.child("work_area").getValue()!=null)
                            fire.setWork_area(dataSnapshot1.child("work_area").getValue().toString());
                        if(dataSnapshot1.child("work_hours").getValue()!=null)
                            fire.setWork_hours(dataSnapshot1.child("work_hours").getValue().toString());
                        fire.setUid(dataSnapshot1.getKey());
                        fire.setPhotoUri(dataSnapshot1.getKey()+"/photo.jpg");
                        bikers.add(fire);
                    }
                }
                buildReciclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Hello", "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void buildReciclerView() {
        rView=findViewById(R.id.biker_rView);
        rLayoutManager=new LinearLayoutManager(this);
        rView.setLayoutManager(rLayoutManager);
        adapter= new BikerViewAdapter(this, bikers);
        adapter.setClickListener(this);
        rView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        //Toast.makeText(this, "Clicked " + adapter.getItem(position).getName() + " on position " + position, Toast.LENGTH_SHORT).show();
        final CharSequence[] items = { "Yes", "No"};
        AlertDialog.Builder builder = new AlertDialog.Builder(BikerActivity.this);
        builder.setTitle("Do you want this biker to deliver the order?");
        builder.setItems(items, (dialog, item) -> {
            if(items[item].equals("No"))
                dialog.dismiss();
            if(items[item].equals("Yes")){

                database.child("biker").child(adapter.getItem(position).getUid()).child("status").setValue("False");
                database.child("biker").child(adapter.getItem(position).getUid()).child("currentOrder").child("restaurantName").setValue(restname);
                database.child("biker").child(adapter.getItem(position).getUid()).child("currentOrder").child("restaurantAddress").setValue(restaddr);
                database.child("biker").child(adapter.getItem(position).getUid()).child("currentOrder").child("deliveryAddress").setValue(order.getAddress());
                database.child("biker").child(adapter.getItem(position).getUid()).child("deliveryHour").setValue(sdf.format(order.getDeliveryTime().getTime()));
                database.child("biker").child(adapter.getItem(position).getUid()).child("currentOrder").child("price").setValue(order.getPrice());
                database.child("biker").child(adapter.getItem(position).getUid()).child("currentOrder").child("info").setValue(order.getInfo());
                database.child("biker").child(adapter.getItem(position).getUid()).child("currentOrder").child("restid").setValue(uid);
                database.child("biker").child(adapter.getItem(position).getUid()).child("currentOrder").child("orderid").setValue(order.getOrderId());
                database.child("restaurateur").child(uid).child("orders").child(order.getOrderId()).child("status").setValue("3");
                Toast.makeText(BikerActivity.this, "Order has been sent to a biker!", Toast.LENGTH_SHORT).show();
                order.setStatus(3);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("order", order);
                setResult(RESULT_STATUS_CHANGE, returnIntent);
                finish();

            }
        });
        builder.show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
