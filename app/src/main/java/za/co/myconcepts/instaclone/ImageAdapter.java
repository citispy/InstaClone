package za.co.myconcepts.instaclone;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import za.co.myconcepts.instaclone.activities.ViewImageOnMap;
import za.co.myconcepts.instaclone.connectivity.ConnectionCheckHelper;
import za.co.myconcepts.instaclone.connectivity.HttpManager;
import za.co.myconcepts.instaclone.helpers.SharedPrefsHelper;
import za.co.myconcepts.instaclone.model.Image;
import za.co.myconcepts.instaclone.model.Message;
import za.co.myconcepts.instaclone.parser.MessageJSONParser;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder>{
    private List<Image> imageList;
    private Context mContext;
    private ProgressDialog pDialog;

    public ImageAdapter(List<Image> list, Context context){
        this.imageList = list;
        this.mContext = context;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.image_cardview, parent, false);
        return new ImageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        final Image image = imageList.get(position);
        holder.tvTitle.setText(image.getUsername() + " - " + image.getCityName() );
        Picasso.with(mContext).load(image.getImage_url())
                .placeholder(R.color.cardview_dark_background)
                .resize(600,600)
                .centerInside()
                .into(holder.imageView);
        holder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Checking for network connection and request data if available
                if (ConnectionCheckHelper.isOnline(mContext)) {
                    requestData(image.getImage_id(), image.getUser_id(), Constants.URL_PREFIX + "like_image.php");
                } else {
                    Toast.makeText(mContext, "No internet connectivity", Toast.LENGTH_LONG).show();
                }
            }
        });

        holder.btnViewMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ViewImageOnMap.class);
                Bundle bundle = new Bundle();
                bundle.putString("image_url", image.getImage_url());
                bundle.putString("latitude", image.getLatitude());
                bundle.putString("longitude", image.getLongitude());
                intent.putExtra("bundle", bundle);
                mContext.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder{
        private TextView tvTitle;
        private ImageView imageView;
        private Button btnLike, btnViewMap;

        public ImageViewHolder(View v){
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            imageView = v.findViewById(R.id.ivImage);
            btnLike = v.findViewById(R.id.btnLike);
            btnViewMap = v.findViewById(R.id.btnViewMap);
        }
    }

    private void requestData(String image_id, String targetUserID, String url) {
        String userID = SharedPrefsHelper.getStringPreference(Constants.PREF_KEY_USER_ID, mContext);

        RequestPackage p = new RequestPackage();
        p.setMethod(Constants.POST_METHOD);
        p.setUri(url);
        p.setParam(Constants.PREF_KEY_USER_ID, userID);
        p.setParam("image_id", image_id);
        p.setParam("target_user_id", targetUserID);

        SubmitLike submitter = new SubmitLike();
        submitter.execute(p);
    }

    private class SubmitLike extends AsyncTask<RequestPackage, String, String> {

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(mContext);
            pDialog.setMessage("Liking image");
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
            } else {
                Toast.makeText(mContext, "A network error has occurred", Toast.LENGTH_LONG).show();
            }
        }
    }

}
