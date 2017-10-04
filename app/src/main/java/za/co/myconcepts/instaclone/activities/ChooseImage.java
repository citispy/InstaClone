package za.co.myconcepts.instaclone.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import za.co.myconcepts.instaclone.Constants;
import za.co.myconcepts.instaclone.R;
import za.co.myconcepts.instaclone.RequestPackage;
import za.co.myconcepts.instaclone.ViewModel.ChooseImageViewModel;
import za.co.myconcepts.instaclone.connectivity.ConnectionCheckHelper;
import za.co.myconcepts.instaclone.connectivity.HttpManager;
import za.co.myconcepts.instaclone.helpers.SetThemeHelper;
import za.co.myconcepts.instaclone.helpers.SharedPrefsHelper;
import za.co.myconcepts.instaclone.model.Message;
import za.co.myconcepts.instaclone.parser.MessageJSONParser;

public class ChooseImage extends AppCompatActivity implements TaskCompleted, OnMapReadyCallback {

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int PICK_FROM_CAMERA = 2;
    private ImageView imageView;
    private static String imageEncoded = "";
    private ProgressDialog pDialog;
    private Location retrievedLocation = null;
    private String cityName;
    private GoogleMap mMap;
    private Uri selectedImage = null;
    private Intent intent;
    private ChooseImageViewModel imageViewModel;
    private Geocoder mGeocoder = new Geocoder(this, Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check Theme
        SetThemeHelper.setTheme(this);

        setContentView(R.layout.activity_choose_image);

        Toolbar toolbar = (Toolbar) findViewById(R.id.include);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        imageView = (ImageView) findViewById(R.id.ivImage);

        imageViewModel = ViewModelProviders.of(this).get(ChooseImageViewModel.class);
        if (imageViewModel.getSelectedImage() != null) {
            selectedImage = imageViewModel.getSelectedImage();
            convertToBitmap();
        }

        //Get image from filterActivity
        Intent intent = getIntent();
        try {
            Uri intentURI = Uri.parse(intent.getStringExtra("image"));
            if (intentURI != null) {
                selectedImage = intentURI;
                convertToBitmap();
            }
        } catch (Exception e) {
            Log.e("onCreate", "selectedImage nulll");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getPlaceDetails();
    }

    private void getPlaceDetails() {

    }

    public void chooseImageFromGallery(View view) {
        Intent in = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(in, RESULT_LOAD_IMAGE);
    }

    //Receives the selected image from gallery
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

            selectedImage = data.getData();
            convertToBitmap();

        } else if (requestCode == PICK_FROM_CAMERA && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            }
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
            selectedImage = Uri.parse(path);
            convertToBitmap();
        }
    }

    private void convertToBitmap() {
        imageViewModel.setSelectedImage(selectedImage);
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            Picasso.with(this).load(selectedImage)
                    .resize(800, 800)
                    .centerInside()
                    .into(imageView);
            EncodingTask task = new EncodingTask(this);
            task.execute(bitmap);
        } catch (Exception e) {
            Log.e("Convert to Bitmap", "Exception");
        }
    }

    @Override
    public void onTaskComplete(Bitmap result) {

    }

    @Override
    public void onEncodingComplete(String result) {
        imageEncoded = result;
    }

    private void sendData(String image, String userID, String url) {
        String latitude = String.valueOf(retrievedLocation.getLatitude());
        String longitude = String.valueOf(retrievedLocation.getLongitude());

        RequestPackage p = new RequestPackage();
        p.setMethod(Constants.POST_METHOD);
        p.setUri(url);
        p.setParam(Constants.KEY_IMAGE, image);
        p.setParam(Constants.PREF_KEY_USER_ID, userID);
        p.setParam("latitude", latitude);
        p.setParam("longitude", longitude);
        p.setParam("city_name", cityName);

        SubmitImage submitImage = new SubmitImage();
        submitImage.execute(p);
    }

    public void uploadImage(View view) {
        if (ConnectionCheckHelper.isOnline(getApplicationContext())) {
            String userID = SharedPrefsHelper.getStringPreference(Constants.PREF_KEY_USER_ID, getApplicationContext());
            if (!imageEncoded.equals("")) {
                if (retrievedLocation != null) {
                    sendData(imageEncoded, userID, Constants.URL_PREFIX + "upload_image.php");
                } else {
                    Toast.makeText(this, "Your location hasn't been retrieved yet", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Please choose an image to upload", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "You are not connected to the internet", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        } else {
            mMap.setMyLocationEnabled(true);
            setMapLocation();
        }
    }

    private void setMapLocation() {
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                retrievedLocation = location;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                addPointToViewPort(ll);
                mMap.setOnMyLocationChangeListener(null);

                //Retrieving city
                List<Address> addresses = null;
                try {
                    addresses = mGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addresses != null && addresses.size() > 0) {
                    TextView tvCityName = (TextView) findViewById(R.id.tvCityName);
                    cityName = addresses.get(0).getLocality();
                    tvCityName.setText("Location: " + cityName);
                    tvCityName.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    private void addPointToViewPort(LatLng newPoint) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPoint, 16.5f), 4000, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 200: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        mMap.setMyLocationEnabled(true);
                        setMapLocation();
                    } catch (SecurityException e) {
                        Log.e("Security Exception", "onRequestPermissionResult");
                    }
                }
                break;
            }

            case 300: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    editImage();
                }
            }
        }
    }

    public void filter(View view) {
        if (selectedImage != null) {
            if (ActivityCompat.checkSelfPermission(ChooseImage.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ChooseImage.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 300);
            } else {
                intent = new Intent(ChooseImage.this, FilterActivity.class);
                editImage();
            }
        } else {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_LONG).show();
        }
    }

    private void editImage() {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        Uri tempImgUri = Uri.parse(path);

        intent.putExtra("image", tempImgUri.toString());
        startActivity(intent);
    }

    public void detectFace(View view) {
        if (selectedImage != null) {
            if (ActivityCompat.checkSelfPermission(ChooseImage.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ChooseImage.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 300);
            } else {
                intent = new Intent(ChooseImage.this, FaceDetectActivity.class);
                editImage();
            }
        } else {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_LONG).show();
        }
    }

    public void takePicture(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, PICK_FROM_CAMERA);
        }
    }

    public void heal(View view) {
        if (selectedImage != null) {
            if (ActivityCompat.checkSelfPermission(ChooseImage.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ChooseImage.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 300);
            } else {
                intent = new Intent(ChooseImage.this, HealActivity.class);
                editImage();
            }
        } else {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_LONG).show();
        }
    }

    private class SubmitImage extends AsyncTask<RequestPackage, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ChooseImage.this);
            pDialog.setMessage("Uploading Image");
            pDialog.setCancelable(false);
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(RequestPackage... params) {
            return HttpManager.getData(params[0]);
        }

        protected void onPostExecute(String result) {

            pDialog.dismiss();

            List<Message> messageList = MessageJSONParser.parseFeed(result);
            if (messageList != null) {
                //Image upload failure
                Message message = messageList.get(0);
                Toast.makeText(ChooseImage.this, message.getMessage(), Toast.LENGTH_LONG).show();

            } else {
                //Image upload successful
                result = result.replaceAll("\n", "");
                Toast.makeText(ChooseImage.this, "Image uploaded", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ChooseImage.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.miBrowse:
                Intent browseUserIntent = new Intent(ChooseImage.this, BrowseUsers.class);
                startActivity(browseUserIntent);
                break;
            case R.id.miNotifications:
                Intent notificationsIntent = new Intent(ChooseImage.this, NotificationsActivity.class);
                startActivity(notificationsIntent);
                break;
            case R.id.miProfile:
                Intent profileIntent = new Intent(ChooseImage.this, Profile.class);
                startActivity(profileIntent);
                break;
            case android.R.id.home:
                Intent homeIntent = new Intent(ChooseImage.this, MainActivity.class);
                startActivity(homeIntent);
                break;
            case R.id.miSettings:
                Intent settingsIntent = new Intent(ChooseImage.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
        }

        return true;
    }
}
