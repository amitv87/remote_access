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
    private HashMap<String, Integer> actionMap;
    private HashMap<Integer, Integer> specialKeyMap;
    private int deviceId = KeyCharacterMap.VIRTUAL_KEYBOARD;

    private static KeyInputEvent kv;
    public static void init(){
        if(kv == null)
            kv = new KeyInputEvent();
    }

    private KeyInputEvent() {
        actionMap = new HashMap<String, Integer>();
        actionMap.put("home", KeyEvent.KEYCODE_HOME);
        actionMap.put("menu", KeyEvent.KEYCODE_MENU);
        actionMap.put("search", KeyEvent.KEYCODE_SEARCH);
        actionMap.put("back", KeyEvent.KEYCODE_BACK);
        actionMap.put("recentapps", KeyEvent.KEYCODE_APP_SWITCH);
        actionMap.put("power", KeyEvent.KEYCODE_POWER);
        actionMap.put("vol+", KeyEvent.KEYCODE_VOLUME_UP);
        actionMap.put("vol-", KeyEvent.KEYCODE_VOLUME_DOWN);
        actionMap.put("vol0", KeyEvent.KEYCODE_VOLUME_MUTE);
        actionMap.put("play", KeyEvent.KEYCODE_MEDIA_PLAY);
        actionMap.put("pause", KeyEvent.KEYCODE_MEDIA_PAUSE);
        actionMap.put("next", KeyEvent.KEYCODE_MEDIA_NEXT);
        actionMap.put("prev", KeyEvent.KEYCODE_MEDIA_PREVIOUS);

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

        try {
            deviceId = KeyCharacterMap.class.getDeclaredField("VIRTUAL_KEYBOARD").getInt(KeyCharacterMap.class);
        }
        catch (Exception e) {
            deviceId = 0;
        }
    }
    
    public static void Dispatch(final JSONArray arr){
        try {
            int keyCode = arr.getInt(1);
            keyCode = kv.specialKeyMap.containsKey(keyCode) ? kv.specialKeyMap.get(keyCode) : keyCode - 36;
            sendKey(
                keyCode,
                arr.getInt(0) == 1 ? KeyEvent.ACTION_DOWN : KeyEvent.ACTION_UP,
                (arr.getInt(2) == 1 ? KeyEvent.META_ALT_ON : 0) +
                (arr.getInt(3) == 1 ? KeyEvent.META_SHIFT_ON : 0) +
                (arr.getInt(4) == 1 ? KeyEvent.META_CTRL_ON : 0)
            );
      } catch (Exception e) {
          Log.i(TAG, "error in dispatching key event",e);
      }
    }
    
    public static void SendSpecial(final JSONArray arr){
        try {
            String action = arr.optString(0);
            Log.i(TAG, action);
            if(kv.actionMap.containsKey(action)){
                int keyCode = kv.actionMap.get(arr.getString(0));
                sendKey(keyCode, KeyEvent.ACTION_DOWN, 0);
                sendKey(keyCode, KeyEvent.ACTION_UP, 0);
            }
        } catch (Exception e) {
            Log.i(TAG, "error in dispatching key event",e);
        }
    }

    private static void sendKey(int keyCode, int action, int meta){
        KeyEvent keyEvent = new KeyEvent(
            SystemClock.uptimeMillis(),
            SystemClock.uptimeMillis(),
            action,
            keyCode,
            0,
            meta,
            kv.deviceId,
            0,
            KeyEvent.FLAG_FROM_SYSTEM,
            InputDevice.SOURCE_KEYBOARD);
        InputManager.DispatchEvent(keyEvent);
    }
}
