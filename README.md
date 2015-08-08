### Remote control your mac / windows from your browser in realtime with full interaction and audio support

Setup
---
Clone the repo and pull latest FFmpeg static build executable (http://ffmpeg.zeranoe.com/builds/)
- npm install
- nmp install http-server -g
- http-server
- node ws.js

open http://localhost:8080 in a browser and remote session should start

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
- audio - native mpHTML5 audio api

### Interactions
Capture keyboard / mouse evets (html5 canvas) and send them over websockets.

Notes
---
- tested on chrome & firefox
- ffmpeg.json is read in an ugly fashion, to fast switch ffmpeg flags

### Known bugs
- mouse double click is broken on mac
- choppy audio
