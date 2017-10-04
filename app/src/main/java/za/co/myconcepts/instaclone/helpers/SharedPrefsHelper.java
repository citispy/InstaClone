package za.co.myconcepts.instaclone.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import za.co.myconcepts.instaclone.Constants;

public class SharedPrefsHelper {
    private static SharedPreferences sharedPreferences;

    public static void setStringChoice(Context context, String key, String value){
        sharedPreferences = context.getSharedPreferences(Constants.PACKAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getStringPreference(String key, Context context){
        sharedPreferences = context.getSharedPreferences(Constants.PACKAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }

    public static void setIntChoice(String key, int value){
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public static int getIntPreference(String key, Context context){
        sharedPreferences = context.getSharedPreferences(Constants.PACKAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, 0);
    }

    public static String getDefaultPrefs(String key, Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(key, "");
    }
}
