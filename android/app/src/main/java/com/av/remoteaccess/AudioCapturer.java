package com.av.remoteaccess;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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


    private MediaRecorder recorder = null;
    ParcelFileDescriptor[] pipe;

    static AudioCapturer ac = null;
    private AudioCapturer(){
        try {
            pipe = ParcelFileDescriptor.createPipe();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
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

    public void start_audioRecord(final WebSocket socket) {
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
                        bufferIndex = mMediaCodec.dequeueInputBuffer(CALLBACK_BUFFER_SIZE_MS);
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

    void start_mediarecord(final WebSocket socket){
        capThread = new Thread(new Runnable() {
            public void run() {
                _recLock.lock();
                try {
                    android.os.Process.setThreadPriority(
                            android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                } catch (Exception e) {
                    Log.e(TAG, "Set rec thread priority failed: " + e.getMessage());
                }
                int bitRate= 128 * 1000;
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.REMOTE_SUBMIX);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                recorder.setAudioChannels(2);
                recorder.setAudioSamplingRate(sampleRate);
                recorder.setAudioEncodingBitRate(bitRate);
                FileDescriptor fd = pipe[1].getFileDescriptor();
                recorder.setOutputFile(fd);
                recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                    @Override
                    public void onError(MediaRecorder mediaRecorder, int i, int i1) {
                        Log.e(TAG, "MediaRecorder.OnError");
                    }
                });
                recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                    @Override
                    public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {
                        Log.e(TAG, "MediaRecorder.OnInfo");
                    }
                });
                ws = socket;
                isRunning = true;
                try {
                    DataInputStream is = new DataInputStream(new FileInputStream(pipe[0].getFileDescriptor()));
                    recorder.prepare();
                    recorder.start();
                    while (isRunning) {
                        byte[] header = new byte[7];
                        header[0] = (byte) 0xFF;
                        while (true) {
                            if ( (is.read()&0xFF) == 0xFF ) {
                                header[1] = (byte) is.read();
                                if ( (header[1]&0xF0) == 0xF0) break;
                            }
                        }
                        fill(header, 2, 5, is);
                        boolean protection = (header[1]&0x01)>0 ? true : false;
                        int frameLength = (header[3]&0x03) << 11 |
                                (header[4]&0xFF) << 3 |
                                (header[5]&0xFF) >> 5 ;
                        frameLength -= (protection ? 7 : 9);
                        if (!protection) is.read(new byte[2],0,2);
                        byte[] frame = new byte[frameLength + header.length];
                        System.arraycopy(header, 0, frame, 0, header.length);
//                        is.read(frame, 7, frameLength);
                        fill(frame, 7, frameLength, is);
                        if (ws != null && ws.isOpen()) {
                            try {
                                ws.send(frame);
                            } catch (Exception e) {
                                isRunning = false;
                                Log.e(TAG, e.getMessage(), e);
                            }
                        } else
                            isRunning = false;
                    }
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "error starting mediarecorder", e);
                }
                finally {
                    if (null != recorder) {
                        recorder.stop();
                        recorder.reset();
                        recorder.release();
                        recorder = null;
                    }
                    _recLock.unlock();
                }

            }
        });
        capThread.start();
    }

    private static int fill(byte[] buffer, int offset,int length, InputStream is) throws IOException {
        int sum = 0, len;
        while (sum<length) {
            len = is.read(buffer, offset+sum, length-sum);
            if (len<0) {
                throw new IOException("End of stream");
            }
            else sum+=len;
        }
        return sum;
    }

    void stopRecording() {
        if (null != recorder) {
            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = null;
        }
    }
}