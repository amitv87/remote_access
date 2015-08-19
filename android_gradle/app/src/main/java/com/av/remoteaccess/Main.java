package com.av.remoteaccess;

import android.os.Looper;

public class Main {
    public static String TAG = Main.class.getCanonicalName();
    public static void main(String[] argv) {
        Looper.prepareMainLooper();
        try {
            Global.init();
            Reflection.init();            
            InputManager.init();
            VideoServer.init(8081);
            fi.iki.elonen.SimpleWebServer.main(new String[]{"-d","/data/data/com.av.remoteaccess/public/", "-q"});
        } catch (Exception e) {
            e.printStackTrace();
        }
        Looper.loop();
    }
}
