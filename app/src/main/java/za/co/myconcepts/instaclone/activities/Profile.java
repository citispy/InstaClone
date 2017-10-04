package za.co.myconcepts.instaclone.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import za.co.myconcepts.instaclone.Constants;
import za.co.myconcepts.instaclone.R;
import za.co.myconcepts.instaclone.helpers.SetThemeHelper;
import za.co.myconcepts.instaclone.helpers.SharedPrefsHelper;

public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check Theme
        SetThemeHelper.setTheme(this);

        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.include);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
    }

    public void logout(View view) {
        SharedPrefsHelper.setStringChoice(this, Constants.PREF_KEY_USER_ID, "");
        Toast.makeText(this, "You have been logged out", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Profile.this, Login.class);
        startActivity(intent);
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
                Intent browseUserIntent = new Intent(Profile.this, BrowseUsers.class);
                startActivity(browseUserIntent);
                break;
            case R.id.miNotifications:
                Intent notificationsIntent = new Intent(Profile.this, NotificationsActivity.class);
                startActivity(notificationsIntent);
                break;
            case R.id.miProfile:
                Intent profileIntent = new Intent(Profile.this, Profile.class);
                startActivity(profileIntent);
                break;
            case android.R.id.home:
                Intent homeIntent = new Intent(Profile.this, MainActivity.class);
                startActivity(homeIntent);
                break;
            case R.id.miSettings:
                Intent settingsIntent = new Intent(Profile.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
        }

        return true;
    }
}
