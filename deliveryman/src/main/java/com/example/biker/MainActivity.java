package com.example.biker;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    public static final String NAME_PREFS = "name_prefs";

    SharedPreferences preferences;
    private CircleImageView avatar;
    private TextView name_tv;
    private TextView alert_tv;
    private DrawerLayout mDrawerLayout;
    private NavigationView nv;
    ActionBarDrawerToggle abdToggle;
    private StorageReference photoref;
    private ToggleButton toggleButton;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        setContentView(R.layout.activity_main_drawer);

        //preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
        {
            FirebaseMessaging.getInstance().subscribeToTopic(FirebaseAuth.getInstance().getCurrentUser().getUid());

        }

        database=FirebaseDatabase.getInstance();
        myRef=database.getReference();

        mDrawerLayout = findViewById(R.id.drawer_layout);
        nv = findViewById(R.id.nav_view);
        nv.setNavigationItemSelectedListener(navSelectListener);

        View header = nv.getHeaderView(0);
        avatar = header.findViewById(R.id.avatar);
        name_tv = header.findViewById(R.id.textView);


        /*if(preferences.contains(EditActivity.URI_PREFS)) {
            avatar.setImageURI(Uri.parse(preferences.getString(EditActivity.URI_PREFS, "")));
            if (avatar.getDrawable() == null)
                avatar.setImageResource(R.mipmap.deliveryman);
        }
        if(preferences.contains(EditActivity.NAME_PREFS))
            name_tv.setText(preferences.getString(NAME_PREFS, "")); */

        if(FirebaseAuth.getInstance().getCurrentUser() == null)
            avatar.setImageResource(R.mipmap.iconmonstr_256);
        else{
            photoref= FirebaseStorage.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()+"/photo.jpg");
            photoref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(avatar);
                }
            });
            FirebaseDatabase.getInstance().getReference().child("biker").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!(dataSnapshot.getValue()==null))
                        name_tv.setText(dataSnapshot.getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            if(checkLocationPermission())
                startService(new Intent(this, LocationService.class).putExtra("databaseurl", FirebaseDatabase.getInstance().getReference().child("biker").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("location").toString()));

        }

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        abdToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {};
        mDrawerLayout.addDrawerListener(abdToggle);
        abdToggle.syncState();

        alert_tv = findViewById(R.id.alert_text);

        toggleButton = (ToggleButton) findViewById(R.id.myToggleButton);
        myRef.child("biker").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.getValue().toString().equals("True")){
                        toggleButton.setChecked(true);
                        toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.biker_available));
                    }
                    else {
                        toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.biker_unavailable));
                        toggleButton.setChecked(false);
                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myRef.child("biker").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("currentOrder").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    toggleButton.setEnabled(false);
                }
                else toggleButton.setEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.biker_available));
                    myRef.child("biker").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status").setValue("True");
                }
                else {
                    toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.biker_unavailable));
                    myRef.child("biker").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status").setValue("False");
                }

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_main_drawer);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        nv = findViewById(R.id.nav_view);
        nv.setNavigationItemSelectedListener(navSelectListener);

        View header = nv.getHeaderView(0);
        avatar = header.findViewById(R.id.avatar);
        name_tv = header.findViewById(R.id.textView);


        /*if(preferences.contains(EditActivity.URI_PREFS)) {
            avatar.setImageURI(Uri.parse(preferences.getString(EditActivity.URI_PREFS, "")));
            if (avatar.getDrawable() == null)
                avatar.setImageResource(R.drawable.ic_launcher_foreground);
        }
        if(preferences.contains(EditActivity.NAME_PREFS))
            name_tv.setText(preferences.getString(NAME_PREFS, "")); */

        if(FirebaseAuth.getInstance().getCurrentUser() == null)
            avatar.setImageResource(R.mipmap.iconmonstr_256);
        else{
            photoref= FirebaseStorage.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()+"/photo.jpg");
            photoref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(avatar);
                }
            });
            FirebaseDatabase.getInstance().getReference().child("biker").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!(dataSnapshot.getValue()==null))
                        name_tv.setText(dataSnapshot.getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            /*if(checkLocationPermission())
                startService(new Intent(this, LocationService.class).putExtra("databaseurl", FirebaseDatabase.getInstance().getReference().child("biker").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("location").toString()));*/
        }

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        abdToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {};        mDrawerLayout.addDrawerListener(abdToggle);
        abdToggle.syncState();

        alert_tv = findViewById(R.id.alert_text);

        toggleButton = (ToggleButton) findViewById(R.id.myToggleButton);
        myRef.child("biker").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue().toString().equals("True")){
                    toggleButton.setChecked(true);
                    toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.biker_available));
                }
                else {
                    toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.biker_unavailable));
                    toggleButton.setChecked(false);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myRef.child("biker").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("currentOrder").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    toggleButton.setEnabled(false);
                }
                else toggleButton.setEnabled(true);

                if (toggleButton.isEnabled()) {
                    alert_tv.setVisibility(View.GONE);
                }
                else {
                    alert_tv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.biker_available));
                    myRef.child("biker").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status").setValue("True");
                }
                else {
                    toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.biker_unavailable));
                    myRef.child("biker").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status").setValue("False");
                }

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (abdToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    public boolean checkLocationPermission()
    {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private NavigationView.OnNavigationItemSelectedListener navSelectListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            choiceActivity((String)item.getTitle());
            item.setChecked(true);
            mDrawerLayout.closeDrawer(nv);
            return true;
        }
    };

    private void choiceActivity(String title) {
        if(title.equals("PROFILE")) {
            if(FirebaseAuth.getInstance().getCurrentUser()==null){
                Toast.makeText(this, "Error: user not signed in.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(i);
        }
        if(title.equals("ORDER")) {
            if(FirebaseAuth.getInstance().getCurrentUser()==null){
                Toast.makeText(this, "Error: user not signed in.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(getApplicationContext(), OrderActivity.class);
            startActivity(i);
        }
        if(title.equals("SIGN UP")) {
            Intent i = new Intent(getApplicationContext(), SignupActivity.class);
            startActivity(i);
        }
        if(title.equals("SIGN IN")) {
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        }
        if(title.equals("SIGN OUT")) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(FirebaseAuth.getInstance().getCurrentUser().getUid());
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "User signed out.",
                    Toast.LENGTH_SHORT).show();
            finish();
            startActivity(getIntent());
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopService(new Intent(this,LocationService.class));
    }
}
