package com.av.remoteaccess;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder.AudioSource;
import android.os.Handler;
import android.util.Log;

import java.util.concurrent.locks.ReentrantLock;
import java.io.BufferedOutputStream;
import java.nio.ByteBuffer;
import org.java_websocket.WebSocket;

public class AudioCapturer{
    static final String TAG = AudioCapturer.class.getCanonicalName();
    static final int channels  = 2;
    static final int sampleSize = 16;
    static final int sampleRate = 48000;
    static final int CALLBACK_BUFFER_SIZE_MS = 10;
    static final int bytesPerFrame = channels * (sampleSize / 8);
    static final int BUFFERS_PER_SECOND = 1000 / CALLBACK_BUFFER_SIZE_MS;
    static final int framesPerBuffer = sampleRate / BUFFERS_PER_SECOND;
    static final int rawBytesLength = bytesPerFrame * framesPerBuffer;

    boolean isRunning = false;
    BufferedOutputStream bos = null;
    Handler hEnc = null;
    Thread encThread = null, capThread = null;
    AudioRecord audioRecord = null;
    MediaCodec mMediaCodec = null;
    MediaFormat format = null;
    private final ReentrantLock _recLock = new ReentrantLock();
    WebSocket ws = null;

    static AudioCapturer ac = null;
    private AudioCapturer(){
        int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        int recBufSize = minBufferSize * 2;
        audioRecord = new AudioRecord(
                AudioSource.REMOTE_SUBMIX,
                sampleRate,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                recBufSize);

        format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
        format.setInteger(MediaFormat.KEY_BIT_RATE, 128 * 1024);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, recBufSize);
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
                    mMediaCodec = MediaCodec.createEncoderByType("audio/mp4a-latm");
                    mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                    audioRecord.startRecording();
                    mMediaCodec.start();
                    isRunning = true;

                    final ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
                    final ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
                    Thread dequeue = new Thread(new Runnable() {
                        public void run() {
                            try {
                                while (isRunning) {
                                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                                    int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 100);
                                    if (outputBufferIndex >= 0) {
                                        ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                                        int outPacketSize = bufferInfo.size + 7; // 7 is ADTS size
                                        byte[] outData = new byte[outPacketSize];
                                        addADTStoPacket(outData, outPacketSize);
                                        outputBuffer.get(outData, 7, bufferInfo.size);
                                        outputBuffer.position(bufferInfo.offset);
                                        if (ws != null && ws.isOpen()) {
                                            try {
                                                ws.send(outData);
                                            } catch (Exception e) {
                                                isRunning = false;
                                                Log.e(TAG, e.getMessage(), e);
                                            }
                                        } else
                                            isRunning = false;
                                        mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                                    }
                                }
                            }
                            catch(Exception e){
                                Log.e(TAG, e.getMessage(), e);
                            }
                        }
                    });
                    dequeue.start();
                    int len = 0, bufferIndex = 0;
                    while (isRunning) {
                        bufferIndex = mMediaCodec.dequeueInputBuffer(CALLBACK_BUFFER_SIZE_MS * 100);
                        if (bufferIndex>=0) {
                            inputBuffers[bufferIndex].clear();
                            len = audioRecord.read(inputBuffers[bufferIndex], rawBytesLength);
                            if (len ==  AudioRecord.ERROR_INVALID_OPERATION || len == AudioRecord.ERROR_BAD_VALUE) {
                                Log.e(TAG,"An error occured with the AudioRecord API !");
                            } else {
                                mMediaCodec.queueInputBuffer(bufferIndex, 0, len, System.nanoTime()/1000, 0);
                            }
                        }
                    }
                    audioRecord.stop();
                    mMediaCodec.stop();
                    mMediaCodec.release();
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

    private void addADTStoPacket(byte[] packet, int packetLen)
    {
        int profile = 2; // 2=AAC-LC
        int freqIdx = determineSamplingRateKey(sampleRate);
        int chanCfg = channels;

        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    private int determineSamplingRateKey(int samplingRate)
    {
        switch (samplingRate)
        {
        case 96000:
            return 0;
        case 88200:
            return 1;
        case 64000:
            return 2;
        case 48000:
            return 3;
        case 44100:
            return 4;
        case 32000:
            return 5;
        case 24000:
            return 6;
        case 22050:
            return 7;
        case 16000:
            return 8;
        case 12000:
            return 9;
        case 11025:
            return 10;
        case 8000:
            return 11;
        case 7350:
            return 12;
        default:
            return 4;
        }
    }
}