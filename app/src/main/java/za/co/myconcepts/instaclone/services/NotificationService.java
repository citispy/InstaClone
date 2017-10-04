package za.co.myconcepts.instaclone.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.List;

import za.co.myconcepts.instaclone.Constants;
import za.co.myconcepts.instaclone.R;
import za.co.myconcepts.instaclone.RequestPackage;
import za.co.myconcepts.instaclone.activities.NotificationsActivity;
import za.co.myconcepts.instaclone.connectivity.HttpManager;
import za.co.myconcepts.instaclone.helpers.SharedPrefsHelper;
import za.co.myconcepts.instaclone.model.NotificationModel;
import za.co.myconcepts.instaclone.parser.NotificationsJSONParser;

public class NotificationService extends Service {

    private PowerManager.WakeLock mWakeLock;
    private final String TAG = "handle intent";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleIntent(Intent intent) {
        // obtain the wake
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();
        // check the global background data setting
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (!cm.getBackgroundDataSetting()) {
            stopSelf();
            return;
        }

        String userID = SharedPrefsHelper.getStringPreference(Constants.PREF_KEY_USER_ID, getApplicationContext());

        RequestPackage p = new RequestPackage();
        p.setMethod(Constants.GET_METHOD);
        p.setUri(Constants.URL_PREFIX + "check_notifications.php");
        p.setParam(Constants.PREF_KEY_USER_ID, userID);

        Downloader downloader = new Downloader();
        downloader.execute(p);
    }

    private class Downloader extends AsyncTask<RequestPackage, String, String> {

        @Override
        protected String doInBackground(RequestPackage... params) {
            return HttpManager.getData(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            List<NotificationModel> notificationList = null;
            try {
                notificationList = NotificationsJSONParser.parseFeed(result);
            } catch (Exception e) {
            }
            if (notificationList != null) {
                for (NotificationModel notification : notificationList) {
                    sendNotication(notification.getUsername(), notification.getNotificationID(),
                            notification.getImage_url());

                    //Update notifications
                    RequestPackage p = new RequestPackage();
                    p.setMethod(Constants.POST_METHOD);
                    p.setUri(Constants.URL_PREFIX + "update_notification.php");
                    p.setParam("notification_id", notification.getNotificationID());

                    NotificationUpdater updater = new NotificationUpdater();
                    updater.execute(p);

                }
            }
        }
    }

    private class NotificationUpdater extends AsyncTask<RequestPackage, String, String> {

        @Override
        protected String doInBackground(RequestPackage... params) {
            return HttpManager.getData(params[0]);
        }
    }

    /* This is called on 2.0+ (API level 5 or higher). Returning
    START_NOT_STICKY tells the system to not restart the service if it is
    killed because of poor resource (memory/cpu) conditions. */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_NOT_STICKY;
    }

    /* In onDestroy() we release our wake lock. This ensures that whenever
    the Service stops (killed for resources, stopSelf() called, etc.), the
    wake lock will be released. */
    public void onDestroy() {
        super.onDestroy();
        mWakeLock.release();
    }

    private void sendNotication(String username, String notificationID, String image_url) {
        String notificationMessage;
        int notification_ID = Integer.valueOf(notificationID);
        if (image_url.equals("null")) {
            notificationMessage = username + " started following you";
        } else {
            notificationMessage = username + " liked one of you pictures";
        }
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("InstaClone")
                        .setContentText(notificationMessage)
                        .setDefaults(Notification.DEFAULT_ALL);
        Intent resultIntent = new Intent(this, NotificationsActivity.class);
        /* The stack builder object will contain an artificial back stack for
        the started Activity. This ensures that navigating backward from the
        Activity leads out of your application to the Home screen. */
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(NotificationsActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager)
                        getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(notification_ID, mBuilder.build());
    }
}
