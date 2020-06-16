package com.acelost.collectionadapter;

import android.util.Log;

/**
 * Окружение, в котором работает {@link CollectionAdapter} и его дружественные классы.
 */
public class CollectionAdapterEnvironment {

    public static boolean LOGGING_ENABLED = false;

    public static boolean DEBUG = false;

    static void log(Object message) {
        Log.i("CollectionAdapterLog", message.toString());
    }
}
