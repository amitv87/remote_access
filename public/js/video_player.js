function H264Player(){
  console.log('using', this);
	var p = new Player({
    useWorker: true,
    workerFile: "/js/Decoder.js",
    webgl: true,
    // transferMemory:true
  });

  this.canvas = p.canvas;
  var parser = new nalParser(p);
  this.play = function(buffer){
    parser.parse(buffer);
  };
}

function OpenH264Player(canvas){
  var _this = this;
  this.renderer = new WebGLRenderer();
  // this.renderer = new RGBRenderer();
  this.decoder = new Worker("/js/openh264_worker.js");
  this.decoder.postMessage({
      rgb: this.renderer.is_rgba()
  });
  this.canvas = canvas;
  var width, height;
  this.decoder.onmessage = function (ev) {
    if (!_this.renderer_initialized) {
        if (ev.data instanceof Uint8Array)
          return;
        width = _this.decoder_width = ev.data.width;
        height = _this.decoder_height = ev.data.height;
        _this.canvas.width = width;
        _this.canvas.height = height;
        _this.renderer.init(_this.canvas, width, height);
        _this.renderer_initialized = true;
        return;
    }
    if (ev.data.length == 1)
        return;
    var yuv = ev.data;
    if (_this.renderer.is_rgba()) {
        _this.renderer.render(yuv, yuv, yuv);
    }
    else {
        var s = _this.decoder_width * _this.decoder_height;
        var y = yuv.subarray(0, s);
        var u = yuv.subarray(s, s * 1.25);
        var v = yuv.subarray(s * 1.25, s * 1.5);
        _this.renderer.render(y, u, v);
    }
  }
  var parser = new nalParser({
    decode: function(byteArray){
      _this.decoder.postMessage(byteArray);
    }
  });
  this.play = function(buffer){
    parser.parse(buffer);
  }
}

function nalParser(player){
  var bufferAr = [];
  var concatUint8 = function(parAr) {
    if (!parAr || !parAr.length){
      return new Uint8Array(0);
    };
    
    if (parAr.length === 1){
      return parAr[0];
    };
    
    var completeLength = 0;
    var i = 0;
    var l = parAr.length;
    for (i; i < l; ++i){
      completeLength += parAr[i].byteLength;
    };

    var res = new Uint8Array(completeLength);
    var filledLength = 0;

    for (i = 0; i < l; ++i){
      res.set(new Uint8Array(parAr[i]), filledLength);
      filledLength += parAr[i].byteLength;
    };
    return res;
  };
  this.parse = function(buffer){
    if (!(buffer && buffer.byteLength)){
      return;
    };
    var data = new Uint8Array(buffer);
    var hit = function(subarray){
      if (subarray){
        bufferAr.push(subarray);
      };
      player.decode(concatUint8(bufferAr));
      bufferAr = [];
    };

    var b = 0;
    var lastStart = 0;
    
    var l = data.length;
    var zeroCnt = 0;
    
    for (b = 0; b < l; ++b){
      if (data[b] === 0){
        zeroCnt++;
      }else{
        if (data[b] == 1){
          if (zeroCnt >= 3){
            if (lastStart < b - 3){
              hit(data.subarray(lastStart, b - 3));
              lastStart = b - 3;
            }else if (bufferAr.length){
              hit();
            }
          };
        };
        zeroCnt = 0;
      };
    };
    if (lastStart < data.length){
      bufferAr.push(data.subarray(lastStart));
    };
  };
}