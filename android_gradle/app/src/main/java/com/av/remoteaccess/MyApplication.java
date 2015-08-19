package com.av.remoteaccess;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

public class MyApplication extends Activity {
    public static Context  s_sharedContext;
    static String TAG = MyApplication.class.getCanonicalName();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(getApplicationContext(), "not implemented yet", Toast.LENGTH_SHORT).show();
//        Intent intent = new Intent(this, BackgroundService.class);
//        intent.putExtra("action", "stop_audio");
//        intent.putExtra("action", "start_audio");
//        this.startService(intent);
        finish();
    }
}