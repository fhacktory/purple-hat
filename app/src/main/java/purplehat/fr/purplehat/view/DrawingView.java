package purplehat.fr.purplehat.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import purplehat.fr.purplehat.screen.ScreenUtilitiesService;

/**
 * Created by vcaen on 11/10/2014.
 */
public class DrawingView extends SurfaceView implements SurfaceHolder.Callback {

    public static final String TAG = "DrawingView";

    public interface Drawer {
        public void draw(Canvas canvas);
    }

    private Collection<Drawer> drawers;

    public class DrawerThread extends Thread {
        private final SurfaceHolder mSurfaceHolder;
        private final Handler mHandler;
        private final Context mContext;

        private boolean verticalMovement = false;
        private boolean horizontalMovement = false;
        PointF touchLocation = new PointF(0f, 0f);
        float lineWidth;



        boolean mRun = false;

        /**
         * Current height of the surface/canvas.
         *
         * @see #setSurfaceSize
         */
        private int mCanvasHeight = 1;

        /**
         * Current width of the surface/canvas.
         *
         * @see #setSurfaceSize
         */
        private int mCanvasWidth = 1;


        /**
         * Used to signal the thread whether it should be running or not.
         * Passing true allows the thread to run; passing false will shut it
         * down if it's already running. Calling start() after this was most
         * recently called with false will result in an immediate shutdown.
         *
         * @param b true to run, false to shut down
         */
        public void setRunning(boolean b) {
            mRun = b;
        }
        /* Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;
            }
        }


        public DrawerThread(SurfaceHolder surfaceHolder, Context context,
        Handler handler) {
            mSurfaceHolder = surfaceHolder;
            mHandler = handler;
            mContext = context;

        }

        void doVerticalMove(float x, float width, Direction direction) {
            verticalMovement = true;
            lineWidth = width;
            touchLocation.x = x;
            touchLocation.y = 0;
        }

        void doHorizontalMove(float y, float width) {
            horizontalMovement = true;
            lineWidth = width;
            touchLocation.x = 0;
            touchLocation.y = y;
        }

        void doClearMoveData() {
            verticalMovement = false;
            horizontalMovement = false;
            touchLocation.set(0f,0f);
        }



        @Override
        public void run() {
            while (mRun) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        doDraw(c);
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        private void doDraw(Canvas canvas) {

            //Clear canvas
            canvas.drawColor(Color.BLUE);

            if(verticalMovement) {
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                canvas.drawRect(
                        touchLocation.x - lineWidth / 2,
                        0,
                        touchLocation.x + lineWidth / 2,
                        canvas.getHeight(),
                        paint);
            }

            if(horizontalMovement) {
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                canvas.drawRect(
                        0,
                        touchLocation.y - lineWidth / 2,
                        canvas.getWidth(),
                        touchLocation.y + lineWidth / 2,
                        paint);
            }

            for (Drawer d : drawers) {
                d.draw(canvas);
            }

        }

    } // THREAD

    /** Handle to the application context, used to e.g. fetch Drawables. */
    private Context mContext;

    /** Pointer to the text view to display "Paused.." etc. */
    private TextView mStatusText;

    /** The thread that actually draws the animation */
    private DrawerThread thread;

    private static final int MIN_TOUCH_DISTANCE_DIRECTION = 100;
    private static final int LINE_WIDTH = 80;

    public enum Direction {
        UP_DOWN,
        DOWN_UP,
        LEFT_RIGHT,
        RIGHT_LEFT
    }

    PointF origin;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        thread = new DrawerThread(holder, context, new Handler() {

            @Override
            public void handleMessage(Message m) {
                mStatusText.setText(m.getData().getString("text"));
            }
        });

        setFocusable(true); // make sure we get key events
        drawers = Collections.synchronizedCollection(new ArrayList<Drawer>());
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {


            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN :
                    //TODO getTimer time
                    origin = new PointF(0,0);
                    origin.set(motionEvent.getRawX(), motionEvent.getRawY());
                    return true;
                case MotionEvent.ACTION_MOVE :
                    return handleMove(motionEvent);
                case MotionEvent.ACTION_UP:
                    thread.doClearMoveData();
                    return true;

            }
            return false;
        }


    private boolean handleMove(MotionEvent event) {
        Point center = ScreenUtilitiesService.getDisplayCenter();
        float x, y;
        x = event.getRawX();
        y = event.getRawY();

        Float dist_x = Math.abs(x - origin.x);
        Float dist_y = Math.abs(y - origin.y);

        // Detect movement direction
        if (dist_x > MIN_TOUCH_DISTANCE_DIRECTION && dist_y < MIN_TOUCH_DISTANCE_DIRECTION) {
            thread.doHorizontalMove(y, LINE_WIDTH);
            return true;
        } else if (dist_y > MIN_TOUCH_DISTANCE_DIRECTION && dist_x < MIN_TOUCH_DISTANCE_DIRECTION) {
            thread.doVerticalMove(x, LINE_WIDTH, Direction.DOWN_UP);
            return true;
        }
        return false;
    }



    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        thread.setSurfaceSize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }


    public void addDrawer(Drawer drawer) {
        drawers.add(drawer);
    }


}
