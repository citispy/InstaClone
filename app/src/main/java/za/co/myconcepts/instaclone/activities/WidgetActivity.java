package za.co.myconcepts.instaclone.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import za.co.myconcepts.instaclone.Constants;
import za.co.myconcepts.instaclone.R;
import za.co.myconcepts.instaclone.RequestPackage;
import za.co.myconcepts.instaclone.connectivity.HttpManager;
import za.co.myconcepts.instaclone.helpers.SharedPrefsHelper;
import za.co.myconcepts.instaclone.model.NotificationModel;
import za.co.myconcepts.instaclone.parser.NotificationsJSONParser;

public class WidgetActivity extends AppWidgetProvider {

    List<NotificationModel> notificationList = null;
    RemoteViews views;
    PendingIntent pendingIntent;
    public static String CLOCK_WIDGET_UPDATE = "za.co.myconcepts.instaclone.TIME_WIDGET_UPDATE" ;
    private static final String LOG_TAG = "ExampleWidget" ;

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        Log.i(LOG_TAG, "Updating widgets");

        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, NotificationsActivity.class);
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setOnClickPendingIntent(R.id.button, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app
            // widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

        }
    }

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        requestData(context, appWidgetManager, appWidgetId);
    }

    public void requestData(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        String userID = SharedPrefsHelper.getStringPreference(Constants.PREF_KEY_USER_ID, context);

        RequestPackage p = new RequestPackage();
        p.setMethod(Constants.GET_METHOD);
        p.setUri(Constants.URL_PREFIX + "latest_notification.php");
        p.setParam(Constants.PREF_KEY_USER_ID, userID);

        Downloader downloader = new Downloader(appWidgetManager, appWidgetId, context);
        downloader.execute(p);
    }
    private class Downloader extends AsyncTask<RequestPackage, String, String> {
        AppWidgetManager appWidgetManager;
        int appWidgetId;
        Context mContext;

        public Downloader(AppWidgetManager appWidgetManager, int appWidgetId, Context context){
            this.appWidgetManager = appWidgetManager;
            this.appWidgetId = appWidgetId;
            this.mContext = context;
        }
        @Override
        protected String doInBackground(RequestPackage... params) {
            return HttpManager.getData(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                notificationList = NotificationsJSONParser.parseFeed(result);
            } catch (Exception e) {
            }
            RemoteViews updatedViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_layout);
            if(notificationList != null){
                NotificationModel notification = notificationList.get(0);

                String message;
                if(notification.getImage_url().equals("null")){
                    message = notification.getUsername() + " started following you";
                } else {
                    message = notification.getUsername() + " liked one of your pictures";
                }

                updatedViews.setTextViewText(R.id.widget1label, message);
                appWidgetManager.updateAppWidget(appWidgetId, updatedViews);
            } else{
                updatedViews.setTextViewText(R.id.widget1label, "You have no notifications to display");
                appWidgetManager.updateAppWidget(appWidgetId, updatedViews);
            }
        }
    }
    private PendingIntent createClockTickIntent(Context context) {
        Intent intent = new Intent(CLOCK_WIDGET_UPDATE);
        PendingIntent pendingIntent = PendingIntent. getBroadcast (context, 0 , intent, PendingIntent. FLAG_UPDATE_CURRENT );
        return pendingIntent;
    }
    @Override
    public void onDisabled(Context context) {
        super .onDisabled(context);
        Log. d (LOG_TAG , "Widget Provider disabled. Turning off timer" );
        AlarmManager alarmManager =
                (AlarmManager)context.getSystemService(Context. ALARM_SERVICE );
        alarmManager.cancel(createClockTickIntent(context));
    }
    @Override
    public void onEnabled(Context context) {
        super .onEnabled(context);
        Log. d (LOG_TAG , "Widget Provider enabled. Starting timer to update widget every second" );
                AlarmManager alarmManager =
                        (AlarmManager)context.getSystemService(Context. ALARM_SERVICE );
        Calendar calendar = Calendar. getInstance ();
        calendar.setTimeInMillis(System. currentTimeMillis ());
        calendar.add(Calendar. SECOND , 1 );
        alarmManager.setRepeating(AlarmManager. RTC , calendar.getTimeInMillis(),
                1000 , createClockTickIntent(context));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super .onReceive(context, intent);
        if (CLOCK_WIDGET_UPDATE .equals(intent.getAction())) {
            // Get the widget manager and ids for this widget provider, then call the shared
            // clock update method.
            ComponentName thisAppWidget = new
                    ComponentName(context.getPackageName(), getClass().getName());
            AppWidgetManager appWidgetManager =
                    AppWidgetManager.getInstance(context);
            int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
            for ( int appWidgetID: ids) {
                updateAppWidget(context, appWidgetManager, appWidgetID);
            }
        }
    }
}

