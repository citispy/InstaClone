package za.co.myconcepts.instaclone.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import za.co.myconcepts.instaclone.model.NotificationModel;

public class NotificationsJSONParser {
    public static List<NotificationModel> parseFeed(String content){

        try {
            JSONArray ar = new JSONArray(content);
            List<NotificationModel> notificationList = new ArrayList<>();

            for (int i = 0; i < ar.length(); i++) {
                JSONObject obj = ar.getJSONObject(i);
                NotificationModel notification = new NotificationModel();

                //Parsing ID's
                notification.setUsername(obj.getString("username"));
                notification.setImage_url(obj.getString("image_url"));
                notification.setNotificationID(obj.getString("notification_id"));
                notificationList.add(notification);

            }

            return notificationList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }
}
