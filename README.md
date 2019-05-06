# ARIN
## Augmented Reality for Indoor Navigation
Augmented Reality for Indoor Navigation System - An Android application for navigating user in an indoor environment with augmented arrows, without continuous data connectivity.
The application consists of 3 main modules:
  1. Source Detection (using text recognition of boards)
  2. Navigation (restricted to 1 floor)
  3. AR render (Sceneform render of ARCore)

## Source Detection
Source detection includes identification of nearby landmarks (which are mostly boards, logos and objects). In the given code, the boards can be detected using text recognition by MLKit.

## Navigation
Navigation can be developed using all sensors of a smart phone - accelerometer (for step counts), magnetometer (for direction) and barometer(for multiple floors).
This code consists of restricted navigation support for a particular indoor geographical space only.

## ARCore
AR applications can be developed using any AR SDKs on a suitable platform. Some of the popular ones include Vuforia, ARCore, ARKit.
For this application, we have used ARCore in Android Studio. For the same, we use ARFragment class to create a fragment for AR View in our application.
