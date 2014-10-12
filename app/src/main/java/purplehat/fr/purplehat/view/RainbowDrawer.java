package purplehat.fr.purplehat.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayDeque;
import java.util.Iterator;

import purplehat.fr.purplehat.R;

/**
 * Created by vcaen on 12/10/2014.
 */
public class RainbowDrawer implements DrawingView.Drawer {

    private static final int HISTORY_SIZE = 200;
    private static final int RAY_WIDTH = 4;
    Context mContext;
    double rotation = 0;
    float oldX, oldY;
    float newX, newY;
    Bitmap rainbow;
    Bitmap back;
    Paint paint = new Paint();
    ArrayDeque<OldPos> oldPoses = new ArrayDeque<OldPos>(20000);
    String[] colors = {"#6400a4", "#0c00a4", "#00baff", "#23ec08", "#ffde00", "#ff7200","#ff0000" };

    class OldPos {
        float x;
        float y;
        double rotation;
    }

    public RainbowDrawer(Context c) {

        mContext = c;
        rainbow = BitmapFactory.decodeResource(c.getResources(), R.drawable.rainbow);
        back = BitmapFactory.decodeResource(c.getResources(), R.drawable.rainbow_bg);
        newX = 0;
        newY = 0;
    }

    public void setXY(float x, float y) {
        OldPos oldPos = new OldPos();
        oldPos.x = newX;
        oldPos.y = newY;
        oldPos.rotation = rotation;
        oldPos.rotation = rotation;
        synchronized (oldPoses) {
            oldPoses.addLast(oldPos);
        }
        oldX = newX;
        oldY = newY;
        newX = x;
        newY = y;
        rotation = GetAngleOfLineBetweenTwoPoints(oldX,oldY, newX, newY);
        if(oldPoses.size() > HISTORY_SIZE) {
            synchronized (oldPoses) {
                oldPoses.removeFirst();
            }
        }
    }

    public static double GetAngleOfLineBetweenTwoPoints(float x1, float y1, float x2,float y2) {
        double xDiff = x2 - x1;
        double yDiff = y2 - y1;
        return Math.toDegrees(Math.atan2(yDiff, xDiff)) - 90;
    }

    @Override
    public void draw(Canvas canvas) {

        int alpha = 255;
        OldPos previous = new OldPos();
        previous.x = newX;
        previous.y = newY;
        previous.rotation = rotation;
        OldPos current;
        paint.setStrokeWidth(RAY_WIDTH);
        float offset = -(colors.length/2) * RAY_WIDTH;
        Iterator itr = oldPoses.iterator();
        //*
        for (int i = 0; i < colors.length; i++) {


            int color = Color.parseColor(colors[i]);
            paint.setColor(color);

            synchronized (oldPoses) {
                while (itr.hasNext()) {
                    current = (OldPos) itr.next();
                    canvas.drawLine(current.x + offset, current.y, previous.x + offset, previous.y, paint);
                    previous = current;
                }

            }
            offset += RAY_WIDTH;
        }
            //*/
            /*
             Iterator itr = oldPoses.iterator();

            while (itr.hasNext()) {
                current = (OldPos) itr.next();
                paint.setAlpha(alpha);
                Matrix matrix = new Matrix();
                matrix.reset();
                matrix.postTranslate(-back.getWidth() / 2, -back.getHeight() / 2); // Centers image
                matrix.postRotate((float) current.rotation);
                matrix.postTranslate(current.x , current.y - back.getHeight()/2);
                canvas.drawBitmap(back, matrix, null);
                matrix.reset();
                matrix.postTranslate(-rainbow.getWidth() / 2, -rainbow.getHeight() / 2); // Centers image
                matrix.postRotate((float) current.rotation);
                matrix.postTranslate(current.x , current.y - rainbow.getHeight()/2);
                canvas.drawBitmap(rainbow, matrix, null);

                matrix.reset();
                matrix.postTranslate(-rainbow.getWidth() / 2, -rainbow.getHeight() / 2); // Centers image
                matrix.postRotate((float) current.rotation);
                matrix.postTranslate(current.x, current.y + rainbow.getHeight()/2);
                canvas.drawBitmap(rainbow, matrix, null);
                alpha -= alpha / HISTORY_SIZE;
            }
        //*/



    }
}
