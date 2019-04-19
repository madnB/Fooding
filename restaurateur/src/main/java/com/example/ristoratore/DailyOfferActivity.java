package com.example.ristoratore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import java.util.ArrayList;

import com.example.ristoratore.menu.Dish;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Objects;

@SuppressLint("Registered")
public class DailyOfferActivity extends AppCompatActivity implements RecyclerViewAdapter.ItemClickListener, RecyclerViewAdapter.ItemLongClickListener {

    private static final int ADD_ITEM_REQ = 40;
    private static final int EDIT_ITEM_REQ = 41;
    private static final int RESULT_SAVE = 34;
    private static final int RESULT_DELETE = 35;
    SharedPreferences preferences;
    private static final String PREF_NAME = "DishList sp";
    private static final String DISHLIST_NAME = "Dishes List";

    private RecyclerView rView;
    private RecyclerViewAdapter adapter;
    private RecyclerView.LayoutManager rLayoutManager;
    ArrayList<Dish> dishes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dailyoffer);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        loadData();
        buildRecyclerView();

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), AddDishActivity.class);
            startActivityForResult(i, ADD_ITEM_REQ);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_ITEM_REQ && resultCode == RESULT_OK) {
            Dish itemReturned = (Dish) data.getSerializableExtra("dish_item");
            if(itemReturned != null) {
                dishes.add(itemReturned);
                adapter.notifyItemInserted(dishes.size());
                saveData();
            }
        }
        else if (requestCode == EDIT_ITEM_REQ && resultCode == RESULT_SAVE){
            Dish itemReturned = (Dish) data.getSerializableExtra("dish_item");
            int position = data.getIntExtra("position", 0);
            if(itemReturned != null){
                dishes.set(position, itemReturned);
                adapter.notifyItemChanged(position);
                saveData();

            }
        }
        else if (requestCode == EDIT_ITEM_REQ && resultCode == RESULT_DELETE){
            int position = data.getIntExtra("position", 0);
            dishes.remove(position);
            adapter.notifyItemRemoved(position);
            saveData();
        }
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(dishes);
        editor.putString(DISHLIST_NAME, json);
        editor.apply();
    }

    private void loadData() {
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString(DISHLIST_NAME, null);
        Type type = new TypeToken<ArrayList<Dish>>() {}.getType();
        dishes = gson.fromJson(json, type);

        if (dishes == null) {
            dishes = new ArrayList<>();
        }
    }

    private void buildRecyclerView() {
        rView = findViewById(R.id.dishes_rView);
        rLayoutManager = new LinearLayoutManager(this);
        rView.setLayoutManager(rLayoutManager);
        adapter = new RecyclerViewAdapter(this, dishes);
        adapter.setClickListener(this);
        adapter.setLongClkListener(this);
        rView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        buildRecyclerView();
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "Clicked " + adapter.getItem(position).getName() + " on position " + position, Toast.LENGTH_SHORT).show();
        /*
        Intent i = new Intent(getApplicationContext(), EditDishActivity.class);
        i.putExtra("dish", adapter.getItem(position));
        i.putExtra("position", position);
        startActivityForResult(i, EDIT_ITEM_REQ);
        */
    }

    public void onItemLongClick(View view, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DailyOfferActivity.this);
        final CharSequence[] items = { "Edit", "Delete", "Cancel"};
        builder.setItems(items, (dialog, item) -> {
            if (items[item].equals("Cancel")) {
                dialog.dismiss();
            } else if (items[item].equals("Edit")) {
                Intent i = new Intent(getApplicationContext(), EditDishActivity.class);
                i.putExtra("dish", adapter.getItem(position));
                i.putExtra("position", position);
                startActivityForResult(i, EDIT_ITEM_REQ);
            }
            else if (items[item].equals("Delete")){
                dishes.remove(position);
                adapter.notifyItemRemoved(position);
                saveData();
            }
        });
        builder.setTitle(adapter.getItem(position).getName());
        builder.show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}