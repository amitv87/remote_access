var edge = require('edge');
var sendEvent = edge.func({
  source: function() {/*
    using System;
    using WindowsInput;
    using WindowsInput.Native;
    using System.Threading.Tasks;
    public class Startup
    {
      static InputSimulator sim = new InputSimulator();
      static IKeyboardSimulator kb = sim.Keyboard;
      static IMouseSimulator mo = sim.Mouse;

      public async Task<object> Invoke(dynamic obj)
      {
        if (obj.Length == 3)
        {
          if (obj[0] == 0)
            mo.LeftButtonUp();
          else if (obj[0] == 1)
            mo.LeftButtonDown();
          else if (obj[0] == 2)
          {
            double x = Convert.ToDouble(obj[1].ToString()) * 65535;
            double y = Convert.ToDouble(obj[2].ToString()) * 65535;
            mo.MoveMouseTo(x, y);
          }
          else if (obj[0] == 3)
            mo.RightButtonUp();
          else if (obj[0] == 4)
            mo.RightButtonDown();
        }
        else if (obj.Length == 5)
        {
          if (obj[0] == 0)
            kb.KeyUp((VirtualKeyCode)obj[1]); 
          else if (obj[0] == 1)
            kb.KeyDown((VirtualKeyCode)obj[1]);
        }
        else if (obj.Length == 2)
        {
          mo.HorizontalScroll(obj[0]/4);
          mo.VerticalScroll(obj[1]/4);
        }
        return 1;
      }
    }
  */},
  references: [ 'WindowsInput.dll' ]
});

var _getCursorState = edge.func({
  source: function() {/*
    using System;
    using System.IO;
    using System.Runtime.InteropServices;
    using System.Threading.Tasks;
    using System.Dynamic;
    using System.Drawing;
    using System.Drawing.Imaging;
    public class Startup
    {
      static Bitmap bmp;
      static IntPtr hicon;
      static CURSORINFO ci = new CURSORINFO();
      static ICONINFO icInfo;
      static MemoryStream ms = new MemoryStream();

      static int x = 0, y = 0;
      static dynamic mouse = new ExpandoObject();
      static int lastHandle = 0;
      public async Task<object> Invoke(dynamic obj)
      {
        ci.cbSize = Marshal.SizeOf(ci);
        GetCursorInfo(out ci);
        if(ci.hCursor.ToInt32() == lastHandle && obj == 0)
          return false;
        lastHandle = ci.hCursor.ToInt32();
        mouse.info = ci;
        bmp = CaptureCursor(ref x, ref y);
        if(bmp == null)
          return false;
        mouse.size = bmp.Size;
        mouse.base64 = Convert.ToBase64String(getBytes(bmp));
        mouse.offset = new int[]{x,y};
        return mouse;
      }

      static Bitmap CaptureCursor(ref int x, ref int y)
      {
        hicon = CopyIcon(ci.hCursor);
        if (GetIconInfo(hicon, out icInfo))
        {
          x = (int)icInfo.xHotspot;
          y = (int)icInfo.yHotspot;
          Icon ic = Icon.FromHandle(hicon);
          return ic.ToBitmap();
        }
        return null;
      }

      static byte[] getBytes(Bitmap bmp){
        ms.SetLength(0);
        bmp.Save(ms, ImageFormat.Png);
        return ms.ToArray();
      }

      public const Int32 CURSOR_SHOWING = 0x00000001;
      [StructLayout(LayoutKind.Sequential)]
      struct POINT
      {
          public Int32 x;
          public Int32 y;
      }

      [StructLayout(LayoutKind.Sequential)]
      struct CURSORINFO
      {
          public Int32 cbSize;        // Specifies the size, in bytes, of the structure. 
          // The caller must set this to Marshal.SizeOf(typeof(CURSORINFO)).
          public Int32 flags;         // Specifies the cursor state. This parameter can be one of the following values:
          //    0             The cursor is hidden.
          //    CURSOR_SHOWING    The cursor is showing.
          public IntPtr hCursor;          // Handle to the cursor. 
          public POINT ptScreenPos;       // A POINT structure that receives the screen coordinates of the cursor. 
      }

      [StructLayout(LayoutKind.Sequential)]
      public struct ICONINFO
      {
          public bool fIcon;         // Specifies whether this structure defines an icon or a cursor. A value of TRUE specifies 
          public Int32 xHotspot;     // Specifies the x-coordinate of a cursor's hot spot. If this structure defines an icon, the hot 
          public Int32 yHotspot;     // Specifies the y-coordinate of the cursor's hot spot. If this structure defines an icon, the hot 
          public IntPtr hbmMask;     // (HBITMAP) Specifies the icon bitmask bitmap. If this structure defines a black and white icon, 
          public IntPtr hbmColor;    // (HBITMAP) Handle to the icon color bitmap. This member can be optional if this 
      }

      [DllImport("user32.dll", EntryPoint = "GetCursorInfo")]
      static extern bool GetCursorInfo(out CURSORINFO pci);

      [DllImport("user32.dll", EntryPoint = "CopyIcon")]
      static extern IntPtr CopyIcon(IntPtr hIcon);

      [DllImport("user32.dll", EntryPoint = "GetIconInfo")]
      static extern bool GetIconInfo(IntPtr hIcon, out ICONINFO piconinfo);
    }

  */},
  references: [ 'System.Drawing.dll', 'System.Windows.Forms.dll' ]
});

var _getScreenBounds = edge.func({
  source: function() {/*
    using System.Drawing;
    using System.Windows.Forms;
    using System.Threading.Tasks;
    public class Startup
    {

      public async Task<object> Invoke(dynamic obj)
      {
        return Screen.PrimaryScreen.Bounds;
      }
    }
  */},
  references: [ 'System.Drawing.dll', 'System.Windows.Forms.dll' ]
});

var screenBounds;
var getScreenBounds = function(callback){
  _getScreenBounds(null,function(error, result){
    if(!error){
      screenBounds = result;
      if(callback)
        callback({width: screenBounds.Width, height: screenBounds.Height})
    }
  });
}

var getCursorState = function(callback, force){
  _getCursorState(force,function(error, result){
    if(!error){
      if(callback && result)
        callback(result);
    }
  });
}

//getCursorState(function(e){console.log(e)});

module.exports = {
  sendEvent: sendEvent,
  getScreenBounds: getScreenBounds,
  screenBounds: screenBounds,
  getCursorState: getCursorState
}
