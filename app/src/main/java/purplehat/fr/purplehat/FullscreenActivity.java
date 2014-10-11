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

import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;

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

    private Slave slave;

    public Master getMaster() {
        return master;
    }

    private Master master;

    // THE WORLD
    World world = new World();
    private static FullscreenActivity instance = null;

    public static FullscreenActivity getInstance() {
        return instance;
    }

    DiscoveryService discoveryService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;
        try {
            discoveryService = new DiscoveryService(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }

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
//        mDrawerView.addDrawer(new DrawingView.Drawer() {
//            @Override
//            public void draw(Canvas canvas) {
//                Paint paint = new Paint();
//                paint.setColor(Color.YELLOW);
//                for (Ball ball : world.getBalls()) {
//                    canvas.drawCircle(ball.getPosition().getX(), ball.getPosition().getY(), ball.getRadius(), paint);
//                }
//            }
//        });

        mDrawerView.setOnTouchListener(new OnBackgroundTouchedListener());

        testTimer();
        testRect();

        //testTheMasterMagic(true);

        // testReadBroadcastedPackets();
        // testDiscoveryAskConnexion();
        // testDiscoveryWaitConnexion();

        new Thread(new ConnexionListener()).start();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //onExitingSwipeEvent(42, 42);
        onEntrantSwipeEvent(42, 42);
    }

    public void becomeASlave(byte[] masterAddress) {
        Log.d("TG", "become slave biatch");
        slave = new Slave();
        slave.addListener("views changed", new Slave.Listener() {
            @Override
            public void notify(JSONObject data) {
                Log.d("ACTIVITY", "views changed" + data);
                world.updateFromJson(data);
            }
        });
        slave.connect(masterAddress, MasterProxy.MASTER_PROXY_PORT_DE_OUF);
    }


    public void becomeAMaster() {
        Log.d("TG", "become master biatch");
        master = new Master(MasterProxy.MASTER_PROXY_PORT_DE_OUF, "424242", null);
        master.start();
    }

    public void onExitingSwipeEvent(final int swipeX, final int swipeY) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InetAddress masterAddress = null;
                if (master != null) {
                    try {
                        masterAddress = discoveryService.getLocalIp();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
                if (slave != null) {
                    try {
                        masterAddress = InetAddress.getByAddress(slave.getMasterAddress());
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    discoveryService.waitConnexion(masterAddress, swipeX, swipeY);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void onEntrantSwipeEvent(final int swipeX, final int swipeY) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (slave == null && master == null) {
                    try {
                        discoveryService.askConnexion(swipeX, swipeY);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void testDiscoveryWaitConnexion() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Context context = getApplicationContext();
                DiscoveryService discoveryService = null;
                try {
                    discoveryService = new DiscoveryService(context);
                } catch (IOException e) {
                    return;
                }

                InetAddress masterAddress = null;
                try {
                    discoveryService.waitConnexion(masterAddress, 42, 1337);
                } catch (IOException e) {
                    return;
                }
            }
        }).start();
    }

    public void testDiscoveryAskConnexion() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Context context = getApplicationContext();
                DiscoveryService discoveryService = null;
                try {
                    discoveryService = new DiscoveryService(context);
                } catch (IOException e) {
                    return;
                }

                try {
                    discoveryService.askConnexion(42, 1337);
                } catch (IOException e) {
                    return;
                }
            }
        }).start();
    }

    public void testTheMasterMagic(boolean iAmTheMaster) {
        int port = 4242;
        String serverHost = "192.168.1.241";

        // Setup the master
        if (iAmTheMaster) {
            WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            Master master = new Master(port, info.getMacAddress(), ScreenUtilitiesService.buildBasePhysicalScreen());
        } else {
            Slave slave = new Slave();

            slave.addListener("views changes", new Slave.Listener() {
                @Override
                public void notify(JSONObject data) {
                    Log.d("ACTIVITY", "views changed" + data);
                    world.updateFromJson(data);
                }
            });

            // slave.connect(serverHost, port);
        }
    }



    public void testReadBroadcastedPackets() {
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

    void testTimer() {
        final SyncTimer s = new SyncTimer();
        s.startAt(System.currentTimeMillis() + 1000);
        mDrawerView.addDrawer(new DrawingView.Drawer() {
            @Override
            public void draw(Canvas canvas) {
                canvas.drawText("TIME : "+ s.getRelativeTime(), 100, 100, new Paint(Color.RED));
            }
        });
    }

    void testRect() {
        final ArrayList<Rect> list = new ArrayList<Rect>();
        list.add(new Rect(10, 100, 500, 200));
        list.add(new Rect(10, 200, 500, 300));
        list.add(new Rect(10, 300, 500, 400));
        list.add(new Rect(10, 400, 500, 500));
        list.add(new Rect(10, 500, 500, 600));

        final PointF[] ps = PolygonUtill.borderOfRectangleUnion(list.toArray(new Rect[list.size()]));
        Random rand = new Random();
        final ArrayList<Integer> color = new ArrayList<Integer>();
        color.add(Color.argb(255, rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
        color.add(Color.argb(255, rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
        color.add(Color.argb(255, rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
        color.add(Color.argb(255, rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
        color.add(Color.argb(255, rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
        color.add(Color.argb(255, rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
        color.add(Color.argb(255, rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
        color.add(Color.argb(255, rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
        mDrawerView.addDrawer(new DrawingView.Drawer() {


            @Override
            public void draw(Canvas canvas) {
                Paint p = new Paint();
                for (int i = 0; i < list.size(); i++) {
                    p.setColor(color.get(i));
                    canvas.drawRect(list.get(i),p );
                }
            }
        });

        mDrawerView.addDrawer(new DrawingView.Drawer() {
            @Override
            public void draw(Canvas canvas) {
                Paint paint = new Paint(Color.BLACK);
                paint.setStrokeWidth(20f);
                for(int i =0 ; i < ps.length - 1; i++) {
                    canvas.drawLine(ps[i].x, ps[i].y, ps[i +1 ].x, ps[i+1].y, paint);
                }
                canvas.drawText("TIME : " + s.getRelativeTime(), 100, 100, new Paint(Color.RED));
            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        /*if (master != null) {
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
        }*/
    }
}


