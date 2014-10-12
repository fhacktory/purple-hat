package purplehat.fr.purplehat.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by vcaen on 11/10/2014.
 */
public class DrawingView extends SurfaceView implements SurfaceHolder.Callback {

    public static final String LOG_TAG = "DRAWING_VIEW";

    public interface Drawer {
        public void draw(Canvas canvas);
    }

    private Collection<Drawer> drawers;

    public class DrawerThread extends Thread {
        private final SurfaceHolder mSurfaceHolder;

        boolean mRun = false;

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

        public DrawerThread(SurfaceHolder surfaceHolder) {
            mSurfaceHolder = surfaceHolder;
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
            if (canvas == null) {
                return;
            }
            Log.d(LOG_TAG, "drawing canvas");

            // fill background
            canvas.drawColor(Color.WHITE);

            // drawers draw
            for (Drawer d : drawers) {
                d.draw(canvas);
            }
        }
    }

    private DrawerThread thread;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        thread = new DrawerThread(holder);

        drawers = Collections.synchronizedCollection(new ArrayList<Drawer>());
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
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

    public Drawer addDrawer(Drawer drawer) {
        drawers.add(drawer);
        return drawer;
    }

    public void removeDrawer(Drawer drawer) {
        drawers.remove(drawer);
    }
}
