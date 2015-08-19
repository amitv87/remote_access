package com.av.remoteaccess;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class Files{
    static String TAG = Files.class.getCanonicalName();
    public static void Setup(Context context) {
        copyFileOrDir("public", context);
        try {
            Runtime.getRuntime().exec("chmod -R 777 " + "/data/data/" + context.getPackageName() + "/" + "public");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void copyFileOrDir(String path, Context context) {
        AssetManager assetManager = context.getAssets();
        String assets[] = null;
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(path, context);
            } else {
                String fullPath = "/data/data/" + context.getPackageName() + "/" + path;
                File dir = new File(fullPath);
                if (!dir.exists())
                    dir.mkdir();
                for (int i = 0; i < assets.length; ++i) {
                    copyFileOrDir(path + "/" +assets[i], context);
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "I/O Exception", ex);
        }
    }

    private static void copyFile(String filename, Context context) {
        Log.i(TAG, "file: " + filename);
        AssetManager assetManager = context.getAssets();
        
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            String newFileName = "/data/data/" + context.getPackageName() + "/" + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage(), e);
        }

    }
}