package com.example.ristoratore;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.ristoratore.menu.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

/*
Shows the orders that have been successfully delivered, pressing on one of them will show up the eventual review associated
to the delivery, if there is one.
 */
@SuppressLint("Registered")
public class DeliveredActivity extends AppCompatActivity implements OrderViewAdapter.ItemClickListener {

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

        loadData();

    }

    private void loadData() {
        database.child("restaurateur").child(uid).child("orders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for(DataSnapshot dataSnapshot1 :dataSnapshot.getChildren()){

                    Order fire = new Order();
                    if(Integer.parseInt(dataSnapshot1.child("status").getValue().toString())!=4)
                        continue;
                    fire.setOrderId(dataSnapshot1.getKey());
                    fire.setPrice(dataSnapshot1.child("priceL").getValue().toString());
                    fire.setAddress(dataSnapshot1.child("address").getValue().toString());
                    fire.setCustId(dataSnapshot1.child("custId").getValue().toString());
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
        buildRecyclerView();
    }

    @Override
    public void onItemClick(View view, int position) {
        view=getLayoutInflater().inflate(R.layout.dialog_review, null);
        AlertDialog alertDialog = new AlertDialog.Builder(DeliveredActivity.this).create();
        alertDialog.setTitle("Review for this order");

        final TextView text_tv=view.findViewById(R.id.text_tv);
        final RatingBar rate_rb=view.findViewById(R.id.rate_rb);
        database.child("reviews").child(uid).child(orders.get(position).getOrderId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()!=0) {
                    if (dataSnapshot.child("comment").getValue() != null) {
                        text_tv.setText(dataSnapshot.child("comment").getValue().toString());
                    }
                    if (dataSnapshot.child("rate").getValue() != null) {
                        rate_rb.setRating(Float.parseFloat(dataSnapshot.child("rate").getValue().toString()));
                    } else {
                        rate_rb.setRating(0);
                    }
                }
                else{
                    text_tv.setText("No review for this delivery");
                    rate_rb.setRating(0);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog.setView(view);
        alertDialog.show();

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