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

    public enum Direction {
        UP_DOWN,
        DOWN_UP,
        LEFT_RIGHT,
        RIGHT_LEFT
    }

    PointF origin;
    Direction direction;
    float lineWidth = 80;

    public boolean onTouch(View v, MotionEvent motionEvent) {
        DrawingView surfaceView = (DrawingView) v;

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN :
                //TODO getTimer time
                origin = new PointF(0,0);

                origin.set(motionEvent.getRawX(), motionEvent.getRawY());
                return true;
            case MotionEvent.ACTION_MOVE :
                return handleMove(surfaceView, motionEvent);
            case MotionEvent.ACTION_UP:
                //thread.doClearMoveData();
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

        Log.d(TAG, "Direction " + ((direction != null) ? direction.name() : ""));
        return false;
    }

}
