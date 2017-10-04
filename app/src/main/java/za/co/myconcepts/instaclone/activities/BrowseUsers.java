package za.co.myconcepts.instaclone.activities;

import android.app.ProgressDialog;
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

import za.co.myconcepts.instaclone.BrowseUserAdapter;
import za.co.myconcepts.instaclone.Constants;
import za.co.myconcepts.instaclone.ImageAdapter;
import za.co.myconcepts.instaclone.R;
import za.co.myconcepts.instaclone.RequestPackage;
import za.co.myconcepts.instaclone.ViewModel.BrowseUsersViewModel;
import za.co.myconcepts.instaclone.connectivity.ConnectionCheckHelper;
import za.co.myconcepts.instaclone.connectivity.HttpManager;
import za.co.myconcepts.instaclone.helpers.SetThemeHelper;
import za.co.myconcepts.instaclone.helpers.SharedPrefsHelper;
import za.co.myconcepts.instaclone.model.Image;
import za.co.myconcepts.instaclone.model.User;
import za.co.myconcepts.instaclone.parser.ImageJSONParser;
import za.co.myconcepts.instaclone.parser.UserJSONParser;

public class BrowseUsers extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager llm = new LinearLayoutManager(this);
    private ProgressDialog pDialog;
    private List<User> userList;
    private BrowseUsersViewModel usersViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check Theme
        SetThemeHelper.setTheme(this);

        setContentView(R.layout.activity_browse_users);

        usersViewModel = ViewModelProviders.of(this).get(BrowseUsersViewModel.class);

        Toolbar toolbar = (Toolbar) findViewById(R.id.include);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        //Setting up recyclerview objects
        recyclerView = (RecyclerView) findViewById(R.id.rvBrowseUsers);
        recyclerView.setHasFixedSize(true);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        //Checking for network connection and request data if available
        if (ConnectionCheckHelper.isOnline(this)) {
            if(usersViewModel.getUserlist() == null) {
                requestData(Constants.URL_PREFIX + "display_users.php");
            } else {
                BrowseUserAdapter adapter = new BrowseUserAdapter(usersViewModel.getUserlist(), BrowseUsers.this);
                recyclerView.setAdapter(adapter);
            }
        } else {
            Toast.makeText(BrowseUsers.this, "No internet connectivity", Toast.LENGTH_LONG).show();
        }

    }

    private void requestData(String url) {
        String userID = SharedPrefsHelper.getStringPreference(Constants.PREF_KEY_USER_ID, getApplicationContext());

        RequestPackage p = new RequestPackage();
        p.setMethod(Constants.POST_METHOD);
        p.setUri(url);
        p.setParam(Constants.PREF_KEY_USER_ID, userID);

        Downloader downloader = new Downloader();
        downloader.execute(p);
    }

    private class Downloader extends AsyncTask<RequestPackage, String, String> {

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(BrowseUsers.this);
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
            List<User> userList = UserJSONParser.parseFeed(result);
            if(userList != null) {
                BrowseUserAdapter adapter = new BrowseUserAdapter(userList, BrowseUsers.this);
                recyclerView.setAdapter(adapter);
                usersViewModel.setUserlist(userList);
            } else{
                Toast.makeText(BrowseUsers.this, "No users to display", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(BrowseUsers.this, MainActivity.class);
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
                Intent browseUserIntent = new Intent(BrowseUsers.this, BrowseUsers.class);
                startActivity(browseUserIntent);
                break;
            case R.id.miNotifications:
                Intent notificationsIntent = new Intent(BrowseUsers.this, NotificationsActivity.class);
                startActivity(notificationsIntent);
                break;
            case R.id.miProfile:
                Intent profileIntent = new Intent(BrowseUsers.this, Profile.class);
                startActivity(profileIntent);
                break;
            case android.R.id.home:
                Intent homeIntent = new Intent(BrowseUsers.this, MainActivity.class);
                startActivity(homeIntent);
                break;
            case R.id.miSettings:
                Intent settingsIntent = new Intent(BrowseUsers.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
        }

        return true;
    }
}
