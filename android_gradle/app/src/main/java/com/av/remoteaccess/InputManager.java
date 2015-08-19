package com.av.remoteaccess;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import android.hardware.input.IInputManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.InputEvent;

public class InputManager {
    private static final String TAG = InputManager.class.getCanonicalName(); 
    private static IInputManager iim;
    private static Handler mainHandler;

    private static void initInputManager() {
        try{
            Class <?> class1 = Class.forName("android.hardware.input.InputManager");
            Method method = class1.getMethod("getInstance", new Class[0]);
            Field field = class1.getDeclaredField("mIm");
            field.setAccessible(true);
            iim = (IInputManager) field.get(method.invoke(null, new Object[0]));
        } catch (Exception e){
            Log.i(TAG, "error getting default instance", e);
        }
        new Thread(new Runnable() {
            public void run() {
                if (Looper.myLooper() == null)
                    Looper.prepare();
                mainHandler = new Handler(Looper.myLooper());
                Looper.loop();
            }
        }).start();
    }
    
    public static void init(){
        if(iim == null){
            initInputManager();
            KeyInputEvent.init();
            MotionInputEvent.init();
        }
    }
    
    public static IInputManager getIInputManager(){
        if(iim == null){
            initInputManager();
        }
        return iim;
    }
    
    public static void DispatchEvent(final InputEvent inputEvent){
        mainHandler.post(new Runnable() {
            public void run() {
                iim.injectInputEvent(inputEvent, 0);
            }
        });
    }
}
