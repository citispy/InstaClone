package za.co.myconcepts.instaclone;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import za.co.myconcepts.instaclone.activities.MainActivity;
import za.co.myconcepts.instaclone.connectivity.ConnectionCheckHelper;
import za.co.myconcepts.instaclone.connectivity.HttpManager;
import za.co.myconcepts.instaclone.helpers.SharedPrefsHelper;
import za.co.myconcepts.instaclone.model.Message;
import za.co.myconcepts.instaclone.model.User;
import za.co.myconcepts.instaclone.parser.MessageJSONParser;

public class BrowseUserAdapter extends RecyclerView.Adapter<BrowseUserAdapter.BrowseUserViewHolder>{
    private List<User> userList;
    private Context mContext;
    private ProgressDialog pDialog;

    public BrowseUserAdapter(List<User> list, Context context){
        this.userList = list;
        this.mContext = context;
    }

    @Override
    public BrowseUserAdapter.BrowseUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.users_cardview, parent, false);
        return new BrowseUserAdapter.BrowseUserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(BrowseUserAdapter.BrowseUserViewHolder holder, int position) {
        final User user = userList.get(position);
        holder.tvUsername.setText(user.getUsername());

        holder.btnViewImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("intentUserID", user.getUserID());
                mContext.startActivity(intent);
            }
        });

        holder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Checking for network connection and request data if available
                if (ConnectionCheckHelper.isOnline(mContext)) {
                    requestData(user.getUserID(), Constants.URL_PREFIX + "follow_user.php");
                } else {
                    Toast.makeText(mContext, "No internet connectivity", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void requestData(String followedUserID, String url) {
        String userID = SharedPrefsHelper.getStringPreference(Constants.PREF_KEY_USER_ID, mContext);

        RequestPackage p = new RequestPackage();
        p.setMethod(Constants.POST_METHOD);
        p.setUri(url);
        p.setParam("user_id_following", followedUserID);
        p.setParam(Constants.PREF_KEY_USER_ID, userID);

        FollowingSubmitter submitter = new FollowingSubmitter();
        submitter.execute(p);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class BrowseUserViewHolder extends RecyclerView.ViewHolder{
        private TextView tvUsername;
        private Button btnViewImages, btnFollow;

        public BrowseUserViewHolder(View v){
            super(v);
            tvUsername = v.findViewById(R.id.tvTitle);
            btnViewImages = v.findViewById(R.id.btnViewImages);
            btnFollow = v.findViewById(R.id.btnFollow);
        }
    }

    private class FollowingSubmitter extends AsyncTask<RequestPackage, String, String> {

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(mContext);
            pDialog.setMessage("Following User...");
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
            List<Message> messageList = MessageJSONParser.parseFeed(result);
            if(messageList != null) {
                Message message = messageList.get(0);
                Toast.makeText(mContext, message.getMessage(), Toast.LENGTH_LONG).show();
            } else{
                Toast.makeText(mContext, "A network error has occurred", Toast.LENGTH_LONG).show();
            }
        }
    }
}
