package com.av.remoteaccess;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import org.java_websocket.WebSocket;
import android.graphics.Rect;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.IRotationWatcher;
import android.view.Surface;
import android.view.IRotationWatcher.Stub;

public class VideoCapturer {
    static final String TAG = VideoCapturer.class.getCanonicalName();

    boolean isRunning = false;
    private final ReentrantLock _recLock = new ReentrantLock();
    private static VideoCapturer vc = null;
    
    public static VideoCapturer getInstance(){
        if (vc == null)
            vc = new VideoCapturer();
        return vc;
    }
    
    private VideoCapturer(){
        
    }
    
    public void start(final WebSocket ws){
        Thread enc = new Thread(new Runnable() {
            public void run() {
                _recLock.lock();
                final int width = Global.s_width;
                final int height = Global.s_height;
                try {
                    MediaFormat encoderInputFormat = MediaFormat.createVideoFormat("video/avc", width, height);
                    encoderInputFormat.setInteger(MediaFormat.KEY_BIT_RATE, 2 * 1024 * 1024 / 2);
                    encoderInputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
                    encoderInputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1000);
                    encoderInputFormat.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 33333);
                    encoderInputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
                    Log.i(TAG, "encoderInputFormat: " + encoderInputFormat.toString());
                    final Rect layerStackRect = new Rect(0, 0, Global.dev_width, Global.dev_height);
                    final Rect displayRect = new Rect(0, 0, width, height);
                    int rotation = Global.getRotation();

                    final MediaCodec encoder = MediaCodec.createEncoderByType(encoderInputFormat.getString(MediaFormat.KEY_MIME));
                    encoder.configure(encoderInputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                    Surface inputSurface = encoder.createInputSurface();

                    final IBinder iBinder = (IBinder) Reflection.createDisplay.invoke(null, "rm", false);
                    Reflection.openTransaction.invoke(null, new Object[0]);
                    Reflection.setDisplaySurface.invoke(null, iBinder, inputSurface);
                    Reflection.setDisplayProjection.invoke(null, iBinder, rotation, getRect(layerStackRect, rotation), getRect(displayRect, rotation));
                    Reflection.setDisplayLayerStack.invoke(null, iBinder, 0);
                    Reflection.closeTransaction.invoke(null, new Object[0]);

                    Stub mRotationWatcher = new IRotationWatcher.Stub() {
                        public void onRotationChanged(int rotation) {
                            Log.i(TAG, "rotation2 " + rotation);
                            try {
                                if(isRunning){
                                    Reflection.openTransaction.invoke(null, new Object[0]);
                                    Reflection.setDisplayProjection.invoke(null, iBinder, rotation, getRect(layerStackRect, rotation), getRect(displayRect, rotation));
                                    Reflection.closeTransaction.invoke(null, new Object[0]);
                                    ws.send("{\"status\":\"orientation\",\"value\":" + rotation + "}");
                                }
                            } catch (Exception e) {
                                Log.i(TAG, e.getMessage(), e);
                            }
                        }
                    };
                    Global.iwm.watchRotation((IRotationWatcher) mRotationWatcher);

                    encoder.start();
                    ByteBuffer[] outputBuffers = encoder.getOutputBuffers();
                    isRunning = true;
                    
                    new Thread(new Runnable() {
                        public void run() {
                            int i = 0;
                            while(isRunning && i < 10){
                                try {
                                    Bundle b = new Bundle();
                                    b.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
                                    if(ws.isOpen())
                                        encoder.setParameters(b);
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                    Log.e(TAG, e.getMessage(), e);
                                    break;
                                }
                                i++;
                            }
                        }
                    }).start();
                    
                    try {
                        while (isRunning) {
                            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                            int outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 100);
                            if (outputBufferIndex >= 0) {
                                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                                byte[] outData = new byte[bufferInfo.size];
                                outputBuffer.get(outData);
                                if (ws.isOpen()) {
                                    try {
                                        ws.send(outData);
                                    } catch (Exception e) {
                                        isRunning = false;
                                        Log.e(TAG, e.getMessage(), e);
                                    }
                                } else
                                    isRunning = false;
                                encoder.releaseOutputBuffer(outputBufferIndex, false);
                            }
                        } 
                    }
                    catch (Exception e){
                        Log.e(TAG, e.getMessage(), e);
                    }
                    Global.iwm.removeRotationWatcher((IRotationWatcher) mRotationWatcher);
                    Reflection.openTransaction.invoke(null, new Object[0]);
                    Reflection.destroyDisplay.invoke(null, iBinder);
                    Reflection.closeTransaction.invoke(null, new Object[0]);
                    encoder.stop();
                    inputSurface.release();
                    encoder.release();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                } finally{
                    if(ws.isOpen())
                        ws.send("kick");
                    _recLock.unlock();
                }
            }
        });
        enc.start();
    }
    
    public void stop(){
        isRunning = false;
    }

    public static Rect getRect(Rect rect, int rotation) {
        if (rotation == 1 || rotation == 3) {
            return new Rect(0, 0, rect.bottom, rect.right);
        }
        return rect;
    }
}