package purplehat.fr.purplehat;


import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.io.IOException;

import purplehat.fr.purplehat.game.Ball;
import purplehat.fr.purplehat.game.World;
import purplehat.fr.purplehat.gesturelistener.OnBackgroundTouchedListener;
import purplehat.fr.purplehat.util.SystemUiHider;
import purplehat.fr.purplehat.view.DrawingView;

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
    private static final boolean TOGGLE_ON_CLICK = false;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    private DrawingView mDrawerView;
    private DrawingView.DrawerThread mDrawerThread;

    // We can be either the server or the client, so keep both instances
    private Master master = null;
    private Slave slave = null;

    // THE WORLD
    World world = new World();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        final View contentView = findViewById(R.id.fullscreen_content);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActionBar().hide();

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        /*
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

                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }

                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.

                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });
*/

        mDrawerView = (DrawingView) findViewById(R.id.fullscreen_content);
        mDrawerView.addDrawer(new DrawingView.Drawer() {
            @Override
            public void draw(Canvas canvas) {
                Paint paint = new Paint();
                paint.setColor(Color.YELLOW);
                for (Ball ball : world.getBalls()) {
                    canvas.drawCircle(ball.getPosition().getX(), ball.getPosition().getY(), ball.getRadius(), paint);
                }
            }
        });

        mDrawerView.setOnTouchListener(new OnBackgroundTouchedListener());

        //testTheMasterMagic(true);
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
            master = new Master(port, info.getMacAddress(), buildBasePhysicalScreen());
        } else {
            slave = new Slave();
            slave.connect(serverHost + ":" + port);
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
    protected void onStop() {
        super.onStop();
        if (master != null) {
            try {
                master.stop();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (slave != null) {
            slave.close();
        }
    }
}


