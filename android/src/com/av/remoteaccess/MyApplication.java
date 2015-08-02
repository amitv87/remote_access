package com.av.remoteaccess;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
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