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

module.exports = {
  sendEvent: sendEvent
}
