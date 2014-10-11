package purplehat.fr.purplehat;

import purplehat.fr.purplehat.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    // We can be either the server or the client, so keep both instances
    private Master masterServer = null;
    private Slave masterClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        //testTheMasterMagic(false);
        //testTheMasterMagic(true);

        // testReadBroadcastedPackets();
        // testWriteBroadcastedPackets();
    }

    // Magic conversion numbers!
    public static final double INCHES_TO_MM = 25.4;

    public PhysicalScreen buildBasePhysicalScreen() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        return new PhysicalScreen(0, 0,
                (int) (INCHES_TO_MM * dm.widthPixels / dm.xdpi),
                (int) (INCHES_TO_MM * dm.heightPixels / dm.ydpi));
    }

    public void testTheMasterMagic(boolean iAmTheMaster) {
        int port = 4242;
        String serverHost = "192.168.1.241";

        // Setup the master
        if (iAmTheMaster) {
            WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            masterServer = new Master(port, info.getMacAddress(), buildBasePhysicalScreen());
            masterServer.start();
        } else {
            masterClient = new Slave(serverHost + ":" + port);
            masterClient.connect();
        }
    }

    public void testReadBroadcastedPackets() {
        // Test : read broadcasted packets
        new Thread(new Runnable() {
            @Override
            public void run() {
                Context context = getApplicationContext();
                int port = 4242;
                BroadcastService broadcastService = null;
                try {
                    broadcastService = new BroadcastService(context, port);
                } catch (IOException _) {
                    return;
                }

                // byte[] data = {42, 13, 37, 6, 66};
                while (true) {
                    try {
                        //broadcastService.send(data, data.length);
                        byte[] data = broadcastService.receive(5);
                        Log.d(data.toString(), "coucou");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void testWriteBroadcastedPackets() {
        // Test : write broadcasted packets
        new Thread(new Runnable() {
            @Override
            public void run() {
                Context context = getApplicationContext();
                int port = 4242;
                BroadcastService broadcastService = null;
                try {
                    broadcastService = new BroadcastService(context, port);
                } catch (IOException _) {
                    return;
                }

                byte[] data = {42, 13, 37, 6, 66};
                while (true) {
                    try {
                        broadcastService.send(data, data.length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (masterServer != null) {
            try {
                masterServer.stop();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (masterClient != null) {
            masterClient.close();
        }
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
