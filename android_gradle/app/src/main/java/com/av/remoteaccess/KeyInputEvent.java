package com.av.remoteaccess;

import java.util.HashMap;
import org.json.JSONArray;
import android.os.SystemClock;
import android.util.Log;
import android.view.IWindowManager;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

public class KeyInputEvent {
    private static String TAG = KeyInputEvent.class.getCanonicalName();
    private static HashMap<String, Integer> actionMap;
    private static HashMap<Integer, Integer> specialKeyMap;
    static IWindowManager iwm;
    
    public static void init() {
        iwm = Global.iwm;
        actionMap = new HashMap<String, Integer>();
        actionMap.put("home", KeyEvent.KEYCODE_HOME);
        actionMap.put("menu", KeyEvent.KEYCODE_MENU);
        actionMap.put("search", KeyEvent.KEYCODE_SEARCH);
        actionMap.put("back", KeyEvent.KEYCODE_BACK);
        actionMap.put("recentapps", KeyEvent.KEYCODE_APP_SWITCH);
        actionMap.put("power", KeyEvent.KEYCODE_POWER);

        specialKeyMap = new HashMap<Integer, Integer>();
        specialKeyMap.put(32, KeyEvent.KEYCODE_SPACE);
        specialKeyMap.put(8, KeyEvent.KEYCODE_DEL);
        specialKeyMap.put(13, KeyEvent.KEYCODE_ENTER);
        specialKeyMap.put(27, KeyEvent.KEYCODE_ESCAPE);
        specialKeyMap.put(38, KeyEvent.KEYCODE_DPAD_UP);
        specialKeyMap.put(40, KeyEvent.KEYCODE_DPAD_DOWN);
        specialKeyMap.put(37, KeyEvent.KEYCODE_DPAD_LEFT);
        specialKeyMap.put(39, KeyEvent.KEYCODE_DPAD_RIGHT);
        specialKeyMap.put(9, KeyEvent.KEYCODE_TAB);
        specialKeyMap.put(16, KeyEvent.KEYCODE_SHIFT_LEFT);
        specialKeyMap.put(17, KeyEvent.KEYCODE_CTRL_LEFT);
        specialKeyMap.put(91, KeyEvent.KEYCODE_CTRL_LEFT);
        specialKeyMap.put(18, KeyEvent.KEYCODE_ALT_LEFT);
        
        specialKeyMap.put(48, KeyEvent.KEYCODE_0);
        specialKeyMap.put(49, KeyEvent.KEYCODE_1);
        specialKeyMap.put(50, KeyEvent.KEYCODE_2);
        specialKeyMap.put(51, KeyEvent.KEYCODE_3);
        specialKeyMap.put(52, KeyEvent.KEYCODE_4);
        specialKeyMap.put(53, KeyEvent.KEYCODE_5);
        specialKeyMap.put(54, KeyEvent.KEYCODE_6);
        specialKeyMap.put(55, KeyEvent.KEYCODE_7);
        specialKeyMap.put(56, KeyEvent.KEYCODE_8);
        specialKeyMap.put(57, KeyEvent.KEYCODE_9);
        
        specialKeyMap.put(188, KeyEvent.KEYCODE_COMMA);
        specialKeyMap.put(190, KeyEvent.KEYCODE_PERIOD);
        specialKeyMap.put(191, KeyEvent.KEYCODE_SLASH);
        specialKeyMap.put(192, KeyEvent.KEYCODE_GRAVE);
        specialKeyMap.put(219, KeyEvent.KEYCODE_LEFT_BRACKET);
        specialKeyMap.put(220, KeyEvent.KEYCODE_BACKSLASH);
        specialKeyMap.put(221, KeyEvent.KEYCODE_RIGHT_BRACKET);
        specialKeyMap.put(222, KeyEvent.KEYCODE_APOSTROPHE);
        specialKeyMap.put(173, KeyEvent.KEYCODE_VOLUME_MUTE);
        specialKeyMap.put(174, KeyEvent.KEYCODE_VOLUME_DOWN);
        specialKeyMap.put(175, KeyEvent.KEYCODE_VOLUME_UP);
        specialKeyMap.put(186, KeyEvent.KEYCODE_SEMICOLON);
        specialKeyMap.put(187, KeyEvent.KEYCODE_EQUALS);
        specialKeyMap.put(189, KeyEvent.KEYCODE_MINUS);
    }
    
    public static void Dispatch(final JSONArray arr){
        try {
          int keyCode = arr.getInt(1);
          KeyEvent keyEvent = new KeyEvent(
                  SystemClock.uptimeMillis(),
                  SystemClock.uptimeMillis(),
                  arr.getInt(0) == 1 ? KeyEvent.ACTION_DOWN : KeyEvent.ACTION_UP,
                  specialKeyMap.containsKey(keyCode) ? specialKeyMap.get(keyCode) : keyCode - 36,
                  0,
                  (arr.getInt(2) == 1 ? KeyEvent.META_ALT_ON : 0) +
                  (arr.getInt(3) == 1? KeyEvent.META_SHIFT_ON : 0) +
                  (arr.getInt(4) == 1? KeyEvent.META_CTRL_ON : 0),
                  KeyCharacterMap.VIRTUAL_KEYBOARD,
                  0,
                  0,
                  InputDevice.SOURCE_KEYBOARD);
          InputManager.DispatchEvent(keyEvent);
      } catch (Exception e) {
          Log.i(TAG, "error in dispatching key event",e);
      }
    }
    
    public static void SendSpecial(final JSONArray arr){
        try {
            String action = arr.optString(0);
            Log.i(TAG,action);
            if(actionMap.containsKey(action)){
              int keyCode = actionMap.get(arr.getString(0));
              
              KeyEvent keyEvent = new KeyEvent(
                      SystemClock.uptimeMillis(),
                      SystemClock.uptimeMillis(),
                      KeyEvent.ACTION_DOWN,
                      keyCode,
                      0,
                      0,
                      KeyCharacterMap.VIRTUAL_KEYBOARD,
                      0,
                      0,
                      InputDevice.SOURCE_KEYBOARD);
              InputManager.DispatchEvent(keyEvent);
              keyEvent = new KeyEvent(
                      SystemClock.uptimeMillis(),
                      SystemClock.uptimeMillis(),
                      KeyEvent.ACTION_UP,
                      keyCode,
                      0,
                      0,
                      KeyCharacterMap.VIRTUAL_KEYBOARD,
                      0,
                      0,
                      InputDevice.SOURCE_KEYBOARD);
              InputManager.DispatchEvent(keyEvent);
            }
            else if(action.equals("landscape")){
                iwm.freezeRotation(3);
            }
            else if(action.equals("portrait")){
                iwm.freezeRotation(0);
            }
        } catch (Exception e) {
            Log.i(TAG, "error in dispatching key event",e);
        }
    }
}