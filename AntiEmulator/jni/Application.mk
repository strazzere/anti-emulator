LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

APP_ABI := armeabi-v7a arm64-v8a

APP_PLATFORM := android-21

include $(BUILD_SHARED_LIBRARY)
