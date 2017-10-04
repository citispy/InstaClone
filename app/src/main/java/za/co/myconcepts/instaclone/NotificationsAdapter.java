package za.co.myconcepts.instaclone;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import za.co.myconcepts.instaclone.model.NotificationModel;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationsViewHolder>{
    private List<NotificationModel> notificationList;
    private Context mContext;

    public NotificationsAdapter(List<NotificationModel> list, Context context){
        this.notificationList = list;
        this.mContext = context;
    }

    @Override
    public NotificationsAdapter.NotificationsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.notifications_cardview, parent, false);
        return new NotificationsAdapter.NotificationsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NotificationsAdapter.NotificationsViewHolder holder, int position) {
        NotificationModel notification = notificationList.get(position);
        String notificationMessage;
        if(notification.getImage_url().equals("null")){
            holder.ivImage.setVisibility(View.GONE);
            notificationMessage = notification.getUsername() + " started following you";
            holder.tvNotification.setText(notificationMessage);
        } else {
            holder.ivImage.setVisibility(View.VISIBLE);
            notificationMessage = notification.getUsername() + " liked one of your pictures";
            holder.tvNotification.setText(notificationMessage);
            Picasso.with(mContext).load(notification.getImage_url())
                    .placeholder(R.color.cardview_dark_background)
                    .resize(200,200)
                    .centerCrop()
                    .into(holder.ivImage);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationsViewHolder extends RecyclerView.ViewHolder{
        private TextView tvNotification;
        private ImageView ivImage;

        public NotificationsViewHolder(View v){
            super(v);
            tvNotification = v.findViewById(R.id.tvNotification);
            ivImage = v.findViewById(R.id.ivImage);
        }
    }
}

