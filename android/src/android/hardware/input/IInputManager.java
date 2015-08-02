package android.hardware.input;

import android.view.InputDevice;
import android.view.InputEvent;

public interface IInputManager
{
    public abstract boolean injectInputEvent(InputEvent inputevent, int i);
    public abstract InputDevice getInputDevice(int id);
}
