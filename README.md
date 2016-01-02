### Remote control your mac / windows / [android](https://github.com/amitv87/remote_access/tree/master/android) from your browser in realtime with full interaction (including dirty clipboard support) and audio support

[![Alt text for your video](https://i.ytimg.com/vi/URjxY663Opc/hqdefault.jpg)](https://www.youtube.com/watch?v=URjxY663Opc&feature=youtu.be)

Setup
---
Clone the repo and pull latest FFmpeg static build executable (win: http://ffmpeg.zeranoe.com/builds/, mac: http://evermeet.cx/ffmpeg/)
- npm install
- npm install http-server -g
- http-server
- node ws.js

open http://remoteip:8080 in a browser and remote session should start

Server-side
---
### Display Capture
- mac - avfoundation screen capture (ffmpeg built in)
- win - screen-capture-recorder directshow filter https://sourceforge.net/projects/screencapturer/files (used v0.12.8)

### Audio Capture
- mac - soundflower https://code.google.com/p/soundflower/downloads/list (used v1.6.6)
- win - screen-capture-recorder built in audio capture

### FFmpeg Encoder
latest static build should work fine
- mac -  used 64bit N-73753-g1aab5d8-tessus
- win -  used 64bit N-74113-gcdb0225

### Streaming format
- video - raw h264
- audio - aac

### Interactions (mouse / keyboard)
- mac - https://github.com/Loknar/node-macmouse (node objc)
- win - https://inputsimulator.codeplex.com/ (node edge module)

Client-side
---
### Playback
- video - raw h264 decoder and webgl canvas playback https://github.com/mbebenita/Broadway
- audio - native HTML5 audio apis (mse, audio context, data url)

### Interactions
Capture keyboard / mouse evets (html5 canvas) and send them over websockets.

Notes
---
- tested on chrome & firefox
- ffmpeg.json is read in an ugly fashion, to fast switch ffmpeg flags

### Known bugs
- choppy audio (with audio context or data url)
