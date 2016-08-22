package com.currie.gord.demoapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.android.colorpicker.ColorPickerDialog;
import com.android.colorpicker.ColorPickerSwatch;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom View for drawing.
 */
public class DrawView extends View {
    private Path path;
    private Paint paint;
    private int color = Color.BLACK;
    public Canvas canvas;
    private Bitmap bitmap;
    private int strokeWidth = 10;
    private MainActivity mainActivity;

    public DrawView(Context context) {
        super(context);
        if(!isInEditMode()) {
            init(context);
        }
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!isInEditMode()) {
            init(context);
        }
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(!isInEditMode()) {
            init(context);
        }
    }

    /**
     * Initialize variables
     */
    private void init(Context context) {
        Map<String, Integer> displaySize = getDisplaySize(context);

        path = new Path();
        paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setDither(true);
        bitmap = Bitmap.createBitmap(displaySize.get("width"), displaySize.get("height"), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        mainActivity = (MainActivity)context;
    }

    /**
     * displays color picker dialog
     */
    public void showColorPicker() {
        ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
        int[] colors = {Color.BLACK, Color.BLUE, Color.GREEN, Color.RED};
        colorPickerDialog.initialize(R.string.color_picker_default_title,colors, colors[0], 4, colors.length);
        colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                paint.setColor(color);
            }
        });
        colorPickerDialog.show(mainActivity.getFragmentManager(), "COLOR_PICKER");
    }

    /**
     * Returns the size of the device display
     * @param context
     * @return Map<String, Integer>
     */
    private Map<String, Integer> getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayWidth = size.x;
        int displayHeight = size.y;

        Map<String, Integer> displaySize = new HashMap<String, Integer>();
        displaySize.put("width", displayWidth);
        displaySize.put("height", displayHeight);
        return displaySize;
    }

    /**
     * Draws the path and background to canvas
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        canvas.drawPath(path, paint);
        super.onDraw(canvas);
    }

    /**
     * touch event listener to respond to user touching screen.
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        //respond to touch_down, touch_move and touch_up events
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(touchX, touchY);
                if(!mainActivity.getOrientationIsLocked()) {
                    mainActivity.lockOrientation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                path.lineTo(touchX, touchY);
                canvas.drawPath(path, paint);
                path.reset();
                break;
            default:
                return false;
        }
        //redraw
        invalidate();
        return true;
    }

    /**
     * clears drawing from screen and unlocks orientation rotation
     */
    public void clearScreen() {
        canvas.drawColor(Color.WHITE);
        invalidate();
        mainActivity.unlockOrientation();
    }

    /**
     * Create a File for saving image
     */
    private File getOutputMediaFile() {
        File mediaStorageDir = new File(mainActivity.getFilesDir(), "Demo_app");

        // Creating file
        Long timeStamp = System.currentTimeMillis()/1000;
        String timeString = timeStamp.toString();
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "demo_app" + timeString + ".jpg");

        return mediaFile;
    }

    /**
     * saves the image on canvas as a jpeg and text vile
     *
     * @return
     */
    public File savePicture() {
        setDrawingCacheEnabled(true);
        setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        File file = getOutputMediaFile();
        FileOutputStream ostream;

        Bitmap bitmap = Bitmap.createBitmap(getDrawingCache());
        destroyDrawingCache();

        try {
            // saves the bitmap
            ostream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
            ostream.flush();
            ostream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * adds passed bitmap to the canvas.
     * @param bitmap
     */
    public void addImageToCanvas(Bitmap bitmap) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();
        float ratio = Math.min((float)canvasWidth / (float)imageWidth, (float)canvasHeight/(float)imageHeight);
        int scaledWidth = Math.round(ratio * imageWidth);
        int scaledHeight = Math.round(ratio * imageHeight);
        int floatLeft = (canvasWidth - scaledWidth) / 2;
        int floatTop = (canvasHeight - scaledHeight) / 2;
        canvas.drawBitmap(Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false), floatLeft, floatTop, paint);
        invalidate();
    }
}
