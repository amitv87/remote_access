package com.av.remoteaccess;

import android.view.IWindowManager;
import android.view.Surface;

public class RotationManager {
    private IWindowManager iwm;
    private static RotationManager rm;

    private RotationManager (){
        iwm = Global.iwm;
    }

    public static void init(){
        if(rm == null)
            rm = new RotationManager();
    }

    public static void setOrientation(int angle){
        int _angle = Surface.ROTATION_0;
        switch (angle){
            case 0:
                _angle = Surface.ROTATION_0;
                break;
            case 90:
                _angle = Surface.ROTATION_90;
                break;
            case 180:
                _angle = Surface.ROTATION_180;
                break;
            case 270:
                _angle = Surface.ROTATION_270;
                break;
        }
        rm.iwm.freezeRotation(_angle);
    }

    public static int getRotation() {
        return  rm.iwm.getRotation();
    }

    public static int getRotationAngle(){
        int rotation = rm.iwm.getRotation();
        return getRotationAngle(rotation);
    }

    public static int getRotationAngle(int rotation){
        int angle = 0;
        switch (rotation){
            case Surface.ROTATION_0:
                angle = 0;
                break;
            case Surface.ROTATION_90:
                angle = 90;
                break;
            case Surface.ROTATION_180:
                angle = 180;
                break;
            case Surface.ROTATION_270:
                angle = 270;
                break;
        }
        return  angle;
    }
}
