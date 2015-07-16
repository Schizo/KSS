package com.google.android.exoplayer.demo.fingerPaint;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.google.android.exoplayer.demo.R;
import com.google.android.exoplayer.demo.ServerAnnotation.UploadActivity;
import com.google.android.exoplayer.demo.player.DemoPlayer;
import com.google.android.exoplayer.util.VerboseLogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class drawing extends GraphicsActivity
        implements ColorPickerDialog.OnColorChangedListener {

    private Bitmap bgImage;
    public Bitmap mBitmap;
    public Bitmap bitmap;
    public String filename;
    private static final int MENU_GROUP_TRACKS = 1;
    private static final int ID_OFFSET = 2;





    private Bitmap loadImageFromStorage(String path)
    {

        try {

            File f=new File(path);
            Log.d("abspath", f.getAbsoluteFile().toString());
           bgImage = BitmapFactory.decodeStream(new FileInputStream(f));


        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            Log.d("failed", "notfound");
        }

        return bgImage;


    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        //File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(getCacheDir(),"profile.jpg");
        Log.d("abspath", mypath.getAbsoluteFile().toString());

        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(mypath);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mypath.getAbsolutePath();
    }

    private void launchUploadActivity(){
        Intent i = new Intent(this, UploadActivity.class);
        String linkOnDevice = saveToInternalStorage(mBitmap);
        boolean isImage = true;
        i.putExtra("filePath", linkOnDevice);
        i.putExtra("isImage", isImage);
        i.putExtra("filename", filename);
        startActivity(i);

    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {



        Log.d("drawingABC", "loading image failed");
        super.onCreate(savedInstanceState);

        final View myview = new MyView(this, bitmap );
        setContentView(myview);


        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 },
                0.4f, 6, 3.5f);

        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);


        Bundle extras = getIntent().getExtras();
        String linkOnDisc  = extras.getString("link");
        filename  = extras.getString("filename");

        loadImageFromStorage(linkOnDisc);



    }

    private Paint       mPaint;
    private MaskFilter mEmboss;
    private MaskFilter  mBlur;

    public void colorChanged(int color) {
        mPaint.setColor(color);
    }




    //========================================================
    //
    //Inner Class
    //
    //========================================================
    public class MyView extends View {

        private static final float MINP = 0.25f;
        private static final float MAXP = 0.75f;

        private Canvas mCanvas;
        private Path mPath;
        private Paint   mBitmapPaint;



        public MyView(Context c, Bitmap bitmap) {
            super(c);

            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            mBitmap = bitmap;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFAAAAAA);

            canvas.drawBitmap(bgImage, 0, 0, mBitmapPaint);
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

            canvas.drawPath(mPath, mPaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }
        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;
            }
        }
        private void touch_up() {
            mPath.lineTo(mX, mY);
            // commit the path to our offscreen
            mCanvas.drawPath(mPath, mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }



    private static final int COLOR_MENU_ID = Menu.FIRST;
    private static final int EMBOSS_MENU_ID = Menu.FIRST + 1;
    private static final int BLUR_MENU_ID = Menu.FIRST + 2;
    private static final int ERASE_MENU_ID = Menu.FIRST + 3;
    private static final int SRCATOP_MENU_ID = Menu.FIRST + 4;
    private static final int SEND_IMAGE_ID = Menu.FIRST + 5;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c');
        menu.add(0, EMBOSS_MENU_ID, 0, "Emboss").setShortcut('4', 's');
        menu.add(0, BLUR_MENU_ID, 0, "Blur").setShortcut('5', 'z');
        menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z');
        menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop").setShortcut('5', 'z');
        menu.add(0, SEND_IMAGE_ID, 0, "Send Annotation").setShortcut('6', 'z');

        /****   Is this the mechanism to extend with filter effects?
         Intent intent = new Intent(null, getIntent().getData());
         intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
         menu.addIntentOptions(
         Menu.ALTERNATIVE, 0,
         new ComponentName(this, NotesList.class),
         null, intent, 0, null);
         *****/

        //todo: how to activate menu??
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);

        switch (item.getItemId()) {
            case COLOR_MENU_ID:
                new ColorPickerDialog(this, this, mPaint.getColor()).show();
                return true;
            case EMBOSS_MENU_ID:
                if (mPaint.getMaskFilter() != mEmboss) {
                    mPaint.setMaskFilter(mEmboss);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case BLUR_MENU_ID:
                if (mPaint.getMaskFilter() != mBlur) {
                    mPaint.setMaskFilter(mBlur);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case ERASE_MENU_ID:
                mPaint.setXfermode(new PorterDuffXfermode(
                        PorterDuff.Mode.CLEAR));
                return true;
            case SRCATOP_MENU_ID:
                mPaint.setXfermode(new PorterDuffXfermode(
                        PorterDuff.Mode.SRC_ATOP));
                mPaint.setAlpha(0x80);
                return true;
            case SEND_IMAGE_ID:
                launchUploadActivity();
        }
        return super.onOptionsItemSelected(item);
    }




}


/*
public class drawing extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_drawing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
*/