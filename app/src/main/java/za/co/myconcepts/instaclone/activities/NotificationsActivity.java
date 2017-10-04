package za.co.myconcepts.instaclone.activities;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import za.co.myconcepts.instaclone.Constants;
import za.co.myconcepts.instaclone.NotificationsAdapter;
import za.co.myconcepts.instaclone.R;
import za.co.myconcepts.instaclone.RequestPackage;
import za.co.myconcepts.instaclone.ViewModel.NotificationsViewModel;
import za.co.myconcepts.instaclone.connectivity.ConnectionCheckHelper;
import za.co.myconcepts.instaclone.connectivity.HttpManager;
import za.co.myconcepts.instaclone.helpers.SetThemeHelper;
import za.co.myconcepts.instaclone.helpers.SharedPrefsHelper;
import za.co.myconcepts.instaclone.model.NotificationModel;
import za.co.myconcepts.instaclone.parser.NotificationsJSONParser;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager llm = new LinearLayoutManager(this);
    private ProgressDialog pDialog;
    private List<NotificationModel> notificationModelList;
    private NotificationsViewModel notificationsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check Theme
        SetThemeHelper.setTheme(this);

        setContentView(R.layout.activity_notifications);
        notificationsViewModel = ViewModelProviders.of(this).get(NotificationsViewModel.class);

        Toolbar toolbar = (Toolbar) findViewById(R.id.include);
        setSupportActionBar(toolbar);


        //Setting up recyclerview objects
        recyclerView = (RecyclerView) findViewById(R.id.rvNotifications);
        recyclerView.setHasFixedSize(true);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        //Checking for network connection and request data if available
        if (ConnectionCheckHelper.isOnline(this)) {
            if(notificationsViewModel.getNotificationModelList() == null) {
                requestData(Constants.URL_PREFIX + "display_notifications.php");
            } else{
                NotificationsAdapter adapter = new NotificationsAdapter(notificationsViewModel.getNotificationModelList(),
                        getApplicationContext());
                recyclerView.setAdapter(adapter);
            }
        } else {
            Toast.makeText(NotificationsActivity.this, "No internet connectivity", Toast.LENGTH_LONG).show();
        }
    }

    private void requestData(String url) {
        String userID = SharedPrefsHelper.getStringPreference(Constants.PREF_KEY_USER_ID, getApplicationContext());

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
            pDialog = new ProgressDialog(NotificationsActivity.this);
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
            notificationModelList = NotificationsJSONParser.parseFeed(result);
            if (notificationModelList != null) {
                NotificationsAdapter adapter = new NotificationsAdapter(notificationModelList, getApplicationContext());
                recyclerView.setAdapter(adapter);
                notificationsViewModel.setNotificationModelList(notificationModelList);
            } else {
                Toast.makeText(NotificationsActivity.this, "No notifications to display", Toast.LENGTH_LONG).show();
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
                Intent browseUserIntent = new Intent(NotificationsActivity.this, BrowseUsers.class);
                startActivity(browseUserIntent);
                break;
            case R.id.miNotifications:
                Intent notificationsIntent = new Intent(NotificationsActivity.this, NotificationsActivity.class);
                startActivity(notificationsIntent);
                break;
            case R.id.miProfile:
                Intent profileIntent = new Intent(NotificationsActivity.this, Profile.class);
                startActivity(profileIntent);
                break;
            case android.R.id.home:
                Intent homeIntent = new Intent(NotificationsActivity.this, MainActivity.class);
                startActivity(homeIntent);
                break;
            case R.id.miSettings:
                Intent settingsIntent = new Intent(NotificationsActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
        }

        return true;
    }

}
