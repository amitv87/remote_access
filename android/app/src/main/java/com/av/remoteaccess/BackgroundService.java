package com.av.remoteaccess;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BackgroundService extends Service {
    private final String TAG = BackgroundService.class.toString();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "service started");
        if(intent != null && intent.getStringExtra("action") != null){
            String action = intent.getStringExtra("action");
            if (action.equals("start_audio"))
                AudioServer.init(8082);
            else if (action.equals("stop_audio"))
                AudioServer.dispose();
            else if (action.equals("setup_files"))
                Files.Setup(this);
        }
        return Service.START_STICKY;
    }
    @Override
    public void onDestroy(){
        AudioServer.dispose();
    }
}
