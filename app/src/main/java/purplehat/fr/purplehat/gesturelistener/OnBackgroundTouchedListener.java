package purplehat.fr.purplehat.gesturelistener;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import purplehat.fr.purplehat.screen.ScreenUtilitiesService;
import purplehat.fr.purplehat.view.DrawingView;

/**
 * Created by vcaen on 11/10/2014.
 */
public class OnBackgroundTouchedListener implements View.OnTouchListener {
    private static final String LOG_TAG = "BackgroundTouched";

    private static final int MIN_TOUCH_DISTANCE_DIRECTION = 10;
    private static final int SCREEN_IO_MARGIN = 30;
    private final InOrOutListener inOrOutListener;
    private TouchListener touchListener;

    public enum Direction {
        UP_DOWN,
        DOWN_UP,
        LEFT_RIGHT,
        RIGHT_LEFT
    }

    public enum IO {
        IN,
        OUT,
        NONE,
    }

    PointF origin;
    Direction direction;
    IO inorout = IO.NONE;
    float lineWidth = 80;

    public interface InOrOutListener {
        public void onIn(int x, int y);

        public void onOut(int x, int y);
    }

    public interface TouchListener {
        public void onTouchDown(int x, int y);
        public void onTouchUp(int x, int y);
        public void onTouchMove(int x, int y);
    }

    public OnBackgroundTouchedListener(InOrOutListener inOrOutListener) {
        this.inOrOutListener = inOrOutListener;
    }

    public OnBackgroundTouchedListener(InOrOutListener inOrOutListener, TouchListener touchListener) {
        this(inOrOutListener);
        this.touchListener = touchListener;
    }

    public boolean onTouch(View v, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN :
                //TODO getTimer time
                return handleDown(motionEvent);

            case MotionEvent.ACTION_MOVE :
                return handleMove(motionEvent);

            case MotionEvent.ACTION_UP:
                handleUp(motionEvent);
                return true;
        }
        return false;
    }

    private boolean handleDown(MotionEvent event) {
        origin = new PointF(0,0);
        inorout = IO.NONE;
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        origin.set(event.getRawX(), event.getRawY());

        // Fire touch down
        touchListener.onTouchDown(x, y);

        return true;
    }

    private boolean handleMove(MotionEvent event) {
        Point center = ScreenUtilitiesService.getDisplayCenter();
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();

        Float dist_x = Math.abs(x - origin.x);
        Float dist_y = Math.abs(y - origin.y);

        // Detect movement direction
        if (dist_x > MIN_TOUCH_DISTANCE_DIRECTION && dist_y < MIN_TOUCH_DISTANCE_DIRECTION) {
            if(x > origin.x) {
                direction = Direction.LEFT_RIGHT;
            } else {
                direction = Direction.RIGHT_LEFT;
            }
            return true;
        } else if (dist_y > MIN_TOUCH_DISTANCE_DIRECTION && dist_x < MIN_TOUCH_DISTANCE_DIRECTION) {
            if(y > origin.y) {
                direction = Direction.UP_DOWN;
            } else {
                direction = Direction.DOWN_UP;
            }
            return true;
        }

        Log.d(LOG_TAG, "Direction " + ((direction != null) ? direction.name() : ""));

        // Fire touch move
        touchListener.onTouchMove(x, y);

        return false;
    }

    public void handleUp(MotionEvent event) {
        Point center = ScreenUtilitiesService.getDisplayCenter();
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        if(direction == Direction.LEFT_RIGHT && x > ScreenUtilitiesService.getWidth() - SCREEN_IO_MARGIN) {
            inorout = IO.OUT;
        } else if(direction == Direction.RIGHT_LEFT && x < SCREEN_IO_MARGIN) {
            inorout = IO.OUT;
        } else if(direction == Direction.UP_DOWN && y > ScreenUtilitiesService.getHeight() - SCREEN_IO_MARGIN) {
            inorout = IO.OUT;
        } else if(direction == Direction.DOWN_UP && y < SCREEN_IO_MARGIN) {
            inorout = IO.OUT;
        } else if(direction == Direction.LEFT_RIGHT
            && Math.abs(x - ScreenUtilitiesService.getDisplayCenter().y) <  SCREEN_IO_MARGIN) {
            inorout = IO.IN;
        } else if(direction == Direction.RIGHT_LEFT
                && Math.abs(x - ScreenUtilitiesService.getDisplayCenter().x) <  SCREEN_IO_MARGIN) {
            inorout = IO.IN;
        } else if(direction == Direction.UP_DOWN
                && Math.abs(y - ScreenUtilitiesService.getDisplayCenter().y) <  SCREEN_IO_MARGIN) {
            inorout = IO.IN;
        } else if(direction == Direction.DOWN_UP
                && Math.abs(y - ScreenUtilitiesService.getDisplayCenter().y) <  SCREEN_IO_MARGIN) {
            inorout = IO.IN;
        }

        // Fire touch up
        touchListener.onTouchUp(x, y);

        // Fire event for in/out
        Log.d(LOG_TAG, "IN or OUT ? " + inorout.name());
        if (inorout.equals(IO.IN)) {
            inOrOutListener.onIn(x, y);
        } else if (inorout.equals(IO.OUT)) {
            inOrOutListener.onOut(x, y);
        }
    }

}
