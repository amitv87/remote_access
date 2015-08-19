package com.av.remoteaccess;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collections;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class VideoServer extends WebSocketServer {
    private static final String TAG = VideoServer.class.getCanonicalName();
    private static VideoServer vs;
    
    public VideoServer( int port , Draft d ) throws UnknownHostException {
        super( new InetSocketAddress( port ), Collections.singletonList( d ) );
    }
    
    public VideoServer( InetSocketAddress address, Draft d ) {
        super( address, Collections.singletonList( d ) );
    }

    @Override
    public void onOpen( final WebSocket conn, ClientHandshake handshake ) {
        try{
            JSONObject json = new JSONObject();
            json.put("status", "config");
            json.put("width", Global.s_width);
            json.put("height", Global.s_height);
            json.put("d_width", Global.dev_width);
            json.put("d_height", Global.dev_height);
            json.put("orientation", Global.getRotation()); 
            json.put("platform", "android");
            json.put("api_level", android.os.Build.VERSION.SDK_INT);
            conn.send(json.toString());
        } catch (Exception e){
            Log.e(TAG, "", e);
        }
        VideoCapturer.getInstance().stop();
        VideoCapturer.getInstance().start(conn);
    }

    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
        VideoCapturer.getInstance().stop();
    }

    @Override
    public void onError( WebSocket conn, Exception ex ) {
        Log.e(TAG, "", ex);
    }

    @Override
    public void onMessage( WebSocket conn, String message ) {
        try{
            Object object = new JSONTokener(message).nextValue();

            if (object instanceof JSONArray){
                JSONArray arr = (JSONArray)object;
                if(arr.length() == 3)
                    MotionInputEvent.Dispatch(arr);
                else if (arr.length() == 5)
                    KeyInputEvent.Dispatch(arr);
                else if (arr.length() == 1)
                    KeyInputEvent.SendSpecial(arr);
            }
            else if (object instanceof JSONObject){
                JSONObject json = (JSONObject)object;
                if(json.has("action")){
                    String action = json.getString("action");
                    if(action.equals("set_clip"))
                        Global.setClip(json.getString("value"));
                    else if(action.equals("get_clip")){
                        JSONObject ret = new JSONObject();
                        ret.put("status", "clip");
                        ret.put("value", Global.getClip());
                        conn.send(ret.toString());
                    }
                }
            }
                //you have an array
        } catch (Exception e){
            Log.e(TAG, "",e);
        }
    }

    @Override
    public void onMessage( WebSocket conn, ByteBuffer blob ) {
        
    }

    @Override
    public void onWebsocketMessageFragment( WebSocket conn, Framedata frame ) {
        
    }

    public static void init(int port) {
        WebSocketImpl.DEBUG = false;
        if(vs == null){
            Log.i(TAG, "starting video server on port: " + port);
            try {
                vs = new VideoServer( port, new Draft_17() );
                vs.start();
            } catch (UnknownHostException e) {
                Log.e(TAG, "", e);
            }
        }
    }
    
    public static void dispose() {
        if(vs != null){
            Log.i(TAG, "stopping video server");
            try {
                vs.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        vs = null;
    }
}
