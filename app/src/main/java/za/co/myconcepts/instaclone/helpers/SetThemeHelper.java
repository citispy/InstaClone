package za.co.myconcepts.instaclone.helpers;

import android.content.Context;

import za.co.myconcepts.instaclone.R;

public class SetThemeHelper {

    public static void setTheme(Context context){
        if(SharedPrefsHelper.getDefaultPrefs("pref_theme", context.getApplicationContext()).equals("Blue")){
            context.setTheme(R.style.BlueTheme);
        }
    }
}
