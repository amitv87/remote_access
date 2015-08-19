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
import android.util.Log;

public class AudioServer extends WebSocketServer {
    private static final String TAG = AudioServer.class.getCanonicalName();
    private static AudioServer as;
    
    public AudioServer( int port , Draft d ) throws UnknownHostException {
        super( new InetSocketAddress( port ), Collections.singletonList( d ) );
    }
    
    public AudioServer( InetSocketAddress address, Draft d ) {
        super( address, Collections.singletonList( d ) );
    }

    @Override
    public void onOpen( final WebSocket conn, ClientHandshake handshake ) {
        AudioCapturer.getInstance().stop();
        AudioCapturer.getInstance().start(conn);
    }

    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
        AudioCapturer.getInstance().stop();
    }

    @Override
    public void onError( WebSocket conn, Exception ex ) {
        ex.printStackTrace();
    }

    @Override
    public void onMessage( WebSocket conn, String message ) {
        
    }

    @Override
    public void onMessage( WebSocket conn, ByteBuffer blob ) {
        
    }

    @Override
    public void onWebsocketMessageFragment( WebSocket conn, Framedata frame ) {
        
    }

    public static void init(int port) {
        WebSocketImpl.DEBUG = false;
        if(as == null){
            Log.i(TAG, "starting audio server on port: " + port);
            try {
                as = new AudioServer( port, new Draft_17() );
                as.start();
            } catch (UnknownHostException e) {
                Log.e(TAG, "", e);
            }
        }
    }
    
    public static void dispose() {
        if(as != null){
            Log.i(TAG, "stopping audio server");
            try {
                as.stop();
                AudioCapturer.getInstance().stop();
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
        }
        as = null;
    }
}
