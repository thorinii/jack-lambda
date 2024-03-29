# Copyright (C) 2012 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-java-files-under, src/)

LOCAL_MODULE := guava-jack

LOCAL_JAVA_LIBRARIES := jsr305lib-jack

LOCAL_MODULE_TAGS := optional

include $(BUILD_HOST_JAVA_LIBRARY)

#
# Build guava for java-allocation-tracker
#
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src/)
LOCAL_MODULE := guava-collect-jack
LOCAL_JARJAR_RULES := $(LOCAL_PATH)/collect-jarjar-rules.txt
LOCAL_STATIC_JAVA_LIBRARIES := jsr305lib-jack

include $(BUILD_HOST_JAVA_LIBRARY)
