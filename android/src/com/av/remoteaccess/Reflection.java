package com.av.remoteaccess;

import java.lang.reflect.Method;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;

public class Reflection{
    static String TAG = Reflection.class.getCanonicalName();
    private static Class <? > Surface;
    public static Method screenshotBmp;
    public static Method screenshotSurface; 
    public static Method createDisplay; 
    public static Method setDisplaySize;
    public static Method openTransaction;
    public static Method closeTransaction;
    public static Method destroyDisplay;
    public static Method setDisplayLayerStack;
    public static Method setDisplayProjection;
    public static Method setDisplaySurface;
    
    public static void init() {
        try {
            Surface = Class.forName("android.view.SurfaceControl");
        } catch (Exception e) {
            Log.i(TAG, "no such class found", e);
            try {
                Surface = Class.forName("android.view.Surface");
            } catch (Exception e1) {
                Log.i(TAG, "no such class found", e1);
            }
        }
        try {
            screenshotBmp = Surface.getMethod("screenshot", int.class, int.class);
            Bitmap bmp = (Bitmap) Reflection.screenshotBmp.invoke(null, 384, 682);
//            Log.i(TAG, "+++++++++++++++++\n+++++++++++++\n+++++++++++++after bmp, length: " + bmp.getByteCount());
        } catch (Exception e) {
            Log.i(TAG, "no such method found", e);
        }
        try {
            screenshotSurface = Surface.getMethod("screenshot", IBinder.class, Surface.class, int.class, int.class);
        } catch (Exception e) {
            Log.i(TAG, "no such method found", e);
        }
        try {
            createDisplay = Surface.getMethod("createDisplay", String.class, boolean.class);
        } catch (Exception e) {
            Log.i(TAG, "no such method found", e);
        }
        try {
            setDisplaySize = Surface.getMethod("setDisplaySize", IBinder.class, int.class, int.class);
        } catch (Exception e) {
            Log.i(TAG, "no such method found", e);
        }
        try {
            openTransaction = Surface.getMethod("openTransaction");
        } catch (Exception e) {
            Log.i(TAG, "no such method found", e);
        }
        try {
            closeTransaction = Surface.getMethod("closeTransaction");
        } catch (Exception e) {
            Log.i(TAG, "no such method found", e);
        }
        try {
            destroyDisplay = Surface.getMethod("destroyDisplay", IBinder.class);
        } catch (Exception e) {
            Log.i(TAG, "no such method found", e);
        }
        try {
            setDisplayLayerStack = Surface.getMethod("setDisplayLayerStack", IBinder.class, int.class);
        } catch (Exception e) {
            Log.i(TAG, "no such method found", e);
        }
        try {
            setDisplayProjection = Surface.getMethod("setDisplayProjection", IBinder.class, int.class, Rect.class, Rect.class);
        } catch (Exception e) {
            Log.i(TAG, "no such method found", e);
        }
        try {
            setDisplaySurface = Surface.getMethod("setDisplaySurface", IBinder.class, Surface.class);
        } catch (Exception e) {
            Log.i(TAG, "no such method found", e);
        }
    }
}