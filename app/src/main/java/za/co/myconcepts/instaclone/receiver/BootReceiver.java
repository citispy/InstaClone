package za.co.myconcepts.instaclone.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import za.co.myconcepts.instaclone.helpers.SharedPrefsHelper;
import za.co.myconcepts.instaclone.services.NotificationService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        int minutes = SharedPrefsHelper.getIntPreference("interval", context);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, NotificationService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        am.cancel(pi);
        // minutes <= 0 means notifications are disabled
        if (minutes > 0) {
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.
                    elapsedRealtime() + minutes * 60 * 1000, minutes * 60 * 1000, pi);
        }
    }
}
