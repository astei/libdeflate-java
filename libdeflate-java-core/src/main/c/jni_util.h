/*
 * Copyright 2024 Andrew Steinborn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#ifndef JNIUTIL_H
#define JNIUTIL_H

#include <jni.h>

#ifndef _WIN32
#define LIBDEFLATEJAVA_PUBLIC __attribute__((visibility("default")))
#else
#define LIBDEFLATEJAVA_PUBLIC
#endif

void throwException(JNIEnv *env, const char *type, const char *msg);

#endif
