var can = document.createElement('canvas');
// $('body').prepend($(can));
can.style.float = 'left';
can.style.backgroundColor = "blue";
var img = document.createElement('img');
var ctx = can.getContext("2d");
img.onload = function() {
  ctx.drawImage(this, 0,0);
  var dataUrl = can.toDataURL('image/png');
  var cursorStyle = 'url(' + dataUrl + ') ' + this.offset[0] + " " + this.offset[1] +', auto';
  canvas.style.cursor = cursorStyle;
};
function setCursor(data, noRepeat){
  // console.log(data.offset, data.size, data.base64.length);
  can.width = data.size.Width;
  can.height = data.size.Height;
  can.style.width = data.size.Width + 'px';
  can.style.height = data.size.Height + 'px';
  if(window.platform == 'win'){
    ctx.shadowColor = "black";
    ctx.shadowOffsetX = 1;
    ctx.shadowOffsetY = 1;
    ctx.shadowBlur = 1;
  }
  img.offset = data.offset;
  img.src = "data:image/png;base64," + data.base64;;
}

window.angle = 0;
function setOrientation(value){
  remoteAngle = value;
  switch(value){
    case 0: angle = 0;break;
    case 90: angle = -90;break;
    case 180: angle = 180;break;
    case 270: angle = 90;break;
  }

  canvas.style.transform = "rotate(" + angle.toString() + "deg)";
}

function initInteractions(){
	canvas.tabIndex = 1000;
  canvas.addEventListener("mousedown", doMouseDown, false);
  canvas.addEventListener("mouseup", doMouseUp, false);
  canvas.addEventListener("mousemove", doMouseMove, false);
  canvas.addEventListener("keydown", doKeyDown, false);
  canvas.addEventListener("keyup", doKeyUp, false);
  canvas.addEventListener("mouseleave", doMouseLeave, false);
  canvas.contenteditable = true;
  // canvas.addEventListener('mousewheel', doMouseWheel, false);

  addWheelListener(canvas, doMouseWheel);
  canvas.addEventListener("touchstart", doTouchStart, false);
  canvas.addEventListener("touchmove", doTouchMove, false);
  canvas.addEventListener("touchend", doTouchEnd, false);
  canvas.oncontextmenu = new Function("return false");

  function supress(e){
    e.cancelBubble = true;
    if( e.stopPropagation ) e.stopPropagation();
    e.preventDefault();
    return false;
  }

  canvas.ondblclick = function(e){
    if(window.platform == 'mac'){
      var type = (e.button == 2 ? 4 : 1);
      send([1]);
    }
    return supress(e);
  }

  function doKeyDown(e){
    sendKey(1,e.keyCode,e.altKey,e.shiftKey,e.ctrlKey);
    return supress(e);
  }

  function doKeyUp(e){
    sendKey(0,e.keyCode,e.altKey,e.shiftKey,e.ctrlKey);
    return supress(e);
  }

  var mouseDown = 0;
  function doMouseDown(e){
    // e.target.onmousemove = doMouseMove;
    var type = (e.button == 2 ? 4 : 1);
    mouseDown = type;
    sendMouse(type, e);
    canvas.focus();
    return supress(e);
  }
  function doMouseUp(e){
    // e.target.onmousemove = null;
    var type = (e.button == 2 ? 3 : 0);
    mouseDown = type;
    sendMouse(type, e);
    return supress(e);
  }
  function doMouseMove(e){
    if(canvas.android && mouseDown != 1)
      return;
    sendMouse(2, e);
  }
  var newX = 0;
  var newY = 0;
  function sendMouse(down, e){
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
    var rect = canvas.getBoundingClientRect();
    var x = event.clientX - rect.left;
    var y = event.clientY - rect.top;
    return [x, y];
  }
  function sendMouseWS(down, x, y){
    var cords = [x,y];
    send([down,cords[0],cords[1]]);
  }
  function sendKey(down,key,alt,shift,ctrl){
    send([down,key != 224 ? key : 91,alt ? 1 : 0,shift ? 1 : 0,ctrl ? 1 : 0]);
  }
  window.sendSpecial = function(action){
      send([action]);
  }
  var scroll = true;
  function doMouseWheel(e){
    if(canvas.android)
      return;
    var x = Math.round(e.deltaX);
    var y = -Math.round(e.deltaY);

    if(window.platform == 'win') {
      x = getWinScroll(x);
      y = getWinScroll(y);
      if(scroll){
        scroll = false;
        setTimeout(function(){
          send([x,y]);
          scroll = true;
        },45)
      }
    }
    else
      send([x,y]);
    return supress(e);
  }

  function getWinScroll(value){
    ret = value;
    var abs = Math.abs(value);
    if(abs > 0)
      ret = 4 * (value/abs);
    return Math.round(ret);
    // var ret = Math.pow(abs,1/3) * (value/(abs ? abs : 1));
    // var ret_abs = Math.abs(ret);
    // if(ret_abs < 10 && ret_abs > 1)
    //   ret = 4 * (ret/ret_abs);
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

  function rotate(x, y) {
    var xm = 1/2, ym = 1/2;
    var a = angle * Math.PI / 180;
    return [x,y];
    return [
      Math.cos(a) * (x-xm) - Math.sin(a) * (y-ym) + xm,
      Math.sin(a) * (x-xm) + Math.cos(a) * (y-ym) + ym
    ];
  }

  window.send = function(e){
    // console.log(e);
    ws.send(JSON.stringify(e));
  }

  window.clipbox = $('#clipbox');
  window.getClip = function(){
    send({action:'get_clip'});
  }

  window.setClip = function(){
    send({action:'set_clip', value: clipbox.val()});
    $('#clipbox').val('');
  }

  window.onClip = function(value){
    clipbox.val(value);
    clipbox.focus();
    clipbox.select();
  }

  clipbox.click(function(e){
    clipbox.focus();
    clipbox.select();
    // return false;
  });

  $('#controls button').click(function(){
    var action = $(this).attr('action');
    sendSpecial(action);
  });

  $('#rotation button').click(function(){
    send({action:'set_orientation', value: Number($(this).attr('angle'))})
  })
}

var body = $('body')[0];
function goFS(){
  if (body.requestFullscreen) {
    body.requestFullscreen();
  } else if (body.msRequestFullscreen) {
    body.msRequestFullscreen();
  } else if (body.mozRequestFullScreen) {
    body.mozRequestFullScreen();
  } else if (body.webkitRequestFullscreen) {
    body.webkitRequestFullscreen();
  }
}

function draggy(selector) {
  interact(selector).draggable({
    inertia: true,
    restrict: {
      restriction: "html",
      endOnly: true,
      elementRect: { top: 0, left: 0, bottom: 1, right: 1 }
    },
    onmove: function(event) {
      var target = event.target,
      x = (parseFloat(target.getAttribute('data-x')) || 0) + event.dx,
      y = (parseFloat(target.getAttribute('data-y')) || 0) + event.dy;
      target.style.webkitTransform =
      target.style.transform = 'translate(' + x + 'px, ' + y + 'px)';
      target.setAttribute('data-x', x);
      target.setAttribute('data-y', y);
    }
  });
  $(selector + ' .bar').dblclick(function(){
    $(selector + ' .holder').slideToggle();
  });
}