function initInteractions(){
	canvas.tabIndex = 1000;
  canvas.addEventListener("mousedown", doMouseDown, false);
  canvas.addEventListener("mouseup", doMouseUp, false);
  canvas.addEventListener("mousemove", doMouseMove, false);
  canvas.addEventListener("keydown", doKeyDown, false);
  canvas.addEventListener("keyup", doKeyUp, false);
  canvas.addEventListener("mouseleave", doMouseLeave, false);
  // canvas.addEventListener("WheelEvent", doMouseWheel, false);
  // canvas.addEventListener('mousewheel', doMouseWheel, false);

  addWheelListener(canvas, doMouseWheel);
  // $(canvas).mousewheel(fdoMouseWheel);
  canvas.addEventListener("touchstart", doTouchStart, false);
  canvas.addEventListener("touchmove", doTouchMove, false);
  canvas.addEventListener("touchend", doTouchEnd, false);
  canvas.oncontextmenu = new Function("return false");
  // canvas.style.cursor = "none";
  
  function doKeyDown(e){
    sendKey(1,e.keyCode,e.altKey,e.shiftKey,e.ctrlKey);
    e.cancelBubble = true;
    if( e.stopPropagation ) e.stopPropagation();
    e.preventDefault();
    return false;
  }

  function doKeyUp(e){
    sendKey(0,e.keyCode,e.altKey,e.shiftKey,e.ctrlKey);
    e.cancelBubble = true;
    if( e.stopPropagation ) e.stopPropagation();
    e.preventDefault();
    return false;
  }
  function doMouseDown(e){
    // e.target.onmousemove = doMouseMove;
    var type = (e.button == 2 ? 4 : 1);
    sendMouse(type, e);
    e.cancelBubble = true;
    if( e.stopPropagation ) e.stopPropagation();
  }
  function doMouseUp(e){
    // e.target.onmousemove = null;
    var type = (e.button == 2 ? 3 : 0);
    sendMouse(type, e);
    e.cancelBubble = true;
    if( e.stopPropagation ) e.stopPropagation();
  }
  function doMouseMove(e){
    sendMouse(2, e);
    e.cancelBubble = true;
    if( e.stopPropagation ) e.stopPropagation();
  }
  var mouseDown = 0;
  var newX = 0;
  var newY = 0;
  function sendMouse(down, e){
    // console.log(e.offsetX, e.layerX, e.offsetY, e.layerY)
    mouseDown = down;
    // newX = (e.offsetX || e.layerX)/canvas.width;
    // newX = newX + (newX * 0.0054);
    // newY = (e.offsetY || e.layerY)/canvas.height;
    var cords = getcords(e);
    newX = cords[0] / canvas.width;
    newY = cords[1] / canvas.height;
    sendMouseWS(down,newX,newY);
  }
  function doMouseLeave(e){
    if(mouseDown == 1 || mouseDown == 4){
      e.target.onmousemove = null;
      mouseDown = (mouseDown == 1 ? 0 : 3);
      sendMouseWS(0,newX,newY);
    }
  }

  function getcords(event){
      var canvasMouseX = event.clientX - (canvas.offsetLeft - window.pageXOffset);
      var canvasMouseY = event.clientY - (canvas.offsetTop - window.pageYOffset);
      return [canvasMouseX, canvasMouseY];
  }
  function sendMouseWS(down, x, y){
    var cords = [x,y];
    send([down,cords[0],cords[1]]);
  }
  function sendKey(down,key,alt,shift,ctrl){
    send([down,key != 224 ? key : 91,alt ? 1 : 0,shift ? 1 : 0,ctrl ? 1 : 0]);
  }
  function sendSpecial(action){
      send([action]);
  }
  var scroll = true;
  function doMouseWheel(e){
    // if(scroll) {
      var x = Math.round(e.deltaX);
      var y = -Math.round(e.deltaY);
      send([x,y]);
      // setTimeout(function(){scroll = true;},400);
    // }
    // scroll = false;
    e.cancelBubble = true;
    if( e.stopPropagation ) e.stopPropagation();
    e.preventDefault();
    return false
  }
  function doTouchStart(e){
    console.log(e);
    e.cancelBubble = true;
    if( e.stopPropagation ) e.stopPropagation();
  }
  function doTouchMove(e){
    console.log(e);
    e.cancelBubble = true;
    if( e.stopPropagation ) e.stopPropagation();
  }
  function doTouchEnd(e){
    console.log(e);
    e.cancelBubble = true;
    if( e.stopPropagation ) e.stopPropagation();
  }
  function send(e){
    // console.log(e);
    ws.send(JSON.stringify(e));
  }
}