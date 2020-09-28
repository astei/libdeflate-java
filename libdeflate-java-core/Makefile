# Copyright 2016 The Netty Project
#
# The Netty Project licenses this file to you under the Apache License,
# version 2.0 (the "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at:
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.

## GNU Makefile designed to build a dynamic JNI library.
## Based on https://github.com/netty/netty/blob/3cc405296310643bccddc8c81998c97f25b3201c/transport-native-unix-common/Makefile
## but extensively modified for our needs.

## Input environment:
# CC - compiler (gcc or clang)
# JNI_PLATFORM - "linux", "darwin" for macOS, "windows" for Windows.
# LIB_DIR - where the static library will be built in
# OBJ_DIR - where the obj files will be built in (defaults to LIB_DIR)
# LIB_NAME - the name of the native library
# DYLIB_SUFFIX - indicates the suffix of native libraries used by the system

cc-option = $(shell if $(CC) $(1) -c -x c /dev/null -o /dev/null \
	      1>&2 2>/dev/null; then echo $(1); fi)

SRC_DIR = src/main/c
JNI_INCLUDE_DIR = $(JAVA_HOME)/include
JNI_INCLUDES = -I$(JNI_INCLUDE_DIR) -I$(JNI_INCLUDE_DIR)/$(JNI_PLATFORM)
LIB = $(LIB_DIR)/$(LIB_NAME).$(DYLIB_SUFFIX)

CFLAGS += -std=c99 -fvisibility=hidden \
	$(JNI_INCLUDES) -I$(SRC_DIR)/libdeflate -I$(SRC_DIR)/libdeflate/common -I$(SRC_DIR)/libdeflate/lib \
	$(call cc-option,-Wpedantic) \
    $(call cc-option,-Wvla)	\
    $(call cc-option,-Wimplicit-fallthrough)

SRCS = $(wildcard $(SRC_DIR)/*.c) $(wildcard $(SRC_DIR)/libdeflate/lib/*.c) $(wildcard $(SRC_DIR)/libdeflate/lib/*/cpu_features.c)
OBJS = $(SRCS:$(SRC_DIR)/%.c=$(OBJ_DIR)/%.o)

.PHONY: directories

all: directories $(LIB)

$(LIB): $(OBJS)
	mkdir -p $(LIB_DIR)
	$(CC) -shared -o $(LIB) $^

$(OBJ_DIR)/%.o: $(SRC_DIR)/%.c
	$(CC) -o $@ -c $< $(CFLAGS)

directories: ${OBJ_DIR}

${OBJ_DIR}:
	mkdir -p $(OBJ_DIR)
	mkdir -p $(OBJ_DIR)/libdeflate/lib
	mkdir -p $(OBJ_DIR)/libdeflate/lib/x86
	mkdir -p $(OBJ_DIR)/libdeflate/lib/arm

clean:
	rm -rf $(LIB_DIR) $(OBJ_DIR)

## Debug support
# use make print-VARIABLE name to see the value
print-%  : ; @echo $* = $($*)
