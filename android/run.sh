#!/bin/bash
# set -xe
set -e

ACTION=$1
NOHUP=$2

PKG="com.av.remoteaccess"
CLS="Main"
APK=`adb shell pm path $PKG | cut -c9- | tr -d '\r' | tr -d '\n'`

start(){
  adb shell am startservice --user 0 -n $PKG/.BackgroundService --es "action" "start_audio"
  adb shell am startservice --user 0 -n $PKG/.BackgroundService --es "action" "setup_files"
  adb shell /data/local/tmp/_run.sh $PKG $CLS $APK $NOHUP
}

stop(){
  adb shell am force-stop $PKG
  adb shell ps | grep -i 'RemoteAccess' | awk '{print $2}' | xargs adb shell kill -2
}
case $ACTION in
  start )
    echo "starting";
    stop;
    start;
    ;;
  stop )
    echo "stopping";
    stop;
    ;;
esac
