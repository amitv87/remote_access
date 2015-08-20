#!/bin/bash
# set -xe
set -e
ACTION=$1
MODE=$2

if [ "$ACTION" == "build" ]; then
  echo "building apk"
  ./gradlew $MODE build
fi

adb install -r app/build/outputs/apk/*-debug.apk
adb push _run.sh /data/local/tmp/
adb shell chmod 777 /data/local/tmp/_run.sh
