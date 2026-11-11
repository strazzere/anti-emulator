anti-emulator
=============

Android Anti-Emulator, originally presented at HitCon 2013: "Dex Education 201: Anti-Emulation"

Purpose of this project was intended to show various ways of detecting an emulated Android environment. Some of the methods are adapted from previously seen malware on other operating systems, others are just random thoughts. Slowly over time things have been added that I've either thought of randomly, stumbled upon or came across in the wild. I'll do my best to comment if anything from the wild is added directly to the project, as this would likely be more interesting to AV/researchers attempting to hide their own sandboxes.

Contents
--------

 - slides/ - Talk slides
 - AntiEmulator/ - Android app and main source of anti* code (Java in `src/`, the native QEMU check in `jni/`)

Building
--------

You need a JDK (17 or newer) and the Android SDK, which can be pointed to with the `ANDROID_HOME` environment
variable or a `local.properties` file containing `sdk.dir=/path/to/Android/Sdk`. The build uses SDK platform 35 and
NDK 28.0.12433566 (the Android Gradle Plugin downloads them if the SDK licenses have been accepted).

```bash
./gradlew assembleDebug
```

The APK is written to `AntiEmulator/build/outputs/apk/debug/`, and can be installed with `adb install`. It is also
a regular Android Studio project, just open the top level directory.

Disclaimer
----------

This presentation and code are meant for education and research purposes only. Do as you please with it, but accept any and all responsibility for your actions. The tools were created specifically to assist in malware reversing and analysis - be careful.

License
-------

    Copyright 2014-19 Tim 'diff' Strazzere <strazz@gmail.com>

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
