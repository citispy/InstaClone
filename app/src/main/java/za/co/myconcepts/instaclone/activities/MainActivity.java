package za.co.myconcepts.instaclone.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import za.co.myconcepts.instaclone.Constants;
import za.co.myconcepts.instaclone.ImageAdapter;
import za.co.myconcepts.instaclone.R;
import za.co.myconcepts.instaclone.RequestPackage;
import za.co.myconcepts.instaclone.ViewModel.ImageViewModel;
import za.co.myconcepts.instaclone.connectivity.ConnectionCheckHelper;
import za.co.myconcepts.instaclone.connectivity.HttpManager;
import za.co.myconcepts.instaclone.helpers.SetThemeHelper;
import za.co.myconcepts.instaclone.helpers.SharedPrefsHelper;
import za.co.myconcepts.instaclone.model.Image;
import za.co.myconcepts.instaclone.parser.ImageJSONParser;
import za.co.myconcepts.instaclone.services.NotificationService;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager llm = new LinearLayoutManager(this);
    private ProgressDialog pDialog;
    private boolean isLoggedIn = false;
    private int minutes;
    private List<Image> imageList;
    private ImageViewModel imageViewModel;
    private String intentUserID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check Theme
        SetThemeHelper.setTheme(this);

        //receive user ID if coming from browse users activity
        setContentView(R.layout.activity_main);
        Intent userIntent = getIntent();
        intentUserID = userIntent.getStringExtra("intentUserID");

        imageViewModel = ViewModelProviders.of(this).get(ImageViewModel.class);

        Toolbar toolbar = (Toolbar) findViewById(R.id.include);
        setSupportActionBar(toolbar);

        if (SharedPrefsHelper.getIntPreference("interval", getApplicationContext()) == 0) {
            SharedPrefsHelper.setIntChoice("interval", 1);
        }
        minutes = SharedPrefsHelper.getIntPreference("interval", getApplicationContext());

        //Open Login activity if user not logged in
        if (SharedPrefsHelper.getStringPreference(Constants.PREF_KEY_USER_ID,
                getApplicationContext()).equals("")) {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
        } else {
            isLoggedIn = true;
        }

        //Setting up recyclerview objects
        recyclerView = (RecyclerView) findViewById(R.id.rvImage);
        recyclerView.setHasFixedSize(true);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        if (isLoggedIn) {
            //Checking for network connection and request data if available
            if (ConnectionCheckHelper.isOnline(this)) {
                if (imageViewModel.getImageList() == null) {
                    if (intentUserID == null) {
                        requestData(Constants.URL_PREFIX + "display_images.php");
                    } else {
                        requestData(Constants.URL_PREFIX + "user_images.php");
                    }
                } else {
                    ImageAdapter adapter = new ImageAdapter(imageViewModel.getImageList(), MainActivity.this);
                    recyclerView.setAdapter(adapter);
                }
            } else {
                Toast.makeText(MainActivity.this, "No internet connectivity", Toast.LENGTH_LONG).show();
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
                Intent browseUserIntent = new Intent(MainActivity.this, BrowseUsers.class);
                startActivity(browseUserIntent);
                break;
            case R.id.miNotifications:
                Intent notificationsIntent = new Intent(MainActivity.this, NotificationsActivity.class);
                startActivity(notificationsIntent);
                break;
            case R.id.miProfile:
                Intent profileIntent = new Intent(MainActivity.this, Profile.class);
                startActivity(profileIntent);
                break;
            case R.id.miSettings:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
        }

        return true;
    }

    private void requestData(String url) {
        String userID;
        if (intentUserID == null) {
            userID = SharedPrefsHelper.getStringPreference(Constants.PREF_KEY_USER_ID, getApplicationContext());
        } else {
            userID = intentUserID;
        }
        RequestPackage p = new RequestPackage();
        p.setMethod(Constants.GET_METHOD);
        p.setUri(url);
        p.setParam(Constants.PREF_KEY_USER_ID, userID);

        Downloader downloader = new Downloader();
        downloader.execute(p);
    }

    private class Downloader extends AsyncTask<RequestPackage, String, String> {

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Connecting");
            pDialog.setCancelable(false);
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(RequestPackage... params) {
            return HttpManager.getData(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
            try {
                imageList = ImageJSONParser.parseFeed(result);
            }catch (Exception e){
                Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_LONG).show();
            }
            if (imageList != null) {
                ImageAdapter adapter = new ImageAdapter(imageList, MainActivity.this);
                recyclerView.setAdapter(adapter);
                imageViewModel.setImageList(imageList);
            } else {
                String message = "No images to display. Please start following some people";
                if (intentUserID != null) message = "This user doesn't have any images";
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, BrowseUsers.class);
                startActivity(intent);
            }
        }
    }

    public void addImage(View view) {
        Intent intent = new Intent(MainActivity.this, ChooseImage.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, NotificationService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        am.cancel(pi);
        // by my own convention, minutes <= 0 means notifications are disabled
        if (minutes > 0) {
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.
                    elapsedRealtime() + minutes * 60 * 1000, minutes * 60 * 1000, pi);
        }
    }
}
