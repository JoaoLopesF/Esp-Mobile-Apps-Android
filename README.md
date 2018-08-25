# Esp-Mobile-Apps-Android
Esp-Mobile-Apps is a set of examples apps to start making Esp32 BLE devices connected to mobile applications (Android and iOS)

I have prepared a set of applications, to serve as a basis,
for those who need to make ble connected mobile projects with the ESP32.

* Part I    - __ESP-IDF app__ - Esp32 firmware example  - https://github.com/JoaoLopesF/Esp-Mobile-Apps-Esp32
* Part II   - __iOS app__ - mobile app example          - https://github.com/JoaoLopesF/Esp-Mobile-Apps-iOS
* Part III  - __Android app__ - mobile app example      - this github repo

It is a advanced, but simple (ready to go), fully functional set of applications.

![Esp-Mobile-App](https://i.imgur.com/MuR7gna.png)

## Contents

 - [Esp32](#esp32)
 - [BLE](#ble)
 - [Part II - Andrioid app](#part-ii---android-app)
 - [Features](#features)
 - [BLE messages](#blemessages)
 - [Structure](#structure)
 - [Prerequisites](#rrerequisites)
 - [Install](#install)
 - [Feedback and contribution](#feedback-and-contribution)
 - [To-do](#to-do)
 - [Researchs used ](#researchs-used)
 - [Release History](#release-history)
 - [Screenshots](#screenshots)

## Esp32

The Esp32 is a powerful board with 2 cores, 520K RAM, 34 GPIO, 3 UART,
Wifi and Bluetooth Dual Mode. And all this at an excellent price.

ESP-IDF is very good SDK, to developer Esp32 projects.
With Free-RTOS (with multicore), WiFi, BLE, plenty of GPIOs, peripherals support, etc.

## BLE

With Esp32, we can develop, in addition to WIFI applications (IoT, etc.),
devices with Bluetooth connection for mobile applications.

BLE is a Bluetooth Low Energy:

    BLE is suitable for connection to Android and iOS.
    Nearly 100% of devices run Android >= 4.3, and most of them should have BLE.
    For iOS, we have support for BLE, and for normal Bluetooth, only some modules with Mfi certification (made for i ...)

    So BLE is the most viable alternative for Esp32 to communicate with mobile devices.

# Part II - Android app 

    This mobile app for Android >= 4.3
    All code is written in Kotlin, that is more advanced than Java.
    No third part libraries is used

## Features

This app example to Android, have advanced features like:

    - Support for large BLE messages 
      (if necessary, automatically send / receive in small pieces)

    - Modular and advanced programming
    - Based in mature code (I have used in Bluetooth devices and mobile apps, since years ago)

    - Stand-by support for ESP32 deep-sleep
      (by a button, or by inativity time, no touchpad yet)

    - Support for battery powered devices
      (this mobile app gets status of this)

    - Fast connection
      If not connected yet, do scan and connects a device with strong signal (more close)
      Else, do scan and if located the last device, connect with it (no wait until end of scan)
      For Android 6 or higher you have to check if the GPS is turned on before scan.
      For this, if Android is 7 or higher, just connect with last device, no scan, very fast.
      And not need turn on GPS ;-)
      No scanned devices list, I no see it in comercial devices

    - Periodic feedback sends, to know if device is ok and to avoid it to enter in standby by inactivity
    
    - General utilities to use
    - Logging macros, to automatically put the function name
    
    - Read for internatiolization, most of string (code, ui, etc.) is in string.xml
      Have 2 string.xml: to english (direct in values) 
      and brazilian portuguese (due I am from Brazil).
      To add languages, just copy the string.xml to your languague directory and translate it.

    - Runs in Android simulator, without BLE stuff, due it not have Bluetooth.
      Usefull to help design UI and test in another devices models and screen sizes

## BLE messages

The communication between this App and ESP32 device is made by BLE messages.

This app act as BLE GATT client) and the ESP32 device act as BLE GATT server. 

For this project and mobile app, have only text delimited based messages.
First field of these messages is a code, that indicate the content or action of each message. 

Example:

    /**
    * BLE text messages of this app
    * -----------------------------
    * Format: nn:payload
    * (where nn is code of message and payload is content, can be delimited too)
    * -----------------------------
    * Messages codes:
    * 01 Initial
    * 10 Energy status(External or Battery?)
    * 11 Informations about ESP32 device
    * 70 Echo debug
    * 80 Feedback
    * 98 Restart the ESP32
    * 99 Standby (enter in deep sleep)
    **/

If your project needs to send much data,
I recommend changing to send binary messages.

This project is for a few messages per second (less than 20).
It is more a mobile app limit (more to Android).

If your project need send more, 
I suggest you use a FreeRTOS task to agregate data after send.
(this app supports large messages)

## Structure

Modules of Android example aplication - this is a Android Studio project:

```
 - EspApp   - The Android application directory (root)
    
    - app   - The Android application directory (module)

    - java  - Kotlin codes (Android Studio default for bitcode Java)

        - com.example   - App package
        
            - espapp        - For this app

                - activities    -   Android activities

                    - MainActivity          - Main activitie - main code of app
                    - ExceptionActivity     - To show app exceptions
                    - Fragment*             - Fragments activities
                            Note: have a template to a new fragment

                - adapters      - For UI adapters

                - helpers       - Helpers (utilities class as AppSettings and MessagesBLE)

                - models                    - Object models

        - util               - Utilities
            - bluetooth/*           - BLE comunication
            - extentions/*          - Kotlin extensions
            - DownloadManager       - To download files (not used in this app)
            - Extensions            - Good extensions to Swift language
            - Fields                - Used to extract fields in text delimited (as BLE messages)
            - FileUtil              - To files (not used in this app)
            - Log                   - For logging
            - Preferences           - To Android preferences
            - Util                  - General utilities routines

    - res   - resources of this app
            Note: have a template to a new fragment
``` 

Generally you do not need to change anything in the util directory. 
If you need, please add a Issue or a commit, to put it in repo, to help a upgrades in util

But yes in the other files, to facilitate, I put a comment "// TODO: see it" in the main points, that needs to see. so to start just find it in your IDE.

## EspApp

This app consists of following screens:

    - Connection (Connecting): when connecting a ESP32 device by BLE
    - MainMenu (EspAPP): Main menu of app
    - Informations: Show informations about ESP32 device connected and
      status of battery (if enabled)
    - Terminal BLE: See or send messages BLE
    - Settings: settings of app (disabled by default)
    - Disconnected: when a disconnect has been detected

For it each on have a fragment

And for main processing and BLE stuff is doneby  MainActivity

Have a templates to make a new ones (for example: for the settings)

See screenshots below

## Prerequisites 

    - Esp-Idf-Mobile-Apps-Esp32 EspApp flashed in ESP32 device
    - Android Studio 3.x  
    - Android device or simulator (only to see, not have Bluetooth)

## Install

To install, just download or use the "Github desktop new" app to do it (is good to updatings).

After open this in Android Studio

Please find all __"TODO: see it"__ occorences in all files, it needs your attention

And enjoy :-)

## Feedback and contribution

If you have a problem, bug, sugesttions to report,
 please do it in GitHub Issues.

Feedbacks and contributions is allways well come

Please give a star to this repo, if you like this.

## To-do

* See some warnings
* Documentation (doxygen)
* Tutorial (guide)
* To try auto reconnection in case of device disconnected
* Revision of translate to english (typing errors or mistranslated)

## Researchs used 

* Nordic github samples repos (very good) - https://github.com/NordicSemiconductor
* StackOverFlow for Android/Kotlin doubts or problems - https://stackoverflow.com

## Release History

* 0.1.0 - 25/08/18
    * First version

## Screenshots 

* Connection (Connecting): 

    - When connecting a ESP32 device by BLE

    ![Connection](https://imgur.com/0naYDmF)

* MainMenu (EspAPP): 

    - Main menu of app

    ![MainMenu](https://imgur.com/Re9gy85)

    - If device not enabled battery support 
    - (see bottom rigth that battery status is not showed)

    ![MainMenuNoBat](https://imgur.com/UzURiOY)

* Informations: 

    - Show informations about ESP32 device connected 
    - and status of battery (if enabled)

    ![Informations](https://imgur.com/v0T1rls)

* Terminal BLE: 

    - See or send messages BLE
    - Have a repeat funcion too (to send/receive repeated echo messages)

    ![Terminal](https://imgur.com/lplBMWV)

        Notes:  
            - See large info message receive with size of 207 bytes
            - See messages ends with [], the app translate codes to extra debug

* Disconnected: 

    - When a disconnect has benn detected

    ![Disconnected](https://imgur.com/Wud9zLf)

* Release 

    - When the app is a release version (to mobile stores)

    ![Release](https://imgur.com/6yj7EDi)

        Note: The Informations and Terminal BLE,
        as only enabled if in development (DEBUG)
        It is optimized to not process if a release app (no overheads)

        Tip: Not need delete it in your app, to have this tools while developing
