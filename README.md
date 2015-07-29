
Server-side
===
Display Capture
---
mac - avfoundation screen capture (ffmpeg built in)
win - screen-capture-recorder https://sourceforge.net/projects/screencapturer/files (used v0.12.8)

Audio Capture
---
mac - soundflower https://code.google.com/p/soundflower/downloads/list (used v1.6.6)
win - screen-capture-recorder built in

FFmpeg Encoder
---
latest static build should work from http://ffmpeg.zeranoe.com/builds/
mac -  used 64bit N-73753-g1aab5d8-tessus
win -  used 64bit N-74113-gcdb0225

Interactions
---
mac - https://github.com/Loknar/node-macmouse
win - https://inputsimulator.codeplex.com/ (node edge module)

Client-side
===
Playback
---
video - raw h264 decoder and webgl playback https://github.com/mbebenita/Broadway
audio - js decoder and HTML5 audio api https://github.com/JoJoBond/3LAS


Notes
===
Streaming formats
---
video - raw h264
audio - mp3 cbr