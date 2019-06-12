package com.example.fooding;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fooding.BrowseAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/*
Activity showing the preferred restaurants by the customer.
 */
public class FavActivity extends AppCompatActivity implements BrowseAdapter.ItemClickListener{
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    FirebaseDatabase database;
    DatabaseReference myRef ;
    List<Restaurant> list = new ArrayList<>();
    RecyclerView recycle;
    TextView emptyView;
    BrowseAdapter adapter;
    private RecyclerView.LayoutManager rLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fav);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        recycle = (RecyclerView) findViewById(R.id.restaurant_rView);
        emptyView = (TextView) findViewById(R.id.empty_view);
        rLayoutManager = new LinearLayoutManager(FavActivity.this);
        recycle.setLayoutManager(rLayoutManager);
        adapter = new BrowseAdapter(FavActivity.this, list);
        adapter.setClickListener(FavActivity.this);
        recycle.setAdapter(adapter);
        mAuth= FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        String uid=currentUser.getUid();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();


    }

    @Override
    public void onItemClick(View view, int position) {
        Intent i = new Intent(getApplicationContext(), MenuActivity.class);
        i.putExtra("restaurant", adapter.getItem(position));
        i.putExtra("position", position);
        startActivity(i);
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        finish();
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();
        setContentView(R.layout.activity_fav);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        recycle = findViewById(R.id.restaurant_rView);
        emptyView = findViewById(R.id.empty_view);
        rLayoutManager = new LinearLayoutManager(FavActivity.this);
        recycle.setLayoutManager(rLayoutManager);
        adapter = new BrowseAdapter(FavActivity.this, list);
        adapter.setClickListener(FavActivity.this);
        recycle.setAdapter(adapter);
        mAuth= FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        String uid=currentUser.getUid();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        list.clear();

        myRef.child("customer").child(uid).child("favourites").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (dataSnapshot.getChildrenCount() == 0) {
                    recycle.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
                else {
                    emptyView.setVisibility(View.GONE);
                    recycle.setVisibility(View.VISIBLE);
                }
                for(DataSnapshot dataSnapshot1 :dataSnapshot.getChildren()){
                    Restaurant fire = new Restaurant();
                    fire.setUid(dataSnapshot1.getKey());
                    fire.setUri(dataSnapshot1.getKey()+"/photo.jpg");
                    myRef.child("restaurateur").child(dataSnapshot1.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            fire.setName(dataSnapshot.child("name").getValue().toString());
                            fire.setType(dataSnapshot.child("type").getValue().toString());
                            list.add(fire);
                            adapter.notifyItemInserted(list.size()-1);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }



            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Hello", "Failed to read value.", error.toException());
            }
        });
    }
}

