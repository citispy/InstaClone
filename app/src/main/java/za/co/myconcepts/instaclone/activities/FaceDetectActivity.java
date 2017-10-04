package za.co.myconcepts.instaclone.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.InputStream;

import za.co.myconcepts.instaclone.R;
import za.co.myconcepts.instaclone.helpers.SetThemeHelper;

public class FaceDetectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detect);
        //Check Theme
        SetThemeHelper.setTheme(this);

        Intent intent = getIntent();
        Uri uri = Uri.parse(intent.getStringExtra("image"));

        InputStream inputStream = null;
        try {
            inputStream = this.getContentResolver().openInputStream(uri);
        } catch (Exception e) {

        }
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        processImage(bitmap);
    }

    private void processImage(Bitmap myBitmap) {
        ImageView myImageView = (ImageView) findViewById(R.id.imgview);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;

        Paint myRectPaint = new Paint();
        myRectPaint.setStrokeWidth(5);
        myRectPaint.setColor(Color.RED);
        myRectPaint.setStyle(Paint.Style.STROKE);

        Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawBitmap(myBitmap, 0, 0, null);

        FaceDetector faceDetector = new FaceDetector
                .Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .build();

        Frame frame = new Frame
                .Builder()
                .setBitmap(myBitmap)
                .build();
        SparseArray<Face> faces = faceDetector.detect(frame);

        for (int i = 0; i < faces.size(); i++) {
            Face thisFace = faces.valueAt(i);
            float x1 = thisFace.getPosition().x;
            float y1 = thisFace.getPosition().y;
            float x2 = x1 + thisFace.getWidth();
            float y2 = y1 + thisFace.getHeight();
            tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);
        }
        myImageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
    }
}
