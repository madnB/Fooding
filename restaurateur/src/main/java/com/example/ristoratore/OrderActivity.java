package com.example.ristoratore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.ristoratore.menu.Dish;
import com.example.ristoratore.menu.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

@SuppressLint("Registered")
public class OrderActivity extends AppCompatActivity implements OrderViewAdapter.ItemClickListener {

    private static final int CHECK_ITEM_REQ = 45;
    private static final int RESULT_STATUS_CHANGE = 46;

    private RecyclerView rView;
    private OrderViewAdapter adapter;
    private RecyclerView.LayoutManager rLayoutManager;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm");
    ArrayList<Order> orders = new ArrayList<Order>();

    private DatabaseReference database;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        database=FirebaseDatabase.getInstance().getReference();
        uid= FirebaseAuth.getInstance().getCurrentUser().getUid();



    }

    private void loadData() {
        orders.clear();
        database.child("restaurateur").child(uid).child("orders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 :dataSnapshot.getChildren()){

                    Order fire = new Order();
                    if(Integer.parseInt(dataSnapshot1.child("status").getValue().toString())==4)
                        continue;
                    fire.setOrderId(dataSnapshot1.getKey());
                    fire.setPrice(dataSnapshot1.child("priceL").getValue().toString());
                    fire.setAddress(dataSnapshot1.child("address").getValue().toString());
                    fire.setCustId(dataSnapshot1.child("custId").getValue().toString());
                    if(dataSnapshot1.child("bikerId").getValue()!=null){
                        fire.setBikerId(dataSnapshot1.child("bikerId").getValue().toString());
                    }
                    fire.setStatus(Integer.parseInt(dataSnapshot1.child("status").getValue().toString()));
                    fire.setInfo(dataSnapshot1.child("info").getValue().toString());
                    try {
                        Date date=sdf.parse(dataSnapshot1.child("deliveryTime").getValue().toString());
                        GregorianCalendar cal=new GregorianCalendar();
                        cal.setTime(date);
                        fire.setDeliveryTime(cal);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    orders.add(fire);

                }

                Collections.sort(orders);
                buildRecyclerView();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Hello", "Failed to read value.", error.toException());
            }
        });
    }

    private void buildRecyclerView() {
        rView = findViewById(R.id.orders_rView);
        rLayoutManager = new LinearLayoutManager(this);
        rView.setLayoutManager(rLayoutManager);
        adapter = new OrderViewAdapter(this, orders);
        adapter.setClickListener(this);
        rView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        //buildRecyclerView();
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent i = new Intent(getApplicationContext(), SingleOrderActivity.class);
        i.putExtra("order", adapter.getItem(position));
        i.putExtra("position", position);
        startActivityForResult(i, CHECK_ITEM_REQ);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHECK_ITEM_REQ:
                if (resultCode == RESULT_STATUS_CHANGE) {
                    int position = data.getIntExtra("position", 0);
                    Order returned_order = (Order)data.getSerializableExtra("order");
                    orders.set(position,returned_order);
                    adapter.notifyItemChanged(position);
                }
                break;
        }
    }
}