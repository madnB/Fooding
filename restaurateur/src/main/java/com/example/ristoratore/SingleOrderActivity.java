package com.example.ristoratore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
/*
Shows info about one order, also has a button that lets you choose a biker for the delivery.
 */
@SuppressLint("Registered")
public class SingleOrderActivity extends AppCompatActivity {

    private static final int CHECK_ITEM_REQ = 45;
    private static final int RESULT_STATUS_CHANGE = 46;
    private static final int BIKER_REQ = 47;
    private static final int SINGLE_BIKER_REQ = 48;
    private static final int RESULT_STATUS_ABORT = 49;


    private RecyclerView rView;
    private SimpleRecyclerViewAdapter adapter;
    private RecyclerView.LayoutManager rLayoutManager;
    ArrayList<Dish> dishes=new ArrayList<>();
    private int position;
    private Order order;
    private TextView address;
    private TextView deliveryTime;
    private TextView info;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm");
    private ImageView orderStatus;
    private ImageButton switch_status_btn;

    private DatabaseReference database;
    private DatabaseReference orderRef;
    private String uid;
    private String restname;
    private String restaddr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_order);

        Intent i = getIntent();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        database= FirebaseDatabase.getInstance().getReference();
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


        position = i.getIntExtra("position",0);
        order = (Order)i.getSerializableExtra("order");
        orderRef=database.child("restaurateur").child(uid).child("orders").child(order.getOrderId());
        address = findViewById(R.id.address);
        deliveryTime = findViewById(R.id.deliveryTime);
        info = findViewById(R.id.info);
        orderStatus = findViewById(R.id.status_iv);
        switch_status_btn = findViewById(R.id.switch_status_btn);

        address.setText(order.getAddress());

        info.setText(order.getInfo());

        switch(order.getStatus()) {
            case 0:
                orderStatus.setImageResource(R.mipmap.new_order);
                break;
            case 1:
                orderStatus.setImageResource(R.mipmap.cooking);
                break;
            case 2:
                orderStatus.setImageResource(R.mipmap.ready);
                break;
            case 3:
                orderStatus.setImageResource(R.mipmap.in_delivery);
                break;
            case 4:
                orderStatus.setImageResource(R.mipmap.delivered);
                break;
        }

        deliveryTime.setText(sdf.format(order.getDeliveryTime().getTime()));

        loadData();

        switch_status_btn.setOnClickListener(e -> {
            if(order.getStatus()==3){
                Intent intent = new Intent(getApplicationContext(), SingleBikerActivity.class);
                intent.putExtra("order", order);
                startActivityForResult(intent, SINGLE_BIKER_REQ);
            }
            else if(order.getStatus()==4){
                Toast.makeText(SingleOrderActivity.this, "Order was already delivered", Toast.LENGTH_SHORT).show();
                return;
            }
            else {
                Intent intent = new Intent(getApplicationContext(), BikerActivity.class);
                intent.putExtra("order", order);
                startActivityForResult(intent, BIKER_REQ);
            }

        });

    }


    private void loadData() {
        DatabaseReference dishesRef=database.child("restaurateur").child(uid).child("orders").child(order.getOrderId()).child("dishes");
        dishesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 :dataSnapshot.getChildren()){

                    Dish fire = new Dish();
                    fire.setName(dataSnapshot1.getKey());
                    fire.setQtySel(Integer.parseInt(dataSnapshot1.getValue().toString()));
                    fire.setPhotoUri(uid+"/"+dataSnapshot1.getKey()+".jpg");
                    dishes.add(fire);

                }
                buildRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void buildRecyclerView() {
        rView = findViewById(R.id.dishes_rView);
        rLayoutManager = new LinearLayoutManager(this);
        rView.setLayoutManager(rLayoutManager);
        adapter = new SimpleRecyclerViewAdapter(this, dishes);
        rView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }



    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("position", position);
        returnIntent.putExtra("order", order);
        setResult(RESULT_STATUS_CHANGE, returnIntent);
        finish();
        return true;
    }

    @Override
    public void onBackPressed()
    {

        Intent returnIntent = new Intent();
        returnIntent.putExtra("position", position);
        returnIntent.putExtra("order", order);
        setResult(RESULT_STATUS_CHANGE, returnIntent);

        super.onBackPressed();


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BIKER_REQ:
                if (resultCode == RESULT_STATUS_CHANGE) {
                    Order returned_order = (Order)data.getSerializableExtra("order");
                    if(returned_order.getStatus()==3) {
                        orderStatus.setImageResource(R.mipmap.in_delivery);
                        order.setStatus(3);
                        order.setBikerId(returned_order.getBikerId());
                    }
                }
                break;
            case SINGLE_BIKER_REQ:
                if (resultCode == RESULT_STATUS_ABORT) {
                    Order returned_order = (Order)data.getSerializableExtra("order");
                    if(returned_order.getStatus()==0) {
                        orderStatus.setImageResource(R.mipmap.new_order);
                        order.setStatus(0);
                        order.setBikerId(null);
                    }
                }
                else if(resultCode == RESULT_STATUS_CHANGE){
                    Order returned_order = (Order)data.getSerializableExtra("order");
                    if(returned_order.getStatus()==0) {
                        orderStatus.setImageResource(R.mipmap.new_order);
                        order.setStatus(0);
                        order.setBikerId(null);
                        Intent intent = new Intent(getApplicationContext(), BikerActivity.class);
                        intent.putExtra("order", order);
                        startActivityForResult(intent, BIKER_REQ);
                    }
                }
                break;
        }
    }
}