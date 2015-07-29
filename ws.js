var log = console.log;
var fs = require('fs');
var ffmpeg_scr, ffmpeg_aud, interactions, os;
if(/^win/.test(process.platform)){
  os = 'win';
  interactions = require("./win_interactions.js");
}
else if(/^darwin/.test(process.platform)){
  os = 'mac';
  interactions = require("./mac_interactions.js");
}
else{
  console.log('platform not supported');
  return
}

var WebSocketServer = require('ws').Server;
var childProcess = require("child_process");
var sjc = require('./strip-json-comments.js');

var wss_scr = new WebSocketServer({port:8081});
var wss_aud = new WebSocketServer({port:8082});

wss_scr.on('connection', function connection(ws) {
  ws.send(JSON.stringify(interactions.getScreenBounds()));
  ws.on('message', function(data){
    sendEvent(data);
  });
  if(ffmpeg_scr && ffmpeg_scr.kill) ffmpeg_scr.kill();
  ffmpeg_scr = new ffmpeg('scr', ws);
});

wss_aud.on('connection', function connection(ws) {
  if(ffmpeg_aud && ffmpeg_aud.kill) ffmpeg_aud.kill();
  ffmpeg_aud = new ffmpeg('aud', ws);
});

function ffmpeg(type, ws){
  try{
    var args = JSON.parse(sjc(fs.readFileSync('./ffmpeg.json', 'utf8')))[os][type];
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
    }
    this.kill = kill;
    ws.on('close', kill);
    ws.on('end', kill);
    ws.on('disconnect', kill);
  }
  catch(e){log(e);}
}

function sendEvent(data){
  try{
    interactions.sendEvent(JSON.parse(data));
  }
  catch (e){log('invalid input');}
}

log('server started');