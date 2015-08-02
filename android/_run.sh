# set -xe
set -e

PKG=$1
CLS=$2
APK=$3
NOHUP=$4

if [ "$NOHUP" == "true" ]; then
	trap "" HUP
fi

# ensure 32-bit mode
export CLASSPATH=$APK
if [ -f /system/bin/app_process32 ]; then
    APP_PROCESS="app_process32"
else
    APP_PROCESS="app_process"
fi
exec $APP_PROCESS /system/bin $PKG.$CLS "hello world"
