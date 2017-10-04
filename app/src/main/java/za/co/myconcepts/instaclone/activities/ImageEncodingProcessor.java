package za.co.myconcepts.instaclone.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

class EncodingTask extends AsyncTask<Bitmap, Void, String> {
    private Context mContext;
    private TaskCompleted mCallback;


    public EncodingTask(Context context) {
        this.mContext = context;
        this.mCallback = (TaskCompleted) context;
    }

    // Decode image in background.
    @Override
    protected String doInBackground(Bitmap... params) {

        String imageEncoded = encodeImage(params[0]);
        return imageEncoded;
    }

    @Override
    protected void onPostExecute(String result) {

        mCallback.onEncodingComplete(result);

    }

    //Encoding image
    private static String encodeImage(Bitmap thumbnail) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.URL_SAFE);
        return imageEncoded;
    }
}
