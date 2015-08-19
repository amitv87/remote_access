package android.view;

import android.os.IBinder;

public interface IRotationWatcher {
    public static class Stub {
        public static IRotationWatcher asInterface(IBinder obj) {
            return null;
        }
        public void onRotationChanged(int rotation) {
            
        }
    }
    public void onRotationChanged(int rotation);
}