package com.av.remoteaccess;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.locks.ReentrantLock;
import java.io.BufferedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import org.java_websocket.WebSocket;

public class AudioCapturer{
    static final String TAG = AudioCapturer.class.getCanonicalName();
    static final int channels  = 2;
    static final int sampleSize = 16;
    static final int sampleRate = 48000;
    static final int CALLBACK_BUFFER_SIZE_MS = 10;
    static final int bytesPerFrame = 1 * channels * (sampleSize / 8);
    static final int BUFFERS_PER_SECOND = 1000 / CALLBACK_BUFFER_SIZE_MS;
    static final int framesPerBuffer = sampleRate / BUFFERS_PER_SECOND;
    static final int rawBytesLength = bytesPerFrame * framesPerBuffer;
    
    static final int LAME_MAXALBUMART = (128 * 1024);
    static final int LAME_MAXMP3BUFFER = (16384 + LAME_MAXALBUMART);
    
    byte[] outBytes = null;
    boolean isRunning = false;
    BufferedOutputStream bos = null;
    Handler hEnc = null;
    Thread encThread = null, capThread = null;
    AudioRecord audioRecord = null;
    private final ReentrantLock _recLock = new ReentrantLock();
    WebSocket ws = null;
    byte mp3Bytes[] = new byte[LAME_MAXMP3BUFFER];
    
    static AudioCapturer ac = null;
    private AudioCapturer(){
        outBytes = new byte[rawBytesLength];
        int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        int recBufSize = minBufferSize * 1;
        audioRecord = new AudioRecord(AudioSource.REMOTE_SUBMIX, sampleRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, recBufSize);
        LibMP3Lame.init(sampleRate, channels, sampleRate, 128, 7);
        encThread = new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                hEnc = new Handler(Looper.myLooper()){
                    public void handleMessage(Message msg) {
                        byte[] pcm  = (byte[])msg.obj;
                        short[] shorts = new short[pcm.length/2]; 
                        ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
//                        Log.i(TAG, "msg received, num bytes: " + pcm.length);
                        int bytesLength = LibMP3Lame.encode(shorts, shorts, shorts.length, mp3Bytes);
//                        Log.i(TAG, "mp3Bytes: " + bytesLength);
                        try{
                            if(bytesLength > 0){ 
                                byte[] b = Arrays.copyOfRange(mp3Bytes, 0, bytesLength);
                                if(ws != null && ws.isOpen()){
                                    ws.send(b);
                                }
                                else{
                                    isRunning = false;
                                }
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                };
                Looper.loop();
            }
        });
        encThread.start();
    }

    public static AudioCapturer getInstance(){
        if(ac == null)
            ac = new AudioCapturer();
        return ac;
    }
    
    public void start(final WebSocket socket) {
        capThread = new Thread(new Runnable() {
            public void run() {
                _recLock.lock();
                ws = socket;
                try {
                    android.os.Process.setThreadPriority(
                        android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                } catch (Exception e) {
                    Log.e(TAG, "Set rec thread priority failed: " + e.getMessage());
                }
                try{
                    audioRecord.startRecording();
                    isRunning = true;
                    while (isRunning) {
                        int bytesRead = audioRecord.read(outBytes, 0, rawBytesLength);
//                        Log.i(TAG, "bytesRead: " + bytesRead);
                        if (bytesRead == rawBytesLength && hEnc != null) {
                            Message msg = hEnc.obtainMessage();
                            msg.obj = outBytes; 
                            msg.sendToTarget();
                        } else {
                            Log.e(TAG,"AudioRecord.read failed: " + bytesRead);
                          if (bytesRead == AudioRecord.ERROR_INVALID_OPERATION) {
                              Log.i(TAG, "invalid operation");
                              break;
                          }
                        }
                        try {
                            Thread.sleep(CALLBACK_BUFFER_SIZE_MS);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    audioRecord.stop();
                }
                catch(Exception e){
                    Log.e(TAG, e.getMessage(), e);
                }
                finally{
                    if(audioRecord != null && audioRecord.getState() == AudioRecord.RECORDSTATE_RECORDING)
                        audioRecord.stop();
                    if(ws.isOpen())
                        ws.send("kick");
                    _recLock.unlock();
                }
            }
        });
        capThread.start();
    }
    
    public void stop() {
        isRunning = false;
    }
}