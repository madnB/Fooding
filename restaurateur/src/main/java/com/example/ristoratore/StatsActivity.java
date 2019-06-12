package com.example.ristoratore;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class StatsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference database;
    private StorageReference storage;
    private CircleImageView best_food_iv;
    private TextView name_tv;
    private TextView time_tv;
    private TextView food_qty_tv;
    private TextView time_qty_tv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        mAuth= FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        database= FirebaseDatabase.getInstance().getReference();
        storage= FirebaseStorage.getInstance().getReference();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        best_food_iv = findViewById(R.id.dish_photo_rec_iv);
        name_tv = findViewById(R.id.dish_name_tv);
        time_tv = findViewById(R.id.time_name_tv);
        food_qty_tv = findViewById(R.id.dish_qtySel_tv);
        time_qty_tv = findViewById(R.id.time_qtySel_tv_);



    }

    @Override
    protected void onResume() {
        super.onResume();

        String uid=currentUser.getUid();

        Query best_food_query = database.child("restaurateur").child(uid).child("stats").child("food").orderByValue().limitToLast(1);
        best_food_query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    name_tv.setText(childSnapshot.getKey());
                    food_qty_tv.setText("Ordered: " + childSnapshot.getValue().toString());
                    storage.child(uid+"/"+name_tv.getText()+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(best_food_iv);
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        Query best_time_query = database.child("restaurateur").child(uid).child("stats").child("time").orderByValue().limitToLast(1);
        best_time_query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    time_tv.setText(childSnapshot.getKey());
                    time_qty_tv.setText("Ordered: " + childSnapshot.getValue().toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
