package android.app;

import java.io.File;

public abstract class LoadedApk{
    public abstract ClassLoader getClassLoader();
    public abstract String getAppDir();
    public abstract String getLibDir();
    public abstract String getResDir();
    public abstract String getDataDir();
    public abstract File getDataDirFile();
    public abstract Application makeApplication(boolean forceDefaultAppClass,
            Instrumentation instrumentation);
}