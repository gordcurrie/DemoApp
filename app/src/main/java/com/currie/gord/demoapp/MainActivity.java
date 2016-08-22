package com.currie.gord.demoapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Interpolator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private DrawView drawView;
    private boolean orientationIsLocked;

    public boolean getOrientationIsLocked() {
        return orientationIsLocked;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawView = (DrawView)findViewById(R.id.draw_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                handleMenuItemClick(item.getItemId());
                return false;
            }
        });
    }

    /**
     * calls action for each Menu Item click
     * @param itemId
     */
    private void handleMenuItemClick(int itemId) {
        switch (itemId) {
            case R.id.clear:
                showDialog();
                break;
            case R.id.color:
                drawView.showColorPicker();
                break;
            case R.id.share:
                shareDrawing();
                break;
            case R.id.cat:
                new GetCatTask().execute();
                break;
        }
    }

    /**
     * creates and shows the confirmation dialog for clearing the screen
     */
    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_message)
                .setTitle(R.string.dialog_title)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        drawView.clearScreen();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Locks the orientation to whatever the current orientation is.
     */
    public void lockOrientation() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        orientationIsLocked = true;
    }

    /**
     * Unlocks screen rotation.
     */
    public void unlockOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        orientationIsLocked = false;
    }

    /**
     * Dispatches sharing intent with file created from drawview.
     */
    private void shareDrawing() {
        File file = getFile();

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(file));
        shareIntent.setType("image/png");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share image"));
    }

    /**
     * Creates a .jpeg file from the drawing in drawView.
     * @return File
     */
    private File getFile() {
        //Creates a file object and OutputStream
        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream outputStream = null;
        File file = new File(path,"android_drawing_app.png");
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (Exception e) {
            //handle exception
        }
        try {
            outputStream = new FileOutputStream(file);
        } catch (Exception e) {
            //handle exception
        }

        //Creates a bitmap from the drawView object and saves it to the OutputStream
        drawView.setDrawingCacheEnabled(true);
        drawView.invalidate();
        Bitmap drawing = drawView.getDrawingCache();
        drawing.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            //handle exception
        }

        return file;
    }

    /**
     * Private nested class to asynchronously access thecatapi.com and display the returned image.
     */
    private class GetCatTask extends AsyncTask<Object, Object, Bitmap> {
        final static private String CAT_API_URL = "http://thecatapi.com/api/images/get?format=src&type=png";
        @Override
        protected Bitmap doInBackground(Object... objects) {
            return getRandomCat();
        }

        /**
         * called after background task is complete
         * @param bitmap
         */
        protected void onPostExecute(Bitmap bitmap) {
            drawView.clearScreen();
            drawView.addImageToCanvas(bitmap);
        }

        /**
         * opens a connection to thecatapi.com and downloads and returns a random image.
         * @return Bitmap
         */
        private Bitmap getRandomCat() {
            Bitmap image = null;
            try {
                URL url = new URL(CAT_API_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                image = BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                //handle Exception
            } finally {
                return image;
            }
        }
    }
}
