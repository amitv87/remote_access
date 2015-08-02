package com.av.remoteaccess;

import java.nio.ByteBuffer;
import org.java_websocket.WebSocket;
import android.graphics.Rect;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.IRotationWatcher;
import android.view.Surface;
import android.view.IRotationWatcher.Stub;

public class MediaCodecStreamer {
    static String TAG = MediaCodecStreamer.class.getCanonicalName();
    public static Boolean isRunning = false;
    public static WebSocket wsSocket = null;

    public static void init(final WebSocket socket) {
        isRunning = false;
        wsSocket = socket;
        MediaCodec encoder;
        MediaFormat encoderInputFormat;
        final int width = Global.s_width;
        final int height = Global.s_height;
        try {
            encoderInputFormat = MediaFormat.createVideoFormat("video/avc", width, height);
            encoderInputFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1 * 1024 * 1024 / 2);
            encoderInputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            encoderInputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1000);
            encoderInputFormat.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 33333);
            encoderInputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            Log.i(TAG, "encoderInputFormat: " + encoderInputFormat.toString());
            final Rect layerStackRect = new Rect(0, 0, Global.dev_width, Global.dev_height);
            final Rect displayRect = new Rect(0, 0, width, height);
            int rotation = Global.getRotation();

            Log.i(TAG, "rotation1 " + rotation);

            encoder = MediaCodec.createEncoderByType(encoderInputFormat.getString(MediaFormat.KEY_MIME));
            encoder.configure(encoderInputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            Surface inputSurface = encoder.createInputSurface();
            
//            VirtualDisplay vd = Global.mDisplayManager.createVirtualDisplay("remotedroid", 384, 682, DisplayMetrics.DENSITY_DEFAULT, inputSurface, 0);

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
                            socket.send("{\"status\":\"orientation\",\"value\":" + rotation + "}");
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
            while (isRunning) {
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                int outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 100);
                if (outputBufferIndex >= 0) {
                    ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                    byte[] outData = new byte[bufferInfo.size];
                    outputBuffer.get(outData);
                    if (socket.isOpen()) {
                        try {
                            socket.send(outData);
                        } catch (Exception e) {
                            isRunning = false;
                            Log.i(TAG, e.getMessage(), e);
                        }
                    } else
                        isRunning = false;
//                    outputBuffer.position(bufferInfo.offset);
                    encoder.releaseOutputBuffer(outputBufferIndex, false);
                }
            }
            Global.iwm.removeRotationWatcher((IRotationWatcher) mRotationWatcher);
            Reflection.openTransaction.invoke(null, new Object[0]);
            Reflection.destroyDisplay.invoke(null, iBinder);
            Reflection.closeTransaction.invoke(null, new Object[0]);
            encoder.stop();
            inputSurface.release();
            encoder.release();
            encoder = null;
        } catch (Exception e) {
            Log.i(TAG, e.getMessage(), e);
        }
    }

    public static Rect getRect(Rect rect, int rotation) {
        if (rotation == 1 || rotation == 3) {
            return new Rect(0, 0, rect.bottom, rect.right);
        }
        return rect;
    }
}