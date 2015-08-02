LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_LDLIBS := -llog
LOCAL_CFLAGS := -DSTDC_HEADERS
#LOCAL_ARM_MODE  := arm
LOCAL_C_INCLUDES := $(LOCAL_PATH)/libmp3lame/include
LOCAL_SRC_FILES := libmp3lame/VbrTag.c \
	libmp3lame/bitstream.c \
	libmp3lame/encoder.c \
	libmp3lame/fft.c \
	libmp3lame/gain_analysis.c \
	libmp3lame/id3tag.c \
	libmp3lame/lame.c \
	libmp3lame/mpglib_interface.c \
	libmp3lame/newmdct.c \
	libmp3lame/presets.c \
	libmp3lame/psymodel.c \
	libmp3lame/quantize.c \
	libmp3lame/quantize_pvt.c \
	libmp3lame/reservoir.c \
	libmp3lame/set_get.c \
	libmp3lame/tables.c \
	libmp3lame/takehiro.c \
	libmp3lame/util.c \
	libmp3lame/vbrquantize.c \
	libmp3lame/version.c \
	libmp3lame/jni_wrapper.c

LOCAL_MODULE    := Lame
include $(BUILD_SHARED_LIBRARY)
