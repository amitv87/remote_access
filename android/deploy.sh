#!/bin/bash
# set -xe
set -e
ACTION=$1
MODE=$2

if [ "$ACTION" == "build" ]; then
	echo "building apk"
	ant $MODE debug -q
fi

adb install -r bin/RemoteAccess-debug.apk
adb push _run.sh /data/local/tmp/
adb shell chmod 777 /data/local/tmp/_run.sh
