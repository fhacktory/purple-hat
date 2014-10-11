package purplehat.fr.purplehat.screen;

import android.content.Context;
import android.graphics.Point;
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
}
