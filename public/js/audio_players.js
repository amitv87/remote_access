/*
  Best quality
  Starts with very little lag but builds up over time
  Using mediasource extensions (works only on chrome for now)
*/
var AudioPlayerMSE = function(mime) {
  console.log('using', this);
  var a_ms = new Audio();
  a_ms.autoplay = true;
  var mediaSource = new MediaSource();
  a_ms.src = window.URL.createObjectURL(mediaSource);

  var sourceBuffer;
  mediaSource.addEventListener('sourceopen', function() {
    sourceBuffer = mediaSource.addSourceBuffer(mime);
  }, true);
  a_ms.load();

  var _buffer = (new Uint8Array()).buffer;
  this.play = function(data){
    appendBuffer(data);
    if(sourceBuffer && !sourceBuffer.updating){
      sourceBuffer.appendBuffer(new Uint8Array(_buffer));
      _buffer = (new Uint8Array()).buffer;
    }
  }

  function appendBuffer(buffer) {
    var tmp = new Uint8Array(_buffer.byteLength + buffer.byteLength);
    tmp.set(new Uint8Array(_buffer), 0);
    tmp.set(new Uint8Array(buffer), _buffer.byteLength);
    _buffer = tmp.buffer;
  };
}

/*
  Choppy but very little lag
  Using HTML5 audio tag element (hacky stuff)
*/
var AudioPlayerURL = function(mime){
  console.log('using', this);
  var buff = [], audios = [], src = '';
  var buffer_counter = 0, a_counter = 0, audio_s_length = 4, buffer_length = 10;
  for(var i = 0; i < audio_s_length; i++){
    var a = new Audio();
    a.autoplay = true;
    a.normalize();
    audios.push(a);
  }
  var audio = audios[0];

  this.play = function(data){
    buffer_counter++;
    buff.push(new Uint8Array(data));
    if(buffer_counter == buffer_length){
      buffer_counter = 0;
      // audio.muted = true;
      audio = audios[a_counter];
      URL.revokeObjectURL(src);
      src = URL.createObjectURL(new Blob(buff, {type:mime}))
      audio.src = src;
      audio.muted = false;
      buff = [];
      a_counter++;
      if(a_counter >= audio_s_length)
        a_counter = 0;
    }
  }
}

/*
  Choppy but very little lag
  Using HTML5 audo context
*/
var AudioPlayerContext = function(mime){
  console.log('using', this);
  var audioCtx = new (window.AudioContext || window.webkitAudioContext)();
  var source = audioCtx.createBufferSource();
  var _buffer = (new Uint8Array()).buffer;

  var buffer_counter = 0, buffer_length = 10;

  this.play = function(data){
    buffer_counter++;
    appendBuffer(data);
    if(buffer_counter == buffer_length){
      buffer_counter = 0;
      audioCtx.decodeAudioData(_buffer, function(buffer) {
        source = audioCtx.createBufferSource();
        source.buffer = buffer;
        source.connect(audioCtx.destination);
        source.start(0);
      },function(e){"Error with decoding audio data", e});
      _buffer = (new Uint8Array()).buffer;
    }
  }

  function appendBuffer(buffer) {
    var tmp = new Uint8Array(_buffer.byteLength + buffer.byteLength);
    tmp.set(new Uint8Array(_buffer), 0);
    tmp.set(new Uint8Array(buffer), _buffer.byteLength);
    _buffer = tmp.buffer;
  };
}

/*
  Good quality but laggy (works with mp3 only)
*/
var AudioPlayerJS = function(mime){
  console.log('using', this);
  var AudioPlayer = new PCMAudioPlayer();
  FormatReader = new AudioFormatReader(mime,
    function(){
      console.log("Reader error: Decoding failed.");
    },
    function(){
      while (FormatReader.SamplesAvailable())
        AudioPlayer.PushBuffer(FormatReader.PopSamples());
    }
  );
  this.play = function(data){
    FormatReader.PushData(data);
  }
}