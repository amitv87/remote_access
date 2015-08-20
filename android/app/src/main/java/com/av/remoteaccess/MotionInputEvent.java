package com.av.remoteaccess;

import org.json.JSONArray;
import android.util.Log;
import android.view.IRotationWatcher;
import android.view.IWindowManager;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.os.Handler;
import android.os.SystemClock;

public class MotionInputEvent extends IRotationWatcher.Stub {
    private static final String TAG = MotionInputEvent.class.getCanonicalName();
    private static MotionInputEvent mie;

    @Override
    public void onRotationChanged(int rotation){
        Log.i("hello rotation", "hi " + rotation);
    }
    
    private MotionInputEvent(){}
    
    public static void init() {
        if(mie == null){
            mie = new MotionInputEvent();
        }
    }
    
    public static void Dispatch(final JSONArray touchArray){
        try {
            mie.sendMotionEvent(
                    touchArray.getInt(0),
                    ((Double)(touchArray.getDouble(1) * Global.dev_width)).intValue(),
                    ((Double)(touchArray.getDouble(2) * Global.dev_height)).intValue()
                    );
            
      } catch (Exception e) {
          Log.i(TAG, "error in dispatching motion event",e);
      }
    }
    
    private void sendMotionEvent(int down, int x, int y) {
        int ACTION = MotionEvent.ACTION_CANCEL;
        switch(down){
            case 2: ACTION = MotionEvent.ACTION_MOVE;break;
            case 1: ACTION = MotionEvent.ACTION_DOWN;break;
            case 0: ACTION = MotionEvent.ACTION_UP;break;
        }
        MotionEvent motionEvent = MotionEvent.obtain(
            SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),
            ACTION,
            x, y,
            down != 0 ? 1 : 0,    //pressure
            1,      //size
            0,      //meta state
            1,      //x precision
            1,      //y precision
            0,      //device id
            0);     //edge flags
        motionEvent.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        InputManager.DispatchEvent(motionEvent);
    }
}
