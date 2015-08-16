var log = console.log;
var ffmpeg_scr, ffmpeg_aud, interactions, platform;
var CURSOR_JOB_INTERVAL = 200;
if(/^win/.test(process.platform)){
  platform = 'win';
  interactions = require("./win_interactions.js");
  CURSOR_JOB_INTERVAL = 100;
}
else if(/^darwin/.test(process.platform)){
  platform = 'mac';
  interactions = require("./mac_interactions.js");
  CURSOR_JOB_INTERVAL = 200;
}
else{
  console.log('platform not supported');
  return
}

var fs = require('fs');
var clip = require("copy-paste");
var WebSocketServer = require('ws').Server;
var childProcess = require("child_process");
var sjc = require('./strip-json-comments.js');

var wss_scr = new WebSocketServer({port:8081});
var wss_aud = new WebSocketServer({port:8082});

var force = 0, cursorJob = null;
wss_scr.on('connection', function connection(ws) {
  interactions.getScreenBounds(function(data){
    data.platform = platform;
    ws_send_json(ws, data);
  })
  ws.on('message', function(data){
    sendEvent(data, ws);
  });
  if(ffmpeg_scr && ffmpeg_scr.kill) ffmpeg_scr.kill();
  ffmpeg_scr = new ffmpeg('scr', ws);
  force = 1;
  cursorJob = setInterval(function(){
    updateCursor(ws);
  },CURSOR_JOB_INTERVAL);
});

wss_aud.on('connection', function connection(ws) {
  if(ffmpeg_aud && ffmpeg_aud.kill) ffmpeg_aud.kill();
  ffmpeg_aud = new ffmpeg('aud', ws);
});

function ffmpeg(type, ws){
  try{
    var args = JSON.parse(sjc(fs.readFileSync('./ffmpeg.json', 'utf8')))[platform][type];
    // log(type, args.join(' '));
    var encoder = childProcess.spawn('./ffmpeg', args);
    log('starting encoder', type);
    encoder.stderr.setEncoding('utf8');
    encoder.stdout.on('data', function(data){
      if(ws.readyState == 1)
        ws.send(data, { binary: true });
    });
    encoder.stderr.on('data', function(data){
      log(data);
    });
    var kill = function(){
      if(ws.readyState == 1)
        ws.send('kick');
      if(!encoder)
        return;
      encoder.kill();
      encoder = null;
      log('killed', type);
      if(cursorJob && type == 'scr'){
        clearInterval(cursorJob);
        cursorJob = null;
      }
    }
    this.kill = kill;
    ws.on('close', kill);
    ws.on('end', kill);
    ws.on('disconnect', kill);
  }
  catch(e){log(e);}
}

function sendEvent(data, ws){
  try{
    var json = JSON.parse(data);
    if(json.constructor === Array){
      interactions.sendEvent(json);
    }
    else if(json.action == 'set_clip')
      clip.copy(json.value);
    else if(json.action == 'get_clip')
      clip.paste(function(e, text){
        ws_send_json(ws, {status: 'clip', value: text});
      });
  }
  catch (e){log('invalid input');}
}

function updateCursor(ws){
  interactions.getCursorState(function(value){
    ws_send_json(ws, {status:'cursor', value:value});
    force = 0;
  },force);
}

function ws_send_json(ws, data){
  if(ws.readyState == 1)
    ws.send(JSON.stringify(data));
}

log('server started');
