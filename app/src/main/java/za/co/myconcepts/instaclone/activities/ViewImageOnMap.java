package za.co.myconcepts.instaclone.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import za.co.myconcepts.instaclone.R;
import za.co.myconcepts.instaclone.helpers.SetThemeHelper;

public class ViewImageOnMap extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private String latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check Theme
        SetThemeHelper.setTheme(this);

        setContentView(R.layout.activity_view_image_on_map);

        Toolbar toolbar = (Toolbar) findViewById(R.id.include);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);


        Bundle bundle = getIntent().getBundleExtra("bundle");
        String imageURL = bundle.getString("image_url");
        latitude = bundle.getString("latitude");
        longitude = bundle.getString("longitude");

        ImageView ivImage = (ImageView) findViewById(R.id.ivImage);
        Picasso.with(this).load(imageURL)
                .placeholder(R.color.cardview_dark_background)
                .resize(600,600)
                .centerCrop()
                .into(ivImage);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng ll = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 16.5f), 4000, null);
        mMap.addMarker(new MarkerOptions().position(ll));

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
                Intent browseUserIntent = new Intent(ViewImageOnMap.this, BrowseUsers.class);
                startActivity(browseUserIntent);
                break;
            case R.id.miNotifications:
                Intent notificationsIntent = new Intent(ViewImageOnMap.this, NotificationsActivity.class);
                startActivity(notificationsIntent);
                break;
            case R.id.miProfile:
                Intent profileIntent = new Intent(ViewImageOnMap.this, Profile.class);
                startActivity(profileIntent);
                break;
            case android.R.id.home:
                Intent homeIntent = new Intent(ViewImageOnMap.this, MainActivity.class);
                startActivity(homeIntent);
                break;
            case R.id.miSettings:
                Intent settingsIntent = new Intent(ViewImageOnMap.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
        }

        return true;
    }
}
