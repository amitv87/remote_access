package android.app;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.IBinder;
import android.os.IInterface;
import android.content.pm.UserInfo;

public interface IActivityManager extends IInterface {
    public ComponentName startService(IApplicationThread caller, Intent service,
            String resolvedType, int userId);
    public int stopService(IApplicationThread caller, Intent service,
            String resolvedType, int userId);
    public IBinder getHomeActivityToken();
    public IBinder peekService(Intent service, String resolvedType);
    public boolean switchUser(int userid);
    public UserInfo getCurrentUser();
    public void addPackageDependency(String packageName);
    public Configuration getConfiguration();
    public void grantUriPermission(IApplicationThread caller, String targetPkg, Uri uri, int mode);
//    public ContentProviderHolder getContentProvider(IApplicationThread caller, String name);
//    public ContentProviderHolder getContentProviderExternal(String name, IBinder token);
}