package com.example.ristoratore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.example.ristoratore.menu.Dish;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class EditDishActivity extends AppCompatActivity {

    private static final int GALLERY_REQCODE = 31;
    private static final int CAM_REQCODE = 32;
    private static final int STORAGE_PERM_CODE = 33;
    private static final int RESULT_SAVE = 34;
    private static final int RESULT_DELETE = 35;


    private Dish dish;

    private ImageView photo;
    private EditText name_et;
    private EditText desc_et;
    private CurrencyEditText price_et;
    private EditText qty_et;
    private Uri selectedPhoto;
    private Button add_image_btn;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dish_descriptor_2);

        photo = findViewById(R.id.dish_photo_iv);
        name_et = findViewById(R.id.dish_name_et);
        desc_et = findViewById(R.id.dish_desc_et);
        price_et = findViewById(R.id.dish_price_et);
        qty_et = findViewById(R.id.dish_qty_et);
        add_image_btn = findViewById(R.id.add_image_btn);
        Button save_btn = findViewById(R.id.save_dish_btn);
        Button delete_btn = findViewById(R.id.delete_dish_btn);

        if (savedInstanceState != null) {
            if(savedInstanceState.containsKey("uri_photo")) {
                selectedPhoto = savedInstanceState.getParcelable("uri");
                photo.setImageURI(selectedPhoto);
            }
        }

        add_image_btn.setOnClickListener(e -> {
            if(isStoragePermissionGranted()) { /* TO-DO : check if before I had permissions !!!!!!!!!!!!!!!!!!!!! */
                selectImage();
            }
        });

        save_btn.setOnClickListener(v -> {
            String name = name_et.getText().toString();
            String description = desc_et.getText().toString();
            ImageView photo = this.photo;
            String price = price_et.formatCurrency(price_et.getRawValue());
            Long pricel = price_et.getRawValue();
            int qty;
            if(qty_et.getText().toString().matches("^-?\\d+$"))
                qty = Integer.parseInt(qty_et.getText().toString());
            else
                qty = 1;

            dish = new Dish(name, description, photo, price, pricel, qty, selectedPhoto != null ? selectedPhoto.toString() : "");
            finish_save();
        });

        delete_btn.setOnClickListener(e -> {
            final CharSequence[] items = { "Yes", "No"};
            AlertDialog.Builder builder = new AlertDialog.Builder(EditDishActivity.this);
            builder.setTitle("Are you sure you want to delete this dish?");
            builder.setItems(items, (dialog, item) -> {
                if (items[item].equals("Yes")) {
                    finish_delete();
                } else if (items[item].equals("No")) {
                    dialog.dismiss();
                }
            });
            builder.show();
        });

        Intent i=getIntent();
        position=i.getIntExtra("position", 0);
        Dish d=(Dish)i.getSerializableExtra("dish");
        if(d.getPhotoUri() != null && !d.getPhotoUri().equals("")){
            selectedPhoto=Uri.parse(d.getPhotoUri());
            photo.setImageURI(selectedPhoto);
        }


        name_et.setText(d.getName(), TextView.BufferType.EDITABLE);
        desc_et.setText(d.getDescription(), TextView.BufferType.EDITABLE);
        price_et.setValue(d.getPriceL());
        qty_et.setText(String.valueOf(d.getAvailable_qty()), TextView.BufferType.EDITABLE);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(selectedPhoto != null)
            outState.putParcelable("uri_photo", selectedPhoto);
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission())
                return true;
            else
                requestStoragePermission();
        }
        else {
            return true;
        }
        return false;
    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(EditDishActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, (dialog, item) -> {
            if (items[item].equals("Take Photo")) {
                cameraIntent();
            } else if (items[item].equals("Choose from Library")) {
                galleryIntent();
            } else if (items[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @SuppressLint("IntentReset")
    private void galleryIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, GALLERY_REQCODE);
    }

    private void cameraIntent() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        selectedPhoto = FileProvider.getUriForFile(EditDishActivity.this, BuildConfig.APPLICATION_ID + ".provider", Objects.requireNonNull(getOutputMediaFile()));
        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, selectedPhoto);
        takePicture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivityForResult(takePicture, CAM_REQCODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {

        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case CAM_REQCODE:
                if (resultCode == RESULT_OK) {
                    photo.setImageURI(selectedPhoto);
                }
                break;
            case GALLERY_REQCODE:
                if (imageReturnedIntent != null && resultCode == RESULT_OK) {
                    selectedPhoto = imageReturnedIntent.getData();
                    photo.setImageURI(selectedPhoto);
                }
                break;
        }
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraDemo");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MAD-photo", "Oops! Failed create directory");
                return null;
            }
        }

        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +"IMG_"+ timeStamp + ".jpg");
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(EditDishActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to store images")
                    .setPositiveButton("Ok", (dialog, which) -> ActivityCompat.requestPermissions(EditDishActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERM_CODE))
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERM_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERM_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    public void finish_save() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("dish_item", dish);
        returnIntent.putExtra("position", position);
        setResult(RESULT_SAVE, returnIntent);
        super.finish();
    }

    public void finish_delete() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("position", position);
        setResult(RESULT_DELETE, returnIntent);
        super.finish();
    }
}