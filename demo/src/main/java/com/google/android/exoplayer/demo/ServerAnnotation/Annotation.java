package com.google.android.exoplayer.demo.ServerAnnotation;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.google.android.exoplayer.demo.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.TimerTask;

/**
 * Created by chavez on 7/19/15.
 */
public class Annotation {

    ImageLoader imageLoader;
    DisplayImageOptions options;
    Activity callerActivity;
    Context ctx;


    public Annotation(Context ctx, Activity activity) {
        callerActivity = activity;
        this.ctx = ctx;


        //Get our  View References
        final ImageView iconImg = (ImageView) callerActivity.findViewById(R.id.iconImg);

        //Setup the ImageLoader, we'll use this to display our images
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(ctx).build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);

        //Setup options for ImageLoader so it will handle caching for us.
        options = new DisplayImageOptions.Builder()
                .cacheInMemory()
                .cacheOnDisc()
                .build();


        //iconImg.setImageBitmap(bm);

        new DownloadFilesTask().execute("");

    }

    private class DownloadFilesTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap bitmap = null;
            //try {
               /* bitmap = BitmapFactory.decodeStream((InputStream)new URL(urls[0]).getContent());*/
            bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.logo_bright);
         /*   } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            //}
            return bitmap;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView i = (ImageView)callerActivity.findViewById(R.id.iconImg);
            i.setImageBitmap(bitmap);
        }
    }

}
