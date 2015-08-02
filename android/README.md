# Android Remote Access (audio experimental)
### Setup
---
make an adb connection to the device an run following commands
- ./deploy.sh
- ./run.sh start true &

open http://device_ip:8080 in a browser
to stop the service run thi command
- ./run.sh stop

### Build
Prerequisites
- SDK android 4.4 or above
- NDK api level 19 or above (for audio and a rooted device)
command
- ./deploy.sh build clean

Components
---
### Display Capture
- source - virtual display surface
- encoder - android mediacodec encoder (surface to h264)

### Audio Capture (experimental, choppy)
- source - android remote_submix device
- encoder - LAME MP3 Encoder (http://sourceforge.net/projects/lame/files/lame/3.99/)

### Interactions (touch / keyboard)
- touch - android.hardware.input.InputManager (private API)
- keyboard - android.hardware.input.InputManager (private API)

Notes
---
- For audio to work (root) apk should be placed in /system/priv-app/ or /system/app/ (depending on android version) with proper permissions. A reboot may be required. And then deploy.sh and run.sh should work
- Touch support isn't perfect (using evdev device to inject touch events should solve this)
