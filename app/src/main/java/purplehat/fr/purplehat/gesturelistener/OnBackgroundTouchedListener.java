package purplehat.fr.purplehat.gesturelistener;

import android.graphics.Point;
import android.graphics.PointF;
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



    private static enum Direction {
        HORIZONTAL,
        VERTICAL
    }

    DrawingView mDrawingView;

    PointF origin;
    Direction direction;

    public OnBackgroundTouchedListener(DrawingView background) {
        mDrawingView = background;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        origin = new PointF(0,0);

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN :
                //TODO getTimer time
                origin.set(motionEvent.getRawX(), motionEvent.getRawY());
                return true;
            case MotionEvent.ACTION_MOVE :
                return handleMove(view, motionEvent);

        }
        return false;
    }


    private boolean handleMove(View v,MotionEvent event) {
        Point center = ScreenUtilitiesService.getDisplayCenter();
        float x, y;
        x = event.getRawX();
        y = event.getRawY();

        Float dist_x = Math.abs(x - origin.x);
        Float dist_y = Math.abs(y - origin.y);

        // Detect movement direction
        if (dist_x > MIN_TOUCH_DISTANCE_DIRECTION && dist_y < MIN_TOUCH_DISTANCE_DIRECTION) {
            direction = Direction.HORIZONTAL;
        } else if (dist_y > MIN_TOUCH_DISTANCE_DIRECTION && dist_x < MIN_TOUCH_DISTANCE_DIRECTION) {
            direction = Direction.VERTICAL;
        }










        return false;
    }

}
