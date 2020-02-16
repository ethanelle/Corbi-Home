# Corbi Home
@ethanelle, @jsanch49, @sid-singh
### A brief overview of the project
Corbi Home is a home security app that connects facial recognition with motion tracking in your home. The system composes of a Raspberry Pi, multiple NodeMCUs, and a mobile app. When an intruder is detected by the initial camera, the motion sensors awaken and track movement throughout your home, alerting you of where the intruder currently stands.
### Contents
* [Setup and Running Programs](https://github.com/ethanelle/iot-project#setup-and-running-programs)
* [Android application](https://github.com/ethanelle/iot-project#android-app)
* [Raspberry Pi and camera](https://github.com/ethanelle/iot-project#raspberry-pi)
* [NodeMCUs and motion sensors](https://github.com/ethanelle/iot-project#nodemcus)
* [Web server](https://github.com/ethanelle/iot-project#web-server)

## Setup and Running Programs
### Android App
The app was built with Android Studio. It can be run on an emulator or on a phone by building the .apk file.
### Face Detection
This program was written completely in Python and made use of many Python libraries. Namely the modules, opencv-python and face_recognition, were used to detect faces with the frontal face classifier with the former module and perform object recognition with the latter module.

Navigate to the working directory iot-project/raspberry/FaceDetection/ and type 'python3 FaceDetection.py'. This will start the demo code. The executable will wait to focus on a single face in the frame then take a picture of the person. It will begin another thread to concurrently do face recognition on the image it had just captured and continue looking for the next face in focus. After the evaluation of is the recently captured image a known person, the program will upload the image to an AWS bucket for viewing on the Android App. If it is a known person, this will print welcome home. Known images are registered by <name>.jpeg stored in iot-project/raspberry/FaceDetection/whitelist/.

### Motion Sensors
The code for the motions sensors was built with Arduino IDE. The 2 external libraries needed are the [ESP8266](https://github.com/esp8266/Arduino) library and the [PubSubClient](https://github.com/knolleary/pubsubclient) library.

Motion sensor statuses are output to the Serial Monitor on channel 9600.

### Web server
The code used for the web server can be run with node: `node index.js {port}`.

The MQTT logger can be run with node: `node mqtt_logger.js`. The logger can be run from anywhere and will connect to the MQTT broker running on the EC2 instance.

## Android App
### States
|State|Description|
|---|---|
|Welcome|Simple, static state to welcome the user to the mobile app.|
|Camera feed|Gallery view of photos of a detected intruder, provided by the Raspberry Pi and camera.|
|Motion feed|Live feed list of rooms that motion has been detected in, provided by the NodeMCUs.|


**Screenshots of the app**

![Welcome screen](https://i.imgur.com/rxhWyPG.png)
![Camera feed](https://i.imgur.com/tY9dErk.png)
![Motion feed](https://i.imgur.com/lN79VSM.png)

### Source code breakdown

**Layouts**
* activity_main.xml
  * Main layout of the app: tabs and fragment placeholder for swapping fragments
* fragment_welcome.xml
  * Initial fragment loaded in, static
* fragment_camera.xml
  * Camera fragment that displays images in a gallery
* fragment_motion.xml
  * Motion list fragment that displays motion in a growing list
* image_table.xml
  * Sub-layout used in the camera fragment layout

**Back-end code**
* MainActivity.java
  * Main activity that all the fragments run inside, swapping is handled here
* WelcomeFragment.java
  * Initial fragment loaded in. Static and simple
* CameraFragment.java
  * Camera fragment that displays images pulled from the EC2 instance
* MotionFragment.java
  * Motion fragment that displays the motion list pulled from the EC2 instance

## Raspberry Pi
**raspberry/FaceDetections**

### OpenCV and facial recognition

## NodeMCUs
**raspberry/node_mcu/motion_and_mosquitto/**
### Motion detection
Attached to each NodeMCU is a PIR motion sensor. The sensor is a digital sensor that when detecting motion, sends a HIGH value to the NodeMCU.

**Source code:** Can be viewed at **motion_and_mosquitto.ino**

### Motion reporting
Each NodeMCU is connected to the internet via a mobile hotspot. The nodes communicate with a Mosquitto broker run on the EC2 instance. Each time motion is detected by the PIR sensor, a message is published on the 'motion' topic stating which room motion is detected in.

**Source code:** Can be viewed at **motion_and_mosquitto.ino**

## Web server
**webservice/**

The Ubuntu web server is running on an AWS EC2 instance. Attached to the server, for extended storage, is an S3 bucket.

### REST API
The REST API is the point of access for the Android app. The API has 2 GET routes: motion list and photos.

**Source code:** can be viewed at **webservice.js** and **index.js**

**API endpoints:**

* http://{hostname}:1997/motion_list
* http://{hostname}:1997/photos

### Mosquitto Broker and Logger
The NodeMCUs report motion back to the Mosquitto broker. The broker is started by simply running mosquitto in the terminal: `mosquitto -p 1883`. A NodeJS application listens to the 'motion' topic and logs any data published by a client into the 'motion_list.txt' file.

**Source code:** can be viewed at **mqtt_logger.js**
