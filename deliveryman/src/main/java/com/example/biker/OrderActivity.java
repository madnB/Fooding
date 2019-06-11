package com.example.biker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Objects;

public class OrderActivity extends AppCompatActivity {
    private DatabaseReference database;
    private FirebaseUser currentUser;
    private TextView name_tv;
    private TextView address_tv;
    private TextView deladdress_tv;
    private TextView deltime_tv;
    private TextView price_tv;
    private TextView info_tv;
    private Button confirm_btn;
    private Button rest_ind_btn;
    private Button cust_ind_btn;
    private String rid;
    private String cid;
    private String oid;
    private String latrest;
    private String longrest;
    private String latcust;
    private String longcust;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        database= FirebaseDatabase.getInstance().getReference();
        currentUser= FirebaseAuth.getInstance().getCurrentUser();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        name_tv=findViewById(R.id.name_text);
        address_tv=findViewById(R.id.address_text);
        deladdress_tv=findViewById(R.id.deladdress_text);
        deltime_tv=findViewById(R.id.deltime_text);
        price_tv=findViewById(R.id.price_text);
        info_tv=findViewById(R.id.info_text);
        confirm_btn=findViewById(R.id.confirm_btn);
        rest_ind_btn=findViewById(R.id.rest_indication_btn);
        rest_ind_btn.setEnabled(false);
        cust_ind_btn=findViewById(R.id.cust_indication_btn);
        cust_ind_btn.setEnabled(false);
        String uid=currentUser.getUid();

        database.child("biker").child(uid).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue().toString().equals("False")){

                    DatabaseReference orderref=database.child("biker").child(uid).child("currentOrder");

                    orderref.child("deliveryAddress").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!(dataSnapshot.getValue()==null))
                                deladdress_tv.setText(dataSnapshot.getValue().toString());
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    orderref.child("deliveryHour").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!(dataSnapshot.getValue()==null))
                                deltime_tv.setText(dataSnapshot.getValue().toString());
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    orderref.child("info").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!(dataSnapshot.getValue()==null))
                                info_tv.setText(dataSnapshot.getValue().toString());
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    orderref.child("price").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!(dataSnapshot.getValue()==null))
                                price_tv.setText(dataSnapshot.getValue().toString());
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    orderref.child("restaurantAddress").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!(dataSnapshot.getValue()==null))
                                address_tv.setText(dataSnapshot.getValue().toString());
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    orderref.child("restaurantName").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!(dataSnapshot.getValue()==null))
                                name_tv.setText(dataSnapshot.getValue().toString());
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    orderref.child("restid").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!(dataSnapshot.getValue()==null))
                                rid=dataSnapshot.getValue().toString();

                            database.child("restaurateur").child(rid).child("location").child("KEY").child("l").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    latrest=dataSnapshot.child("0").getValue().toString();
                                    longrest=dataSnapshot.child("1").getValue().toString();
                                    rest_ind_btn.setEnabled(true);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    orderref.child("custid").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!(dataSnapshot.getValue()==null))
                                cid=dataSnapshot.getValue().toString();

                            database.child("customer").child(cid).child("location").child("KEY").child("l").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    latcust=dataSnapshot.child("0").getValue().toString();
                                    longcust=dataSnapshot.child("1").getValue().toString();
                                    cust_ind_btn.setEnabled(true);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    orderref.child("orderid").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!(dataSnapshot.getValue()==null))
                                oid=dataSnapshot.getValue().toString();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else
                {
                    Toast.makeText(OrderActivity.this, "No order to delivery!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        confirm_btn.setOnClickListener(e->{
            final CharSequence[] choices = { "Yes", "No"};
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(OrderActivity.this);
            dialogBuilder.setTitle("Confirm delivery?");
            dialogBuilder.setItems(choices, (dial, choice) -> {
                if (choices[choice].equals("Yes")) {
                    database.child("biker").child(uid).child("status").setValue("True");
                    database.child("restaurateur").child(rid).child("orders").child(oid).child("status").setValue("4");
                    database.child("customer").child(cid).child("currentOrder").child("status").setValue("4");
                    database.child("biker").child(uid).child("currentOrder").removeValue();
                    Double dist = Haversine.distance(Double.parseDouble(latrest),Double.parseDouble(longrest),Double.parseDouble(latcust),Double.parseDouble(longcust));
                    database.child("biker").child(uid).child("stats").child("dist").runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            if(mutableData.getValue() == null) {
                                mutableData.setValue((double)Math.round(dist*100d)/100d);
                            } else {
                                mutableData.setValue((Double) mutableData.getValue() + (double)Math.round(dist*100d)/100d);
                            }
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                        }
                    });
                    database.child("biker").child(uid).child("stats").child("num").runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            if(mutableData.getValue() == null) {
                                mutableData.setValue(1);
                            } else {
                                mutableData.setValue((Long) mutableData.getValue() + 1);
                            }
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                        }
                    });
                    Toast.makeText(OrderActivity.this, "Delivery successful!", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (choices[choice].equals("No")) {
                    dial.dismiss();
                }
            });
            dialogBuilder.show();
        });


        rest_ind_btn.setOnClickListener(e->{
            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr="+latrest+","+longrest));
            startActivity(intent);
        });

        cust_ind_btn.setOnClickListener(e->{
            Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr="+latcust+","+longcust));
            startActivity(intent);
        });

    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        finish();
        return true;
    }
}
