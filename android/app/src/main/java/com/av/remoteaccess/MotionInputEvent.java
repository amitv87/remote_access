package com.av.remoteaccess;

import org.json.JSONArray;
import android.util.Log;
import android.view.IRotationWatcher;
import android.view.IWindowManager;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.os.Handler;
import android.os.SystemClock;

public class MotionInputEvent {
    private static final String TAG = MotionInputEvent.class.getCanonicalName();
    
    public static void Dispatch(final JSONArray touchArray){
        try {
            sendMotionEvent(
                    touchArray.getInt(0),
                    ((Double)(touchArray.getDouble(1) * Global.dev_width)).intValue(),
                    ((Double)(touchArray.getDouble(2) * Global.dev_height)).intValue()
                    );
            
      } catch (Exception e) {
          Log.i(TAG, "error in dispatching motion event",e);
      }
    }
    
    private static void sendMotionEvent(int down, int x, int y) {
        int ACTION = MotionEvent.ACTION_CANCEL;
        switch(down){
            case 2: ACTION = MotionEvent.ACTION_MOVE;break;
            case 1: ACTION = MotionEvent.ACTION_DOWN;break;
            case 0: ACTION = MotionEvent.ACTION_UP;break;
        }

        MotionEvent.PointerProperties pp = new MotionEvent.PointerProperties();
        pp.toolType = MotionEvent.TOOL_TYPE_FINGER;
        pp.id = 0;
        MotionEvent.PointerProperties[] pps = new MotionEvent.PointerProperties[]{pp};

        MotionEvent.PointerCoords pc = new MotionEvent.PointerCoords();
        pc.size = 1;
        pc.pressure = 1;
        pc.x = x;
        pc.y = y;
        MotionEvent.PointerCoords[] pcs = new MotionEvent.PointerCoords[]{pc};

        MotionEvent motionEvent = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                ACTION,
                pps.length,
                pps,
                pcs,
                0,
                0,
                1,
                1,
                5,
                0,
                InputDevice.SOURCE_TOUCHSCREEN,
                0
        );

//        MotionEvent motionEvent = MotionEvent.obtain(
//            SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),
//            ACTION,
//            x, y,
//            down != 0 ? 1 : 0,    //pressure
//            1,      //size
//            0,      //meta state
//            1,      //x precision
//            1,      //y precision
//            0,      //device id
//            0);     //edge flags
//        motionEvent.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        InputManager.DispatchEvent(motionEvent);
    }
}
