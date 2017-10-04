package za.co.myconcepts.instaclone.activities;

import android.graphics.Bitmap;

public interface TaskCompleted {
    public void onTaskComplete(Bitmap result);
    public void onEncodingComplete(String result);
}
