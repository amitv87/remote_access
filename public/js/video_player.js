function H264Player(){
	var p = new Player({
    useWorker: true,
    workerFile: "/js/Decoder.js",
    webgl: true,
    // transferMemory:true
  });

  this.canvas = p.canvas;

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
  
  this.decodeRaw = function(data){
    if (!(data && data.length)){
      return;
    };
    
    var hit = function(subarray){
      if (subarray){
        bufferAr.push(subarray);
      };
      p.decode(concatUint8(bufferAr));
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