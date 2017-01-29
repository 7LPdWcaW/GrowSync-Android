# Read me

Welcome to grow sync. This is an application addon for the [Grow Tracker](https://github.com/7LPdWcaW/GrowTracker-Android).

The addon works by sending data from the Grow Tracker app to an API running at the IP/host of your choosing. The application comes with a basic server-side implementation, source available in [/syncwebserver](/syncwebserver/).

# Latest releases

[GrowSync for Android: (MD5) 1234567 v1.0]()
[GrowSync web server: (MD5) 1234567 v1.0]()

# Installation

Install the APK. You must have Grow Tracker version 2.2 or above, inside the Grow Tracker app, go to Settings from the sidebar, and you should see listed under `Addons` the addon. You can tap to configure the addon which you will need to do before it will begin syncing.

## How to install

1. Follow [this guide](https://gameolith.uservoice.com/knowledgebase/articles/76902-android-4-0-tablets-allowing-app-installs-from) to enable unknown sources
2. Download the APK from [here](https://github.com/7LPdWcaW/GrowTracker-Android/raw/master/app/app-release.apk)
3. Click on downloaded app and install

#Encryption

Note that this is **not** a guarantee form of encryption from law enforcement agencies.

Encryption in the app uses basic AES for encryption using the provided passphrase. If the passphrase is less than 128 bits (16 UTF-8 chars), it will be padded with `0x0` bytes. You can view the key generator method [here](https://github.com/7LPdWcaW/GrowTracker-Android/blob/master/app/src/main/java/me/anon/lib/helper/EncryptionHelper.java#L27)

You can decrypt your files using your passphrase either by writing a script that uses AES decryption, or an online tool such as [Online-Domain-Tools](http://aes.online-domain-tools.com/).

#License

Copyright 2014-2016 7LPdWcaW

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
