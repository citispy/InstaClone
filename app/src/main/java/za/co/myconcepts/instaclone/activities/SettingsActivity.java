package za.co.myconcepts.instaclone.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import za.co.myconcepts.instaclone.R;
import za.co.myconcepts.instaclone.SettingsFragment;
import za.co.myconcepts.instaclone.helpers.SetThemeHelper;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check Theme
        SetThemeHelper.setTheme(this);

        setContentView(R.layout.activity_settings);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);

    }
}
