package za.co.myconcepts.instaclone.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import za.co.myconcepts.instaclone.Constants;
import za.co.myconcepts.instaclone.R;
import za.co.myconcepts.instaclone.RequestPackage;
import za.co.myconcepts.instaclone.connectivity.ConnectionCheckHelper;
import za.co.myconcepts.instaclone.connectivity.HttpManager;
import za.co.myconcepts.instaclone.helpers.SetThemeHelper;
import za.co.myconcepts.instaclone.helpers.SharedPrefsHelper;
import za.co.myconcepts.instaclone.model.Message;
import za.co.myconcepts.instaclone.parser.MessageJSONParser;

public class Login extends AppCompatActivity {
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check Theme
        SetThemeHelper.setTheme(this);

        setContentView(R.layout.activity_login);
    }

    public void register(View view) {
        Intent intent = new Intent(Login.this, Register.class);
        startActivity(intent);
    }

    public void login(View view) {
        EditText etUsername = (EditText) findViewById(R.id.etUsername);
        EditText etPassword = (EditText) findViewById(R.id.etPassword);
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        if(username.equals("")){
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
        } else if(password.equals("")){
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
        } else {
            if (ConnectionCheckHelper.isOnline(getApplicationContext())) {
                sendData(username, password, "https://myconcepts.000webhostapp.com/instaclone/login.php");
            } else{
                Toast.makeText(this, "Not connected to the internet", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendData(String username, String password, String url) {
        RequestPackage p = new RequestPackage();
        p.setMethod(Constants.POST_METHOD);
        p.setUri(url);
        p.setParam(Constants.KEY_USERNAME, username);
        p.setParam(Constants.KEY_PASSWORD, password);

        SubmitLogin submitLogin = new SubmitLogin();
        submitLogin.execute(p);
    }

    private class SubmitLogin extends AsyncTask<RequestPackage, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(Login.this);
            pDialog.setMessage("Logging in");
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
            if (messageList != null){
                //Login error
                Message message = messageList.get(0);
                Toast.makeText(Login.this, message.getMessage(), Toast.LENGTH_LONG).show();
            } else {
                //Login successful
                result = result.replaceAll("\n", "");
                SharedPrefsHelper.setStringChoice(getApplicationContext(), Constants.PREF_KEY_USER_ID, result);
                Toast.makeText(Login.this, "You have been logged in", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Login.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    }
}
