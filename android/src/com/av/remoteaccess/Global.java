package com.av.remoteaccess;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Application;
import android.app.LoadedApk;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.IClipboard;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.ServiceManager;
import android.util.Log;
import android.view.Display;
import android.view.IWindowManager;
import android.view.WindowManager;
import android.os.Process;

public class Global {
    private static final String TAG = Global.class.getCanonicalName();
    public static Context context;
    public static IWindowManager iwm;
    public static WindowManager wm;
    public static IClipboard cb;
    public static ClipboardManager cm;
    public static DisplayManager mDisplayManager;
    public static int dev_width;
    public static int dev_height;
    public static final String packageName = "com.android.shell";
    public static int s_width = 384;
    public static int s_height = 682;
    public static int ratio = 2;
    
    public static void init() {
        try {
            getIPAddress();
            Class <? > Proc = Class.forName("android.os.Process");
            
            Method setArgV0 = Proc.getMethod("setArgV0", String.class);
            Log.i(TAG, "setArgV0: " + setArgV0.toString());
            setArgV0.invoke(null, "RemoteAccess");
            
//            Method setGid = Proc.getMethod("setGid", int.class);
//            Log.i(TAG, "setGid: " + setGid.toString());
//            setGid.invoke(null, 0);
//            Method setUid = Proc.getMethod("setUid", int.class);
//            Log.i(TAG, "setUid: " + setUid.toString());
//            setUid.invoke(null, 0);
//            Log.i(TAG, "myUid: " + Process.myUid());
//            Log.i(TAG, "myUserHandle: " + Process.myUserHandle());
            
            Class <? > ActivityThread = Class.forName("android.app.ActivityThread");
            Method systemMain = ActivityThread.getMethod("systemMain");
            Object activityThread = systemMain.invoke(null, new Object[0]);
            Method getSystemContext = activityThread.getClass().getMethod("getSystemContext");
            context = (Context) getSystemContext.invoke(activityThread, (Object[]) null);
            
            Class <?> DisplayManagerGlobal = Class.forName("android.hardware.display.DisplayManagerGlobal");
            Method method = DisplayManagerGlobal.getMethod("getInstance", new Class[0]);
            Object obj = method.invoke(null, new Object[0]);
            method = obj.getClass().getMethod("getRealDisplay", int.class);            
            Display display = (Display)method.invoke(obj,Display.DEFAULT_DISPLAY);
            Point p = new Point();
            display.getRealSize(p);
            Log.i(TAG, "display dimensions: " + p.toString());
            dev_width = p.x;
            dev_height = p.y;
            s_width = dev_width * 2 / 5;
            s_height = dev_height * 2 / 5;

            iwm = IWindowManager.Stub.asInterface(ServiceManager.getService(Context.WINDOW_SERVICE));
            wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            mDisplayManager = (DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE);

//            Log.i(TAG, "systemMain: " + systemMain.toString());
//            Log.i(TAG, "context: " + context);
//            Log.i(TAG, "appcontext: " + context.getApplicationContext());
//            Log.i(TAG, "pkg name: " + context.getPackageName());

//            Field f = context.getClass().getDeclaredField("mPackageInfo");
//            f.setAccessible(true);
//            LoadedApk apk = (LoadedApk) f.get(context);
//            Log.i(TAG, "getClassLoader: " + apk.getClassLoader());

//            Application app = apk.makeApplication(true, null);
//            Log.i(TAG, "app: " + app);
//            Log.i(TAG, "app context: " + app.getApplicationContext());

//            context = context.createPackageContext(packageName,Context.CONTEXT_INCLUDE_CODE + Context.CONTEXT_IGNORE_SECURITY);
//            Log.i(TAG, "friendContext: " + context);
//            Log.i(TAG, "appcontext: " + context.getApplicationContext());
//            Log.i(TAG, "pkg name: " + context.getPackageName());

//            f = context.getClass().getDeclaredField("mPackageInfo");
//            f.setAccessible(true);
//            apk = (LoadedApk) f.get(context);
//            Log.i(TAG, "getClassLoader: " + apk.getClassLoader());
//            app = apk.makeApplication(true, null);
//            Log.i(TAG, "app: " + app);
//            Log.i(TAG, "app context: " + app.getApplicationContext());

//            Class <? > AppGlobals = Class.forName("android.app.AppGlobals");
//            Method getInitialApplication = AppGlobals.getMethod("getInitialApplication");
//            Log.i(TAG, "getInitialApplication: " + getInitialApplication.toString());
//            app = (Application)getInitialApplication.invoke(null);
//            Log.i(TAG, "app: " + app);

//            cb = IClipboard.Stub.asInterface(ServiceManager.getService(Context.CLIPBOARD_SERVICE));
//            cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
//            ClipboardListener clipboardListener = new ClipboardListener();
//            cm.addPrimaryClipChangedListener(clipboardListener);
//            cm.setPrimaryClip(ClipData.newPlainText("text", "hello clipboard"));

        } catch (final Exception e) {
            Log.i(TAG, e.getMessage(), e);
        }
    }
    
    public static int getRotation() {
        return  iwm.getRotation();
    }
    
    static class ClipboardListener implements OnPrimaryClipChangedListener {
        public void onPrimaryClipChanged() {
            // TODO Auto-generated method stub
            ClipData.Item clipItem = cb.getPrimaryClip(packageName).getItemAt(0);
            if(clipItem.getText() != null){
              Log.i(TAG, "clipboard text set: " + clipItem.getText());
            }
        }
    }
    
    public static void getIPAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    String sAddr = addr.getHostAddress();
                    boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
                    if (isIPv4) {
                        log(sAddr + "\n");
                    } else{
                        int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                        log((delim<0 ? sAddr : sAddr.substring(0, delim)) + "\n");
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
    }
    
    public static void log(Object... args){
        String str = Arrays.toString(args);
        System.out.print(str + "\n");
    }
}