# Android Remote Access (audio experimental)
### Setup
---
make an adb connection to the device an run following commands
- ./deploy.sh
- ./run.sh start true &

open http://device_ip:8080 in a browser
to stop the service run this command
- ./run.sh stop

### Build
Prerequisites
- SDK android 4.4 or above (rooted device for audio capture)
- ./deploy.sh build clean

Components
---
### Display Capture
- source - virtual display surface
- encoder - android mediacodec encoder (surface to h264)

### Audio Capture (experimental, choppy)
- source - android remote_submix device
- encoder - mediacodec aac encoder

### Interactions (touch / keyboard)
- touch - android.hardware.input.InputManager (private API)
- keyboard - android.hardware.input.InputManager (private API)

Notes
---
- For audio to work (root) apk should be placed in /system/priv-app/ or /system/app/ (depending on android version) with proper permission. A reboot may be required. And then running deploy.sh and run.sh should start the audio service.
- Touch support isn't perfect (using evdev device to inject touch events should solve this)
