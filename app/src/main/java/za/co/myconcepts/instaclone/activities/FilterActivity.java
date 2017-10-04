/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.myconcepts.instaclone.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import za.co.myconcepts.instaclone.R;
import za.co.myconcepts.instaclone.filtering.FastStyleModelTiled;
import za.co.myconcepts.instaclone.helpers.SetThemeHelper;

public class FilterActivity extends AppCompatActivity {
    private static final String TAG = "RS NeuralNet";

    private static int IMG_SIZE = 256;

    private final int NUM_BITMAPS = 3;
    RenderScriptTask currentTask = null;
    private int mCurrentBitmap = 0;
    private Bitmap mBitmapIn;
    private Bitmap mBitmapInOriginal;
    private Bitmap[] mBitmapsOut;
    private ImageView mImageView;
    private Bitmap processedBitmap;

    // TODO Bonus: Replace FastStyleModel with FastStyleModelTiled and see the perf diff.
    private FastStyleModelTiled mFSNN;
    private boolean IMG_LOADED = false;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    class ImAdapter extends ArrayAdapter<Drawable> {
        ArrayList<Drawable> items;
        ImageView image;

        ImAdapter(Context c, int resources, ArrayList<Drawable> list) {
            super(c, resources, list);
            this.items = list;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.images, null);
            }
            image = (ImageView) convertView.findViewById(R.id.imagex);
            image.setBackground(items.get(position));
            return convertView;

        }
    }


    private AlertDialog getModelDialog() {
        ArrayList<Drawable> items = new ArrayList<>();
        items.add(ContextCompat.getDrawable(this, R.drawable.composition));
        items.add(ContextCompat.getDrawable(this, R.drawable.seurat));
        items.add(ContextCompat.getDrawable(this, R.drawable.candy));
        items.add(ContextCompat.getDrawable(this, R.drawable.kanagawa));
        items.add(ContextCompat.getDrawable(this, R.drawable.starry));
        items.add(ContextCompat.getDrawable(this, R.drawable.fur));
        ImAdapter adapter = new ImAdapter(this, 0, items);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select Model");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String modelName = null;
                switch (item) {
                    case 0:
                        modelName = "composition";
                        break;
                    case 1:
                        modelName = "seurat";
                        break;
                    case 2:
                        modelName = "candy";
                        break;
                    case 3:
                        modelName = "kanagawa";
                        break;
                    case 4:
                        modelName = "starrynight";
                        break;
                    case 5:
                        modelName = "fur";
                        break;
                    default:
                        modelName = "composition";
                        break;
                }
                if (mFSNN.mModel == null || !mFSNN.mModel.equals(modelName)) {
                    try {
                        long time = System.currentTimeMillis();
                        mFSNN.loadModel(modelName);
                        mFSNN.mModel = modelName;
                        time = System.currentTimeMillis() - time;
                        Log.v(TAG, "loaded model, using time: " + time);
                    } catch (IOException e) {

                    }
                }
                dialog.cancel();
            }
        });

        return builder.create();
    }

    private AlertDialog getSizeDialog() {
        final String[] items = new String[]{"Low Resolution", "High Resolution"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, items);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select Resolution");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    IMG_SIZE = 256;
                    FastStyleModelTiled.MAX_CHUNK_SIZE = 256;
                    FastStyleModelTiled.MAX_IMG_SIZE = 256;
                } else {
                    IMG_SIZE = 512;
                    FastStyleModelTiled.MAX_CHUNK_SIZE = 512;
                    FastStyleModelTiled.MAX_IMG_SIZE = 512;
                }
                dialog.cancel();
            }
        });

        return builder.create();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check Theme
        SetThemeHelper.setTheme(this);

        setContentView(R.layout.filter_activity_layout);

        //Retrieving image from ChoosImage.java
        Intent intent = getIntent();
        Uri uri = Uri.parse(intent.getStringExtra("image"));

        mImageView = (ImageView) findViewById(R.id.imageView);
        // Hide the progress bar.
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        final AlertDialog dialogModel = getModelDialog();
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogModel.show();
            }
        });

        final AlertDialog dialogSize = getSizeDialog();
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSize.show();
            }
        });

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                * Start processing the Neural Net.
                */
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                NeuralNetTask nnT = new NeuralNetTask();
                nnT.execute(getApplicationContext());
            }
        });

        SeekBar seekbar = (SeekBar) findViewById(R.id.seekBar1);
        seekbar.setProgress(0);
        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (IMG_LOADED) {
                    updateImage((float) progress / 100.0f);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        CreateModelTask createFSNN = new CreateModelTask();
        createFSNN.execute(this);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ActivityCompat.checkSelfPermission(FilterActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(FilterActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
                } else {
                    saveProcessedImage();
                }
            }
        });

        loadBitmap(uri);
    }

    /*
      Invoke AsynchTask and cancel previous task.
      When AsyncTasks are piled up (typically in slow device with heavy kernel),
      Only the latest (and already started) task invokes RenderScript operation.
     */
    private void updateImage(final float f) {
        if (currentTask != null)
            currentTask.cancel(false);
        currentTask = new RenderScriptTask();
        currentTask.execute(f);
    }

    private class NeuralNetTask extends AsyncTask<Context, Integer, Integer> {
        private void tryFSNN() {
            int height = mBitmapIn.getHeight();
            int width = mBitmapIn.getWidth();

            float xyRatio = (float) width / height;
            if (xyRatio > 1) {
                height = IMG_SIZE;
                width = (int) (IMG_SIZE * xyRatio);
            } else {
                width = IMG_SIZE;
                height = (int) (IMG_SIZE / xyRatio);
            }

            Bitmap testBitmap = Bitmap.createScaledBitmap(mBitmapIn, width, height, false);
            mBitmapInOriginal = Bitmap.createBitmap(testBitmap, (width - IMG_SIZE) / 2, (height - IMG_SIZE) / 2, IMG_SIZE, IMG_SIZE);

            if (mFSNN.mModel == null) {
                try {
                    mFSNN.loadModel();
                } catch (IOException e) {
                    Log.v("WTF ", e.toString());
                }
            }

            long time = System.currentTimeMillis();
            mBitmapIn = mFSNN.processImage(mBitmapInOriginal);
            time = System.currentTimeMillis() - time;
            Log.v(TAG, "processed model, using time: " + time);
            updateImage(0.0f);
        }

        protected Integer doInBackground(Context... ctx) {
            tryFSNN();
            return 1;
        }

        void updateView(Integer result) {
            if (result != -1) {
                // Hide the progress bar.
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            }
        }

        protected void onPostExecute(Integer result) {
            updateView(result);
        }
    }

    private class CreateModelTask extends AsyncTask<Context, Integer, Integer> {
        protected Integer doInBackground(Context... ctx) {
            mFSNN = new FastStyleModelTiled(ctx[0]);
            return 1;
        }
    }

    /*
     * In the AsyncTask, it invokes RenderScript intrinsics to do a filtering.
     * After the filtering is done, an operation blocks at Allication.copyTo() in AsyncTask thread.
     * Once all operation is finished at onPostExecute() in UI thread, it can invalidate and update ImageView UI.
     */
    private class RenderScriptTask extends AsyncTask<Float, Integer, Integer> {
        Boolean issued = false;

        // Just toy code to blend the image before and after processing.
        private Bitmap blend(Bitmap base, Bitmap blend, int opacity) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap result = null;
            result = base.copy(Bitmap.Config.ARGB_8888, true);
            Bitmap transBitmap = Bitmap.createBitmap(blend.getWidth(), blend.getHeight(), Bitmap.Config.ARGB_8888);
            Paint p = new Paint();
            p.setAlpha(opacity);
            Canvas c = new Canvas(transBitmap);
            c.drawARGB(0, 0, 0, 0);
            c.setBitmap(result);
            c.drawBitmap(blend, 0, 0, p);
            return result;
        }


        protected Integer doInBackground(Float... values) {
            int index = -1;
            if (!isCancelled()) {
                issued = true;
                index = mCurrentBitmap;
                mBitmapsOut[index] = blend(mBitmapIn, mBitmapInOriginal, (int) (values[0] * 255));
                mCurrentBitmap = (mCurrentBitmap + 1) % NUM_BITMAPS;
            }
            return index;
        }

        void updateView(Integer result) {
            if (result != -1) {
                // Request UI update
                processedBitmap = mBitmapsOut[result];
                mImageView.setImageBitmap(processedBitmap);
                mImageView.invalidate();
            }
        }

        protected void onPostExecute(Integer result) {
            updateView(result);
        }

        protected void onCancelled(Integer result) {
            if (issued) {
                updateView(result);
            }
        }
    }

    public void loadBitmap(Uri uri) {
        Bitmap bitmap = null;

        Uri mImageCaptureUri = uri;
        InputStream inputStream = null;
        try {
            inputStream = this.getContentResolver().openInputStream(mImageCaptureUri);
        } catch (Exception e) {

        }
        bitmap = BitmapFactory.decodeStream(inputStream);

        /*
         * Initialize UI
         */
        if (bitmap != null) {
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            if (width > height) {
                width = height;
            } else {
                height = width;
            }
            mBitmapIn = Bitmap.createBitmap(bitmap,
                    (bitmap.getWidth() - width) / 2,
                    (bitmap.getHeight() - height) / 2,
                    width,
                    height);
        }

        mBitmapInOriginal = mBitmapIn.copy(mBitmapIn.getConfig(), true);
        IMG_LOADED = true;

        mBitmapsOut = new Bitmap[NUM_BITMAPS];
        for (int i = 0; i < NUM_BITMAPS; ++i) {
            mBitmapsOut[i] = Bitmap.createBitmap(mBitmapIn.getWidth(),
                    mBitmapIn.getHeight(), mBitmapIn.getConfig());
        }

        processedBitmap = mBitmapsOut[mCurrentBitmap];
        mImageView.setImageBitmap(processedBitmap);
        mCurrentBitmap = (mCurrentBitmap + 1) % NUM_BITMAPS;
        IMG_LOADED = true;

        updateImage(0.0f);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 200: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveProcessedImage();
                }
            }
        }
    }

    private void saveProcessedImage() {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        processedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), processedBitmap, "Title", null);
        Uri tempImgUri = Uri.parse(path);

        Intent intent = new Intent(FilterActivity.this, ChooseImage.class);
        intent.putExtra("image", tempImgUri.toString());
        startActivity(intent);
        finish();
    }


}
