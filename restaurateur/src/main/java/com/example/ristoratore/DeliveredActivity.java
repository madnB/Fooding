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

    //Hard-coded list of orders to check if it works before implementing interactivity with customer app
    /*String desc1 = "A baked potato with a fluffy interior and a crisp skin. It comes served with a butter filling";
    String desc2 = "Made by roasting carrots with olive oil, salt, and pepper, until they’re golden on the edges and tender throughout.";
    String desc3 = "The garlicky marinade on these chops highlights the lamb's slightly gamey flavor";
    Dish Potato = new Dish("Baked Potatoes with Butter", desc1,null,"5 €",(long)5,11, null);
    Dish Carrot = new Dish("Roasted Carrots", desc2,null,"4.50 €",(long)4.50,21,null);
    Dish Meat = new Dish("Grilled Lamb Chops with Roasted Garlic", desc3,null,"8 €",(long)8,13,null);
    ArrayList<Dish> dishL1 = new ArrayList<Dish>(Arrays.asList(Potato,Carrot));
    ArrayList<Dish> dishL2 = new ArrayList<Dish>(Arrays.asList(Meat,Carrot));
    ArrayList<Dish> dishL3 = new ArrayList<Dish>(Arrays.asList(Potato,Meat));
    GregorianCalendar date1 = new GregorianCalendar(2019,4,18,12,03);
    GregorianCalendar date2 = new GregorianCalendar(2019,4,18,10,00);
    GregorianCalendar date3 = new GregorianCalendar(2019,4,18,16,00);
    Order ord1 = new Order (1, 0, dishL1,"Interphone Rossani, 4th floor","Corso Duca degli Abruzzi, 22",date1,"9.5 €",(long)9.5);
    Order ord2 = new Order (2, 0, dishL2,"No garlic","Largo Francia, 2",date2,"12.5 €", (long)12.5);
    Order ord3 = new Order (3, 0, dishL3,"Third floor","Piazza Statuto, 25",date3,"13 €",(long)13);
    ArrayList<Order> orders = new ArrayList<Order>(Arrays.asList(ord1,ord2,ord3));*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        database=FirebaseDatabase.getInstance().getReference();
        uid= FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadData();
        //Collections.sort(orders);
        //buildRecyclerView();

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
                if(dataSnapshot.child("comment").getValue()!=null) {
                    text_tv.setText(dataSnapshot.child("comment").getValue().toString());
                }
                if(dataSnapshot.child("rate").getValue()!=null) {
                    rate_rb.setRating(Float.parseFloat(dataSnapshot.child("rate").getValue().toString()));
                }
                else {
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