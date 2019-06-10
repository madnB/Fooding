package com.example.fooding;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class CurrentOrderActivity extends AppCompatActivity {
    private Order order;
    private TextView name_tv;
    private TextView distance_tv;
    private Button accept_btn;
    private Button review_btn;
    private DatabaseReference database;
    private StorageReference storage;
    private StorageReference photoref;
    private ImageView photo_iv;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curr_order);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        database= FirebaseDatabase.getInstance().getReference();
        storage= FirebaseStorage.getInstance().getReference();
        uid= FirebaseAuth.getInstance().getCurrentUser().getUid();

        name_tv=findViewById(R.id.name_tv);
        distance_tv=findViewById(R.id.distance_tv);
        accept_btn=findViewById(R.id.accept_btn);
        review_btn=findViewById(R.id.review_btn);
        photo_iv=findViewById(R.id.photo_iv);

        database.child("customer").child(uid).child("currentOrder").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name_tv.setText(dataSnapshot.child("address").getValue().toString());
                distance_tv.setText(dataSnapshot.child("deliveryTime").getValue().toString());
                int status=Integer.parseInt(dataSnapshot.child("status").getValue().toString());
                switch(status){
                        case 0:
                            photo_iv.setImageResource(R.mipmap.new_order);
                            break;
                        case 1:
                            photo_iv.setImageResource(R.mipmap.cooking);
                            break;
                        case 2:
                            photo_iv.setImageResource(R.mipmap.ready);
                            break;
                        case 3:
                            photo_iv.setImageResource(R.mipmap.in_delivery);
                            break;
                        case 4:
                            photo_iv.setImageResource(R.mipmap.delivered);
                            break;
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        database.child("customer").child(uid).child("currentOrder").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int status=Integer.parseInt(dataSnapshot.child("status").getValue().toString());
                switch(status){
                    case 0:
                        photo_iv.setImageResource(R.mipmap.new_order);
                        break;
                    case 1:
                        photo_iv.setImageResource(R.mipmap.cooking);
                        break;
                    case 2:
                        photo_iv.setImageResource(R.mipmap.ready);
                        break;
                    case 3:
                        photo_iv.setImageResource(R.mipmap.in_delivery);
                        break;
                    case 4:
                        photo_iv.setImageResource(R.mipmap.delivered);
                        break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
