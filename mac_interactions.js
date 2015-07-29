'use strict'

var fs = require('fs');
var sjc = require('./strip-json-comments.js');
var keyMap = JSON.parse(sjc(fs.readFileSync('./mac_kc.json', 'utf8')));

var $ = require('NodObjC');
$.framework('Cocoa');

var pool;

var ptX = 0;
var ptY = 0;


/**
 * Usage:  mouse.init();
 * Desc:   Initializes the macmouse module
 * Before: mouse is an uninitialized macmouse
 * After:  mouse is an initialized macmouse
 */
var init = function() {
    pool = $.NSAutoreleasePool('alloc')('init');
    var pos = getRealPos();
    setPos(pos.x, pos.y);
}

/**
 * Usage:  var pos = mouse.getRealPos();
 * Desc:   Sends request for real mouse position, more expensive than getPos
 * Before: mouse is an initialized macmouse
 * After:  pos holds x and y numbers representing the system mouse position
 */
var getRealPos = function() {
    var pos = $.NSEvent("mouseLocation");
    return { x: pos.x, y: pos.y };
}

/**
 * Usage:  var pos = mouse.getPos();
 * Desc:   Returns mouse position currently stored in the mouse module
 * Before: mouse is an initialized macmouse
 * After:  pos holds x and y numbers representing the system mouse position currently stored in the
 *         mouse module
 */
var getPos = function() {
    return { x: ptX, y: ptY };
}

// simple private helper function
var setPos = function(x, y) {
    ptX = x;
    ptY = y;
}

/**
 * Usage:  mouse.Place();
 * Desc:   Sends mouse event message to place the system mouse at a specific position
 * Before: mouse is an initialized macmouse, x and y are numbers representing a specific position
 * After:  mouse event has been sent to move the system mouse to position defined by x and y
 */
var Place = function(x, y) {
    setPos(x, y);
    var moveEvent = $.CGEventCreateMouseEvent(null, $.kCGEventMouseMoved, $.CGPointMake(x, y), $.kCGMouseButtonLeft);
    $.CGEventPost($.kCGHIDEventTap, moveEvent);
}

/**
 * Usage:  mouse.DragPlace(x, y);
 * Desc:   Sends mouse event message to place the system mouse at a specific position while in a 
 *         dragging state
 * Before: mouse is an initialized macmouse, x and y are numbers representing a specific position, the 
 *         system mouse currently has (or thinks it has) the left mouse button pressed
 * After:  mouse event has been sent to move the system mouse to position defined by x and y with left 
 *         mouse button pressed
 */
var DragPlace = function(x, y) {
    setPos(x, y);
    var moveEvent = $.CGEventCreateMouseEvent(null, $.kCGEventLeftMouseDragged, $.CGPointMake(x, y), $.kCGMouseButtonLeft);
    $.CGEventPost($.kCGHIDEventTap, moveEvent);
}

/**
 * Usage:  mouse.Move(dx, dy);
 * Desc:   Sends mouse event message to move the system mouse (from current stored position in the mouse 
 *         module) by a vector defined by dx and dy
 * Before: mouse is an initialized macmouse, dx and dy are numbers representing our moving vector 
 * After:  mouse event has been sent to move the system mouse by a vector defined by the numbers dx and dy
 */
var Move = function(dx, dy) {
    ptX += dx;
    ptY += dy;
    var moveEvent = $.CGEventCreateMouseEvent(null, $.kCGEventMouseMoved, $.CGPointMake(ptX, ptY), $.kCGMouseButtonLeft);
    $.CGEventPost($.kCGHIDEventTap, moveEvent);
}

/**
 * Usage:  mouse.DragMove(dx, dy);
 * Desc:   Sends mouse event message to move the system mouse (from current stored position in the mouse 
 *         module) by a vector defined by dx and dy while in a dragging state
 * Before: mouse is an initialized macmouse, dx and dy are numbers representing our moving vector, the 
 *         system mouse currently has (or thinks it has) the left mouse button pressed
 * After:  mouse event has been sent to move the system mouse by a vector defined by the numbers dx and dy 
 *         with left mouse button pressed
 */
var DragMove = function(dx, dy) {
    ptX += dx;
    ptY += dy;
    var moveEvent = $.CGEventCreateMouseEvent(null, $.kCGEventLeftMouseDragged, $.CGPointMake(ptX, ptY), $.kCGMouseButtonLeft);
    $.CGEventPost($.kCGHIDEventTap, moveEvent);
}

/**
 * Usage:  mouse.LeftButtonPress();
 * Desc:   Sends mouse event message to press and hold down the left button of the system mouse
 * Before: mouse is an initialized macmouse
 * After:  mouse event has been sent to press and hold the left button on the system mouse
 */
var LeftButtonPress = function() {
    var clickDown = $.CGEventCreateMouseEvent(null, $.kCGEventLeftMouseDown, $.CGPointMake(ptX, ptY), $.kCGMouseButtonLeft);
    $.CGEventPost($.kCGHIDEventTap, clickDown);
}

/**
 * Usage:  mouse.LeftButtonRelease();
 * Desc:   Sends mouse event message to release a pressed left button of the system mouse
 * Before: mouse is an initialized macmouse
 * After:  mouse event has been sent to release a pressed left button on the system mouse
 */
var LeftButtonRelease = function() {
    var clickUp = $.CGEventCreateMouseEvent(null, $.kCGEventLeftMouseUp, $.CGPointMake(ptX, ptY), $.kCGMouseButtonLeft);
    $.CGEventPost($.kCGHIDEventTap, clickUp);
}

/**
 * Usage:  mouse.Click();
 * Desc:   Sends mouse event message to press and release left button of the system mouse
 * Before: mouse is an initialized macmouse
 * After:  mouse event has been sent to press and release left button on the system mouse
 */
var Click = function() {
    LeftButtonPress();
    LeftButtonRelease();
}

/**
 * Usage:  mouse.RightButtonPress();
 * Desc:   Sends mouse event message to press and hold down the right button of the system mouse
 * Before: mouse is an initialized macmouse
 * After:  mouse event has been sent to press and hold the right button on the system mouse
 */
var RightButtonPress = function() {
    var clickDown = $.CGEventCreateMouseEvent(null, $.kCGEventRightMouseDown, $.CGPointMake(ptX, ptY), $.kCGEventRightMouseDown);
    $.CGEventPost($.kCGHIDEventTap, clickDown);
}

/**
 * Usage:  mouse.RightButtonRelease();
 * Desc:   Sends mouse event message to release a pressed right button of the system mouse
 * Before: mouse is an initialized macmouse
 * After:  mouse event has been sent to release a pressed right button on the system mouse
 */
var RightButtonRelease = function() {
    var clickUp = $.CGEventCreateMouseEvent(null, $.kCGEventRightMouseUp, $.CGPointMake(ptX, ptY), $.kCGEventRightMouseDown);
    $.CGEventPost($.kCGHIDEventTap, clickUp);
}

/**
 * Usage:  mouse.RightClick();
 * Desc:   Sends mouse event message to press and release right button of the system mouse
 * Before: mouse is an initialized macmouse
 * After:  mouse event has been sent to press and release right button on the system mouse
 */
var RightClick = function() {
    RightButtonPress();
    RightButtonRelease();
}

/**
 * Usage:  mouse.DoubleClick();
 * Desc:   Sends mouse event message to double click the system mouse
 * Before: mouse is an initialized macmouse
 * After:  mouse event has been sent to double click the system mouse
 */
var DoubleClick = function() {
    var evt = $.CGEventCreateMouseEvent(null, $.kCGEventLeftMouseDown, $.CGPointMake(ptX, ptY), $.kCGMouseButtonLeft);
    $.CGEventSetIntegerValueField(evt, $.kCGMouseEventClickState, 2);
    $.CGEventPost($.kCGHIDEventTap, evt);
    $.CGEventSetType(evt, $.kCGEventLeftMouseUp);
    $.CGEventPost($.kCGHIDEventTap, evt);
    $.CGEventSetType(evt, $.kCGEventLeftMouseDown);
    $.CGEventPost($.kCGHIDEventTap, evt);
    $.CGEventSetType(evt, $.kCGEventLeftMouseUp);
    $.CGEventPost($.kCGHIDEventTap, evt);
}

/**
 * Usage:  mouse.Scroll(vertical, horizontal);
 * Desc:   Sends mouse scroll event message
 * Before: mouse is an initialized macmouse, vertical and horizontal
 *         are 'small signed integer values, typically in a range from -10 to +10',
 *         in reality they can be any integer from -32768 to 32767,
 *         if horizontal isn't provided it defaults to 0
 * After:  scroll event has been sent to scroll by a vector defined by the
 *         vertical and horizontal integers
 */
var Scroll = function(vertical, horizontal) {
    if (typeof horizontal === 'undefined') horizontal = 0;
    var scrollEvent = $.CGEventCreateScrollWheelEvent(null, $.kCGScrollEventUnitLine, 1, vertical, horizontal);
    $.CGEventPost($.kCGHIDEventTap, scrollEvent);
}

/**
 * Usage:  mouse.quit();
 * Desc:   Does garbage collection some on objective c stuff, be a good lad and call this when 
 *         you're done using the macmouse module
 * Before: mouse is an initialized macmouse
 * After:  mouse is an uninitialized macmouse
 */
var quit = function() {
    pool('drain');
}

var keyd = function(kc){
    if(kc == null)
        return
    var keyEvent = $.CGEventCreateKeyboardEvent(null, kc, true);
    $.CGEventPost($.kCGHIDEventTap, keyEvent);
}

var keyu = function(kc){
    if(kc == null)
        return
    var keyEvent = $.CGEventCreateKeyboardEvent(null, kc, false);
    $.CGEventPost($.kCGHIDEventTap, keyEvent);
}

var key = function(kc){
    keyd(kc);
    keyu(kc);
}

var resw = $.CGDisplayPixelsWide(), resh = $.CGDisplayPixelsHigh();

init();

var sendEvent = function(arr){
  if(arr.length == 5){
    if(arr[0] == 1)
      keyd(keyMap[arr[1]]);
    else if(arr[0] == 0)
      keyu(keyMap[arr[1]]);
  }
  else if(arr.length == 3){
    if(arr[0] == 1)
      LeftButtonPress();
    else if(arr[0] == 0)
      LeftButtonRelease();
    else if(arr[0] == 4)
      RightButtonPress();
    else if(arr[0] == 3)
      RightButtonRelease();
    else
      Place(arr[1] * resw, arr[2] * resh);
  }
  else if(arr.length == 2)
    Scroll(arr[1]/10, arr[0]/10);
}

var getScreenBounds = function(){
    return {width: resw, height: resh}
}

module.exports = {
    sendEvent: sendEvent,
    getScreenBounds: getScreenBounds,
    quit: quit
}