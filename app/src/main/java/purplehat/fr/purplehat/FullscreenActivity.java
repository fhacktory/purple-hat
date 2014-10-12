package purplehat.fr.purplehat;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import purplehat.fr.purplehat.game.Ball;
import purplehat.fr.purplehat.game.Vector2;
import purplehat.fr.purplehat.game.World;
import purplehat.fr.purplehat.network.ConnectionListener;
import purplehat.fr.purplehat.network.DiscoveryService;
import purplehat.fr.purplehat.utils.AddBallAction;
import purplehat.fr.purplehat.utils.ScreenUtilitiesService;
import purplehat.fr.purplehat.view.BgTouchListener;
import purplehat.fr.purplehat.view.DrawingView;
import purplehat.fr.purplehat.view.RainbowDrawer;

public class FullscreenActivity extends Activity {
    private static final int MASTER_PORT = 1618;
    private static final String MASTER_ID = "424242";
    RainbowDrawer rainbowDrawer;
    private Slave slave;

    private Vector2<Double> viewportOffset;

    private Master master;
    private DrawingView mDrawerView;
    private Thread connectionListeningThread;
    private ConnectionListener connectionListener;
    private static final String LOG_TAG = "FULLSCREEN_ACTIVITY";

    public World getWorld() {
        return world;
    }

    // THE WORLD
    World world = new World();
    private static FullscreenActivity instance = null;

    public static FullscreenActivity getInstance() {
        return instance;
    }

    DiscoveryService discoveryService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        try {
            discoveryService = new DiscoveryService(getApplicationContext());
        } catch (IOException e) {
            Log.w(LOG_TAG, "DiscoveryService instance couldn't be instanciated", e);
        }

        viewportOffset = new Vector2<Double>(0.0, 0.0);

        // Fullscreen
        setContentView(R.layout.activity_fullscreen);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActionBar().hide();

        mDrawerView = (DrawingView) findViewById(R.id.fullscreen_content);
        final DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);


        mDrawerView.addDrawer(new DrawingView.Drawer() {
            @Override
            public void draw(Canvas canvas) {
                drawWorld(canvas);
                drawHUD(canvas);
            }
        });

        // UI drawer
        /*mDrawerView.addDrawer(new DrawingView.Drawer() {
            @Override
            public void draw(Canvas canvas) {

            }
        });*/

        mDrawerView.setOnTouchListener(new BgTouchListener(new BgTouchListener.InOrOutListener() {
            @Override
            public void onIn(int x, int y) {
                Vector2<Double> p = ScreenUtilitiesService.pixel2mm(new Point(x, y));
                onEntrantSwipeEvent(p.getX().intValue(), p.getY().intValue());
            }

            @Override
            public void onOut(int x, int y) {
                Vector2<Double> p = ScreenUtilitiesService.pixel2mm(new Point(x, y));
                onExitingSwipeEvent(p.getX().intValue(), p.getY().intValue());
            }
        }, new BgTouchListener.TouchListener() {
            @Override
            public void onTouchDown(int x, int y) {
                swiping = true;
                if (master != null || slave != null) {
                    AddBallAction action = new AddBallAction(0.0, 0.0, 0., 500., 500.);
                    if (master != null) {
                        master.broadcast(action.getJSON());
                    } else {
                        slave.send(action.getJSON());
                    }
                }
            }

            @Override
            public void onTouchUp(int x, int y) {
                swiping = false;
            }

            @Override
            public void onTouchMove(int x, int y, BgTouchListener.Direction direction) {
                currentSwipePoint.x = x;
                currentSwipePoint.y = y;
                currentSwipeDirection = direction;
            }
        }));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        Timer animationTimer = new Timer();
        animationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (FullscreenActivity.getInstance() == null)
                    return;

                final float dt = 0.030f;
                synchronized (world.getBalls()) {
                    for (Ball ball : world.getBalls()) {
                        double oldX = ball.getPosition().getX();
                        double oldY = ball.getPosition().getY();
                        Vector2<Double> om = ball.getPosition();
                        Vector2<Double> vel = ball.getVelocity();
                        Vector2<Double> dom = new Vector2<Double>(vel.getX() * dt, vel.getY() * dt);
                        om.setX(om.getX() + dom.getX());
                        om.setY(om.getY() + dom.getY());

                        if (master != null) {
                            boolean inWorld = false;
                            // Log.d("nbr", "" + master.getScreenMap().size());
                            PhysicalScreen container = null;
                            for (PhysicalScreen screen : master.getScreenMap().values()) {
                                // Log.d("xy", "" + screen.getX1() + " ; " + screen.getY1() + " -> " + screen.getX2() + " ; " + screen.getY2());
                                if (om.getX() >= screen.getX1()
                                        && om.getX() <= screen.getX2()
                                        && om.getY() >= screen.getY1()
                                        && om.getY() <= screen.getY2()) {
                                    inWorld = true;
                                    break;
                                }
                                if (oldX >= screen.getX1()
                                        && oldX <= screen.getX2()
                                        && oldY >= screen.getY1()
                                        && oldY <= screen.getY2()) {
                                    container = screen;
                                }
                            }

                            if (!inWorld) {
                                PhysicalScreen screen;
                                if (container == null) {
                                    screen = ScreenUtilitiesService.buildBasePhysicalScreen();
                                } else {
                                    screen = container;
                                }

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
            }
        }, 0, 30);

        // start connexion listener
        connectionListener = new ConnectionListener();
        connectionListeningThread = new Thread(connectionListener);
        connectionListeningThread.start();

        // load bitmap
        /*try {
            InputStream stream = getAssets().open("purplehat-small.png");
            purpleHatBmp = BitmapFactory.decodeStream(stream);
        } catch (IOException e) {
        }*/
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Stop listening (brutally)
        connectionListeningThread.interrupt();

        // Join master/slave
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

    private BgTouchListener.Direction currentSwipeDirection;
    Point currentSwipePoint = new Point();
    boolean swiping = false;

    Bitmap purpleHatBmp = null;
    boolean firstDraw = true;


    private void drawWorld(Canvas canvas) {
        if (getInstance() == null) {
            return;
        }
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        synchronized (world.getBalls()) {
            for (Ball ball : world.getBalls()) {
            if(firstDraw) {
                ball.setRainbowDrawer(new RainbowDrawer(this));
                mDrawerView.addDrawer(ball.getRainbowDrawer());
            }
                Point p = ScreenUtilitiesService.mm2pixel(ball.getPosition(), viewportOffset);
            ball.getRainbowDrawer().setXY(p.x,p.y);
                if (purpleHatBmp == null) {
                    canvas.drawCircle(p.x, p.y, ScreenUtilitiesService.mm2pixel(ball.getRadius().floatValue()), paint);
                } else {
                    canvas.drawBitmap(purpleHatBmp, p.x - (purpleHatBmp.getWidth() / 2),
                            p.y - (purpleHatBmp.getHeight() / 2), paint);
                }
            }
        firstDraw = false;
        }
    }

    private void drawHUD(Canvas canvas) {
        if (swiping && currentSwipeDirection != null) {
            Paint paint = new Paint();
            paint.setStrokeWidth(0);
            Rect rect = null;
            Shader shader = null;

            switch (currentSwipeDirection) {
                case DOWN_UP:
                    rect = new Rect(0, (int) (0.15f * (float) ScreenUtilitiesService.getHeight()),
                            ScreenUtilitiesService.getWidth(), 0);
                    shader = new LinearGradient(rect.left, 0.15f * (float) ScreenUtilitiesService.getHeight(),
                            rect.left, 0,
                            Color.TRANSPARENT, Color.rgb(125 - (int) ((1 - currentSwipePoint.y * 1.0 / ScreenUtilitiesService.getHeight()) * 125),
                            125 - (int) ((1 - currentSwipePoint.y * 1.0 / ScreenUtilitiesService.getHeight()) * 125),
                            125 - (int) ((1 - currentSwipePoint.y * 1.0 / ScreenUtilitiesService.getHeight()) * 125)), Shader.TileMode.CLAMP);
                    break;

                case UP_DOWN:
                    rect = new Rect(0, (int) (0.85f * (float) ScreenUtilitiesService.getHeight()),
                            ScreenUtilitiesService.getWidth(), ScreenUtilitiesService.getHeight());
                    shader = new LinearGradient(rect.left, 0.85f * (float) ScreenUtilitiesService.getHeight(),
                            rect.left, ScreenUtilitiesService.getHeight(),
                            Color.TRANSPARENT, Color.rgb(125 - (int) ((currentSwipePoint.y * 1.0 / ScreenUtilitiesService.getHeight()) * 125),
                            125 - (int) ((currentSwipePoint.y * 1.0 / ScreenUtilitiesService.getHeight()) * 125),
                            125 - (int) ((currentSwipePoint.y * 1.0 / ScreenUtilitiesService.getHeight()) * 125)), Shader.TileMode.CLAMP);
                    break;

                case LEFT_RIGHT:
                    rect = new Rect((int) (0.85f * (float) ScreenUtilitiesService.getWidth()),  0,
                            ScreenUtilitiesService.getWidth(), ScreenUtilitiesService.getHeight());
                    shader = new LinearGradient(0.85f * (float) ScreenUtilitiesService.getWidth(), rect.top,
                            ScreenUtilitiesService.getWidth(), rect.top,
                            Color.TRANSPARENT, Color.rgb(125 - (int) ((currentSwipePoint.x * 1.0 / ScreenUtilitiesService.getWidth()) * 125),
                            125 - (int) ((currentSwipePoint.x * 1.0 / ScreenUtilitiesService.getWidth()) * 125),
                            125 - (int) ((currentSwipePoint.x * 1.0 / ScreenUtilitiesService.getWidth()) * 125)), Shader.TileMode.CLAMP);
                    break;

                case RIGHT_LEFT:
                    rect = new Rect(0, 0,
                            (int) (0.15f * (float) ScreenUtilitiesService.getWidth()), ScreenUtilitiesService.getHeight());
                    shader = new LinearGradient(0.15f * (float) ScreenUtilitiesService.getWidth(), rect.top,
                            0, rect.top,
                            Color.TRANSPARENT, Color.rgb(125 - (int) ((1 - currentSwipePoint.x * 1.0 / ScreenUtilitiesService.getWidth()) * 125),
                            125 - (int) ((1 - currentSwipePoint.x * 1.0 / ScreenUtilitiesService.getWidth()) * 125),
                            125 - (int) ((1 - currentSwipePoint.x * 1.0 / ScreenUtilitiesService.getWidth()) * 125)), Shader.TileMode.CLAMP);
                    break;
            }

            if (rect != null && shader != null) {
                paint.setShader(shader);
                canvas.drawRect(rect, paint);
            }
        }
    }

    public void becomeASlave(byte[] masterAddress, String id) {
        Log.d(LOG_TAG, "become a slave");
        slave = new Slave(id);

        slave.addListener("create ball", new Slave.Listener() {
            @Override
            public void notify(JSONObject data) {
                addBallInWorld(AddBallAction.parseJson(data).getBall());
            }
        });

        slave.addListener("world:position", new Slave.Listener() {
            @Override
            public void notify(JSONObject data) {
                try {
                    JSONArray posList = data.getJSONArray("positions");
                    for (int i = 0; i < posList.length(); i++) {
                        JSONObject posData = posList.getJSONObject(i);
                        String id = posData.getString("id");
                        if (id.equals(slave.getId())) {
                            double posX = posData.getDouble("dx");
                            double posY = posData.getDouble("dy");
                            viewportOffset.setX(posX);
                            viewportOffset.setY(posY);
                            Log.d("LoL", "maj viewport");
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        slave.addListener("world:balls", new Slave.Listener() {
            @Override
            public void notify(JSONObject data) {
                try {
                    JSONArray balls = data.getJSONArray("balls");
                    synchronized (world.getBalls()) {
                        world.getBalls().clear();
                        for (int i = 0; i < balls.length(); i++) {
                            JSONObject ballData = balls.getJSONObject(i);
                            double x = ballData.getDouble("x");
                            double y = ballData.getDouble("y");
                            double vx = ballData.getDouble("vx");
                            double vy = ballData.getDouble("vy");
                            double r = ballData.getDouble("r");
                            Ball ball = new Ball(new Vector2<Double>(x, y), r);
                            ball.setVelocity(new Vector2<Double>(vx, vy));
                            world.getBalls().add(ball);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // connect to master
        slave.connect(masterAddress, MASTER_PORT);
    }

    public void becomeAMaster() {
        Log.d(LOG_TAG, "becoming a master");
        master = new Master(MASTER_PORT, MASTER_ID, null);
        master.start();

        Timer updateWorldTimer = new Timer();
        updateWorldTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                master.broadcastWorld();
            }
        }, 0, 30);
    }

    public void addBallInWorld(Ball ball) {
        synchronized (world.getBalls()) {
            world.getBalls().add(ball);
        }
    }

    public void onExitingSwipeEvent(final int swipeX, final int swipeY) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // figure out the master's address
                InetAddress masterAddress = null;
                if (master != null) {
                    try {
                        masterAddress = discoveryService.getLocalIp();
                    } catch (UnknownHostException e) {
                    }
                } else if (slave != null) {
                    try {
                        masterAddress = InetAddress.getByAddress(slave.getMasterAddress());
                    } catch (UnknownHostException e) {
                    }
                }

                // start discovering
                try {
                    if (discoveryService != null) {
                        discoveryService.waitConnection(masterAddress, swipeX, swipeY);
                    }
                } catch (IOException e) {
                    Log.w(LOG_TAG, "discovery (waitConnection) interrupted", e);
                }
            }
        }).start();
    }

    public void onEntrantSwipeEvent(final int swipeX, final int swipeY) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (slave == null && master == null && discoveryService != null) {
                    try {
                        discoveryService.askConnection(swipeX, swipeY);
                    } catch (IOException e) {
                        Log.w(LOG_TAG, "discovery (askConnection) interrupted", e);
                    }
                }
            }
        }).start();
    }

    public Master getMaster() {
        return master;
    }

    public Slave getSlave() {
        return slave;
    }
}
