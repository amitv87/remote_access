package android.content;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.IOnPrimaryClipChangedListener;
import android.os.IBinder;
/**
 * Programming interface to the clipboard, which allows copying and pasting
 * between applications.
 * {@hide}
 */
public interface IClipboard {
    public static class Stub {
        public static IClipboard asInterface( IBinder binder ) {
            return null;
        }
    }
    void setPrimaryClip(ClipData clip, String callingPackage);
    ClipData getPrimaryClip(String pkg);
    ClipDescription getPrimaryClipDescription(String callingPackage);
    boolean hasPrimaryClip(String callingPackage);
    void addPrimaryClipChangedListener(IOnPrimaryClipChangedListener listener,
            String callingPackage);
    void removePrimaryClipChangedListener(IOnPrimaryClipChangedListener listener);
    boolean hasClipboardText(String callingPackage);
}