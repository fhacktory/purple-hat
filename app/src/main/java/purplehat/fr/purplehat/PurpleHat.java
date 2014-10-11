package purplehat.fr.purplehat;

import android.app.Application;
import android.content.Context;

/**
 * Created by vcaen on 11/10/2014.
 */
public class PurpleHat extends Application {

    private static Context context;

    public void onCreate(){
        super.onCreate();
        PurpleHat.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return PurpleHat.context;
    }
}