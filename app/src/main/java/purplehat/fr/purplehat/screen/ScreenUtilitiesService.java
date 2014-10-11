package purplehat.fr.purplehat.screen;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import purplehat.fr.purplehat.PurpleHat;

/**
 * Created by vcaen on 11/10/2014.
 */
public class ScreenUtilitiesService {

    public static Point getDisplayCenter() {
        WindowManager wm = (WindowManager) PurpleHat.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point center = new Point();
        display.getSize(center);
        center.x /= 2;
        center.y /=2;
        return center;
    }

    public static int getWidth() {
        WindowManager wm = (WindowManager) PurpleHat.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        return dm.widthPixels;
    }

    public static int getHeight() {
        WindowManager wm = (WindowManager) PurpleHat.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        return dm.heightPixels;
    }
}
