package com.av.remoteaccess;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collections;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.framing.FrameBuilder;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import android.util.Log;

public class StreamServer extends WebSocketServer {
    static final String TAG = StreamServer.class.getCanonicalName();
//    private int counter = 0;
    private static StreamServer streamServer;
//    private HashMap<WebSocket, Integer> conns = new HashMap<WebSocket, Integer>();
    
    public StreamServer( int port , Draft d ) throws UnknownHostException {
        super( new InetSocketAddress( port ), Collections.singletonList( d ) );
    }
    
    public StreamServer( InetSocketAddress address, Draft d ) {
        super( address, Collections.singletonList( d ) );
    }

    @Override
    public void onOpen( final WebSocket conn, ClientHandshake handshake ) {
        new Thread(new Runnable() {
            public void run() {
//                final WebSocket socket = conn;
                try {
                    MediaCodecStreamer.init(conn);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
//        conns.remove(conn);
        MediaCodecStreamer.isRunning = false;
    }

    @Override
    public void onError( WebSocket conn, Exception ex ) {
        ex.printStackTrace();
    }

    @Override
    public void onMessage( WebSocket conn, String message ) {
//        conn.send( message );
    }

    @Override
    public void onMessage( WebSocket conn, ByteBuffer blob ) {
        conn.send( blob );
    }

    @Override
    public void onWebsocketMessageFragment( WebSocket conn, Framedata frame ) {
        FrameBuilder builder = (FrameBuilder) frame;
        builder.setTransferemasked( false );
        conn.sendFrame( frame );
    }

    public static void init(int port) {
        WebSocketImpl.DEBUG = false;
        Log.i(TAG, "hello ws");
        if(streamServer != null) return;
        
        try {
            streamServer = new StreamServer( port, new Draft_17() );
            streamServer.start();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}