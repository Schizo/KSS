package com.google.android.exoplayer.demo;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.exoplayer.demo.fingerPaint.drawing;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.VideoSurfaceView;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.demo.player.DemoPlayer;
import com.google.android.exoplayer.demo.player.ExtractorRendererBuilder;
import com.google.android.exoplayer.demo.player.UnsupportedDrmException;
import com.google.android.exoplayer.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer.util.Util;
import android.view.TextureView;

import java.io.File;
import java.io.FileOutputStream;


public class MinimalPlayer extends Activity implements SurfaceHolder.Callback,
        DemoPlayer.Listener,
        AudioCapabilitiesReceiver.Listener, TextureView.SurfaceTextureListener  {

    public static final String CONTENT_TYPE_EXTRA = "content_type";
    public static final String CONTENT_ID_EXTRA = "content_id";

    private static final String TAG = "PlayerActivity";

    private static final float CAPTION_LINE_HEIGHT_RATIO = 0.0533f;
    private static final int MENU_GROUP_TRACKS = 1;
    private static final int ID_OFFSET = 2;

    private EventLogger eventLogger;
    private MediaController mediaController;
    private View shutterView;
    private VideoSurfaceView surfaceView;
    private TextView debugTextView;
    private TextView playerStateTextView;
    private DemoPlayer player;
    private boolean playerNeedsPrepare;


    private Uri contentUri;
    private int contentType;


    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    private AudioCapabilities audioCapabilities;


    private TextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;

    private Surface customSurface;// = new Surface(mTextureView.getSurfaceTexture());
    //private Renderer mRenderer;
    private Button button;
    private ImageView imageview;


    // Activity lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {






        contentUri = Uri.parse("http://192.168.1.2/kss/out.mp4");
        //contentUri = Uri.parse("http://html5demos.com/assets/dizzy.mp4");
        contentType =   DemoUtil.TYPE_MP4; //intent.getIntExtra(CONTENT_TYPE_EXTRA, -1);
        super.onCreate(savedInstanceState);



        setContentView(R.layout.player_activity);
        View root = findViewById(R.id.root);

        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    toggleControlsVisibility();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    view.performClick();
                }
                return true;
            }
        });


        root.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                    return mediaController.dispatchKeyEvent(event);
                }
                return false;
            }
        });
        audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(getApplicationContext(), this);

        shutterView = findViewById(R.id.shutter);

        //surfaceView = (VideoSurfaceView) findViewById(R.id.surface_view);
        //surfaceView.getHolder().addCallback(this);
       //mTextureView.setSurfaceTextureListener(mRenderer);


        mTextureView = (TextureView) findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(this);
       //mTextureView.getSurfaceTexture();


        debugTextView = (TextView) findViewById(R.id.debug_text_view);

        playerStateTextView = (TextView) findViewById(R.id.player_state_view);


        mediaController = new MediaController(this);
        mediaController.setAnchorView(root);

        

        DemoUtil.setDefaultCookieManager();
        addListenerOnButton();
    }

    @Override
    public void onResume() {
        super.onResume();
        //configureSubtitleView();

        // The player will be prepared on receiving audio capabilities.
        audioCapabilitiesReceiver.register();
    }


    // AudioCapabilitiesReceiver.Listener methods

    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
        boolean audioCapabilitiesChanged = !audioCapabilities.equals(this.audioCapabilities);
        if (player == null || audioCapabilitiesChanged) {
            this.audioCapabilities = audioCapabilities;
            //releasePlayer();
            preparePlayer();
        } else if (player != null) {
            player.setBackgrounded(false);
        }
    }


    public void addListenerOnButton() {

        //Select a specific button to bundle it with the action you want
        //button = (Button) findViewById(R.id.button);
        //imageview = (ImageView)findViewById(R.id.imageView);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //imageview.setImageBitmap(mTextureView.getBitmap());
                releasePlayer();
                takeScreenshot();


                Log.v("Button", "pressed");

            }

        });

    }

    private void releasePlayer() {
    if (player != null) {
      player.release();
      player = null;
      eventLogger.endSession();
      eventLogger = null;
    }
  }

    //Buttons yafes

    private void takeScreenshot(){


        String link = saveToInternalStorage( mTextureView.getBitmap());
       Intent intent = new Intent(this, drawing.class);
        intent.putExtra("link", link);
        startActivity(intent);

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

    // Internal methods

    private DemoPlayer.RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        switch (contentType) {
            case DemoUtil.TYPE_MP4:
                return new ExtractorRendererBuilder(this, userAgent, contentUri, debugTextView,
                        new Mp4Extractor());
            default:
                throw new IllegalStateException("Unsupported type: " + contentType);
        }
    }

    private void preparePlayer() {
        if (player == null) {
            player = new DemoPlayer(getRendererBuilder());
            player.addListener(this);

            playerNeedsPrepare = true;
            mediaController.setMediaPlayer(player.getPlayerControl());
            mediaController.setEnabled(true);
            eventLogger = new EventLogger();
            eventLogger.startSession();
            player.addListener(eventLogger);
            player.setInfoListener(eventLogger);
            player.setInternalErrorListener(eventLogger);
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
            //updateButtonVisibilities();
        }
        //player.setSurface(customSurface);
        //player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(true);
    }



    // DemoPlayer.Listener implementation

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
/*    if (playbackState == ExoPlayer.STATE_ENDED) {
      showControls();
    }*/
        String text = "playWhenReady=" + playWhenReady + ", playbackState=";
        switch(playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                text += "buffering";
                break;
            case ExoPlayer.STATE_ENDED:
                text += "ended";
                break;
            case ExoPlayer.STATE_IDLE:
                text += "idle";
                break;
            case ExoPlayer.STATE_PREPARING:
                text += "preparing";
                break;
            case ExoPlayer.STATE_READY:
                text += "ready";
                break;
            default:
                text += "unknown";
                break;
        }
        playerStateTextView.setText(text);
        //updateButtonVisibilities();
    }

    @Override
    public void onError(Exception e) {
        if (e instanceof UnsupportedDrmException) {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            int stringId = unsupportedDrmException.reason == UnsupportedDrmException.REASON_NO_DRM
                    ? R.string.drm_error_not_supported
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.drm_error_unsupported_scheme
                    : R.string.drm_error_unknown;
            Toast.makeText(getApplicationContext(), stringId, Toast.LENGTH_LONG).show();
        }
        playerNeedsPrepare = true;
        //updateButtonVisibilities();
        // showControls();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, float pixelWidthAspectRatio) {
        shutterView.setVisibility(View.GONE);
        surfaceView.setVideoWidthHeightRatio(
                height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);
    }



    // SurfaceHolder.Callback implementation

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (player != null) {
            //player.setSurface(holder.getSurface());
        }
    }

    private void toggleControlsVisibility()  {
        if (mediaController.isShowing()) {
            mediaController.hide();
            //debugRootView.setVisibility(View.GONE);
        } else {
            showControls();
        }
    }

    private void showControls() {
        mediaController.show(0);
        //debugRootView.setVisibility(View.VISIBLE);
    }


    //Surface Texture View Implementations
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player != null) {
            player.blockingClearSurface();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        customSurface = new Surface(mTextureView.getSurfaceTexture());
        player.setSurface(customSurface);
        Log.v("texture", "in function onSurfaceTextureAvailable");
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}