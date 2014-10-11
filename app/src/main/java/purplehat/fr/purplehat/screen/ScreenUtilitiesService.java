package purplehat.fr.purplehat.screen;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import purplehat.fr.purplehat.FullscreenActivity;
import purplehat.fr.purplehat.PhysicalScreen;
import purplehat.fr.purplehat.PurpleHat;

/**
 * Created by vcaen on 11/10/2014.
 */
public class ScreenUtilitiesService {
    // Magic conversion numbers!
    public static final double INCHES_TO_MM = 25.4;

    public static Point getDisplayCenter() {
        WindowManager wm = (WindowManager) PurpleHat.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point center = new Point();
        display.getSize(center);
        center.x /= 2;
        center.y /= 2;
        return center;
    }

    public static Point pixel2mm(Point point) {
        DisplayMetrics dm = new DisplayMetrics();
        FullscreenActivity.getInstance().getWindowManager().getDefaultDisplay().getMetrics(dm);
        return new Point(
                (int) (INCHES_TO_MM * point.x / dm.xdpi),
                (int) (INCHES_TO_MM * point.y / dm.ydpi));
    }

    public static PhysicalScreen buildBasePhysicalScreen() {
        DisplayMetrics dm = new DisplayMetrics();
        FullscreenActivity.getInstance().getWindowManager().getDefaultDisplay().getMetrics(dm);
        return new PhysicalScreen(0, 0,
                (int) (INCHES_TO_MM * dm.widthPixels / dm.xdpi),
                (int) (INCHES_TO_MM * dm.heightPixels / dm.ydpi));
    }
}
