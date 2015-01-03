LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := anti
LOCAL_SRC_FILES := anti.c

include $(BUILD_SHARED_LIBRARY)