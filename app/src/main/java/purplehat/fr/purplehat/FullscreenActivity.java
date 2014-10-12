package purplehat.fr.purplehat;


import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import purplehat.fr.purplehat.Geometrics.Edge;
import purplehat.fr.purplehat.Geometrics.PolygonUtill;
import purplehat.fr.purplehat.game.Ball;
import purplehat.fr.purplehat.game.Vector2;
import purplehat.fr.purplehat.game.World;
import purplehat.fr.purplehat.gesturelistener.OnBackgroundTouchedListener;
import purplehat.fr.purplehat.screen.ScreenUtilitiesService;
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

    private Vector2<Double> viewportOffset;

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
        try {
            discoveryService = new DiscoveryService(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }

        viewportOffset = new Vector2<Double>(0.0, 0.0);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        final View contentView = findViewById(R.id.fullscreen_content);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActionBar().hide();

        mDrawerView = (DrawingView) findViewById(R.id.fullscreen_content);
        final DisplayMetrics dm = new DisplayMetrics();
        FullscreenActivity.getInstance().getWindowManager().getDefaultDisplay().getMetrics(dm);

        // World drawer
        mDrawerView.addDrawer(new DrawingView.Drawer() {
            @Override
            public void draw(Canvas canvas) {
                drawWorld(canvas);
            }
        });

        // UI drawer
        mDrawerView.addDrawer(new DrawingView.Drawer() {
            @Override
            public void draw(Canvas canvas) {
                drawHUD(canvas);
            }
        });

        mDrawerView.setOnTouchListener(new OnBackgroundTouchedListener(new OnBackgroundTouchedListener.InOrOutListener() {
            @Override
            public void onIn(int x, int y) {
                onEntrantSwipeEvent(x, y);
            }

            @Override
            public void onOut(int x, int y) {
                onExitingSwipeEvent(x, y);
            }
        }, new OnBackgroundTouchedListener.TouchListener() {
            @Override
            public void onTouchDown(int x, int y) {
                swiping = true;
            }

            @Override
            public void onTouchUp(int x, int y) {
                swiping = false;
            }

            @Override
            public void onTouchMove(int x, int y, OnBackgroundTouchedListener.Direction direction) {
                currentSwipePoint.x = x;
                currentSwipePoint.y = y;
                currentSwipeDirection = direction;
            }
        }));

        Timer animationTimer = new Timer();
        animationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final float dt = 0.030f;
                for (Ball ball : world.getBalls()) {
                    Vector2<Double> om = ball.getPosition();
                    Vector2<Double> vel = ball.getVelocity();
                    Vector2<Double> dom = new Vector2<Double>(vel.getX() * dt, vel.getY() * dt);
                    om.setX(om.getX() + dom.getX());
                    om.setY(om.getY() + dom.getY());

                    if (master != null) {
                        boolean inWorld = false;
                        for (PhysicalScreen screen : master.getScreenMap().values()) {
                            if (om.getX() >= screen.getX1()
                                    && om.getX() <= screen.getX2()
                                    && om.getY() >= screen.getY1()
                                    && om.getY() <= screen.getY2()) {
                                inWorld = true;
                                break;
                            }
                        }

                        if (!inWorld) {
                            PhysicalScreen screen = ScreenUtilitiesService.buildBasePhysicalScreen();
                            if (om.getX() < screen.getX1()) {
                                om.setX(screen.getX1());
                                ball.getVelocity().setX(-ball.getVelocity().getX());
                            } else if (om.getX() > screen.getX2()) {
                                om.setX(screen.getX2());
                                ball.getVelocity().setX(-ball.getVelocity().getX());
                            } else if (om.getY() < screen.getY1()) {
                                om.setY(screen.getY1());
                                ball.getVelocity().setY(-ball.getVelocity().getY());
                            } else if (om.getY() > screen.getY2()) {
                                om.setY(screen.getY2());
                                ball.getVelocity().setY(-ball.getVelocity().getY());
                            }
                        }
                    } else if (master == null && slave == null) {
                        boolean inWorld = false;
                        PhysicalScreen screen = ScreenUtilitiesService.buildBasePhysicalScreen();
                        if (om.getX() >= screen.getX1()
                                && om.getX() <= screen.getX2()
                                && om.getY() >= screen.getY1()
                                && om.getY() <= screen.getY2()) {
                            inWorld = true;
                        }

                        if (!inWorld) {
                            if (om.getX() < screen.getX1()) {
                                om.setX(screen.getX1());
                                ball.getVelocity().setX(-ball.getVelocity().getX());
                            } else if (om.getX() > screen.getX2()) {
                                om.setX(screen.getX2());
                                ball.getVelocity().setX(-ball.getVelocity().getX());
                            } else if (om.getY() < screen.getY1()) {
                                om.setY(screen.getY1());
                                ball.getVelocity().setY(-ball.getVelocity().getY());
                            } else if (om.getY() > screen.getY2()) {
                                om.setY(screen.getY2());
                                ball.getVelocity().setY(-ball.getVelocity().getY());
                            }
                        }
                    }

                    ball.setPosition(om);
                }
            }
        }, 0, 30);

        new Thread(new ConnexionListener()).start();
    }

    private OnBackgroundTouchedListener.Direction currentSwipeDirection;
    Point currentSwipePoint = new Point();
    boolean swiping = false;

    @Override
    protected void onStop() {
        super.onStop();
        if (master != null) {
            try {
                master.stop();
            } catch (IOException e) {
            } catch (InterruptedException e) {
            }
        } else if (slave != null) {
            slave.close();
        }
    }

    private void drawWorld(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        for (Ball ball : world.getBalls()) {
            Point p = ScreenUtilitiesService.mm2pixel(ball.getPosition(), viewportOffset);
            canvas.drawCircle(p.x, p.y, ScreenUtilitiesService.mm2pixel(ball.getRadius().floatValue()), paint);
        }
    }

    private void drawHUD(Canvas canvas) {
        if (swiping && currentSwipeDirection != null) {
            Paint paint = new Paint();
            paint.setStrokeWidth(0);
            Rect rect = null;
            Shader shader = null;

            switch (currentSwipeDirection) {
                case UP_DOWN:
                    break;

                case DOWN_UP:
                    break;

                case LEFT_RIGHT:
                    rect = new Rect((int) (0.85f * (float) ScreenUtilitiesService.getWidth()),  0,
                            ScreenUtilitiesService.getWidth(), ScreenUtilitiesService.getHeight());
                    shader = new LinearGradient(ScreenUtilitiesService.getWidth() - currentSwipePoint.x / 2, rect.top,
                            ScreenUtilitiesService.getWidth(), rect.top,
                            Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP);
                    break;

                case RIGHT_LEFT:
                    rect = new Rect(0, 0,
                            (int) (0.15f * (float) ScreenUtilitiesService.getWidth()), ScreenUtilitiesService.getHeight());
                    shader = new LinearGradient(currentSwipePoint.x / 2, rect.top,
                            0, rect.top,
                            Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP);
                    break;
            }

            /*if (rect != null && shader != null) {
                paint.setShader(shader);
                canvas.drawRect(rect, paint);
            }*/
        }
    }

    public void becomeASlave(byte[] masterAddress) {
        Log.d("TG", "become slave biatch");
        slave = new Slave();
        /*slave.addListener("world:virtual:updated", new Slave.Listener() {
            @Override
            public void notify(JSONObject data) {
                Log.d("ACTIVITY", "world:updated" + data);
                // world.updateFromJson(data);
            }
        });*/
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

    public Master getMaster() {
        return master;
    }
}
