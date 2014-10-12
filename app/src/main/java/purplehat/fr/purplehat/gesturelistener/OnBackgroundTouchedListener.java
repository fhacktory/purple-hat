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
    private static final String TAG = "BackgroundTouched";

    private static final int MIN_TOUCH_DISTANCE_DIRECTION = 10;
    private static final int SCREEN_IO_MARGIN = 50;
    private final InOrOutListener inOrOutListener;

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
        BOTH
    }

    PointF origin;
    Direction direction;
    IO inorout = IO.NONE;
    float lineWidth = 80;

    public interface InOrOutListener {
        public void onIn(int x, int y);

        public void onOut(int x, int y);
    }

    public OnBackgroundTouchedListener(InOrOutListener inOrOutListener) {
        this.inOrOutListener = inOrOutListener;
    }

    public boolean onTouch(View v, MotionEvent motionEvent) {
        DrawingView surfaceView = (DrawingView) v;

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //TODO getTimer time
                origin = new PointF(0, 0);
                inorout = IO.NONE;
                origin.set(motionEvent.getRawX(), motionEvent.getRawY());
                return true;
            case MotionEvent.ACTION_MOVE:
                return handleMove(surfaceView, motionEvent);
            case MotionEvent.ACTION_UP:
                handleUp(surfaceView, motionEvent);
                Log.d(TAG, "IN or OUT ? " + inorout.name());
                return true;
        }
        return false;
    }

    private boolean handleMove(DrawingView v, MotionEvent event) {
        Point center = ScreenUtilitiesService.getDisplayCenter();
        final float x, y;
        x = event.getRawX();
        y = event.getRawY();

        Float dist_x = Math.abs(x - origin.x);
        Float dist_y = Math.abs(y - origin.y);


        // Detect movement direction
        if (dist_x > MIN_TOUCH_DISTANCE_DIRECTION && dist_y < MIN_TOUCH_DISTANCE_DIRECTION) {
            if (x > origin.x) {
                direction = Direction.LEFT_RIGHT;
            } else {
                direction = Direction.RIGHT_LEFT;
            }
            return true;
        } else if (dist_y > MIN_TOUCH_DISTANCE_DIRECTION && dist_x < MIN_TOUCH_DISTANCE_DIRECTION) {
            if (y > origin.y) {
                direction = Direction.UP_DOWN;
            } else {
                direction = Direction.DOWN_UP;
            }
            return true;
        }

        Log.d(TAG, "Direction " + ((direction != null) ? direction.name() : ""));
        return false;
    }

    public void handleUp(DrawingView v, MotionEvent event) {
        Point center = ScreenUtilitiesService.getDisplayCenter();
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        int rightMargin = ScreenUtilitiesService.getWidth() - SCREEN_IO_MARGIN;
        int bottomMargin = ScreenUtilitiesService.getHeight() - SCREEN_IO_MARGIN;
        if (direction == Direction.LEFT_RIGHT && x > rightMargin) {
            if(origin.x < SCREEN_IO_MARGIN) {
                inorout = IO.BOTH;
            } else {
                inorout = IO.OUT;
            }
        } else if (direction == Direction.RIGHT_LEFT && x < SCREEN_IO_MARGIN) {
            if(origin.x > rightMargin) {
                inorout = IO.BOTH;
            } else {
                inorout = IO.OUT;
            }
        } else if (direction == Direction.UP_DOWN && y > bottomMargin) {
            if(origin.y < SCREEN_IO_MARGIN) {
                inorout = IO.BOTH;
            } else {
                inorout = IO.OUT;
            }
        } else if (direction == Direction.DOWN_UP && y < SCREEN_IO_MARGIN) {
            if(origin.y > bottomMargin) {
                inorout = IO.BOTH;
            } else {
                inorout = IO.OUT;
            }
        } else if (direction == Direction.LEFT_RIGHT
                && x > SCREEN_IO_MARGIN) {
            inorout = IO.IN;
        } else if (direction == Direction.RIGHT_LEFT
                && x < rightMargin) {
            inorout = IO.IN;
        } else if (direction == Direction.UP_DOWN
                && y > SCREEN_IO_MARGIN) {
            inorout = IO.IN;
        } else if (direction == Direction.DOWN_UP
                && y < bottomMargin) {
            inorout = IO.IN;
        }


        // Fire event for in/out
        if(inorout.equals(IO.IN)||inorout==IO.BOTH)
        {
            inOrOutListener.onIn(x, y);
        }

        if(inorout.equals(IO.OUT)||inorout==IO.BOTH)
        {
            inOrOutListener.onOut(x, y);
        }
    }

}
