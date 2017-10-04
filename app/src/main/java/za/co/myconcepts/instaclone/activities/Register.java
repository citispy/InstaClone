package za.co.myconcepts.instaclone.activities;

import android.app.ProgressDialog;
import android.content.Intent;
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

public class Register extends AppCompatActivity {

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check Theme
        SetThemeHelper.setTheme(this);

        setContentView(R.layout.activity_register);
    }

    public void register(View view) {
        EditText etUsername = (EditText) findViewById(R.id.etUsername);
        EditText etPassowrd = (EditText) findViewById(R.id.etPassword);
        EditText etConfirmPassword = (EditText) findViewById(R.id.etPasswordConfirm);

        String username = etUsername.getText().toString();
        String password = etPassowrd.getText().toString();
        String passwordConfirm = etConfirmPassword.getText().toString();

        //Validate fields
        if(username.equals("")){
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_LONG).show();
        } else if(password.equals("")){
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_LONG).show();
        } else if(passwordConfirm.equals("")){
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_LONG).show();
        } else if(!password.equals(passwordConfirm)){
            Toast.makeText(this, "Your passwords do not match", Toast.LENGTH_LONG).show();
        } else{
            //send username and password to server
            if (ConnectionCheckHelper.isOnline(getApplicationContext())) {
                sendData(username, password, Constants.URL_PREFIX + "register.php");
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

        SubmitRegister submitRegister = new SubmitRegister();
        submitRegister.execute(p);
    }

    private class SubmitRegister extends AsyncTask<RequestPackage, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(Register.this);
            pDialog.setMessage("Completing Registration");
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
                //Registration failure
                Message message = messageList.get(0);
                Toast.makeText(Register.this, message.getMessage(), Toast.LENGTH_LONG).show();

            } else {
                //Registration successful
                result = result.replaceAll("\n", "");
                SharedPrefsHelper.setStringChoice(getApplicationContext(), Constants.PREF_KEY_USER_ID, result);
                Toast.makeText(Register.this, "You have been registered", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Register.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    }
}
