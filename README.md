# ARIN
## Augmented Reality for Indoor Navigation
Augmented Reality for Indoor Navigation System - An Android application for navigating user in an indoor environment with augmented arrows, without continuous data connectivity.
You can find a demomstration video here: https://www.youtube.com/watch?v=XtAy7S753eQ<BR>
The application consists of 3 main modules:
  1. Source Detection (using text recognition for boards)
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

### Updates
Google demonstrated AR navigation already integrated with GMaps in Pixel3a in its I/O 2019. Awaiting the same beauty and exerience for indoor navigation... :)
Also it has pushed for more on-device applications and algorithms so as to enhance privacy and minimise the need to share user data. Pushes this project idea further, yay!

### References

#### MLkit text recogniser tutorial
https://www.youtube.com/watch?v=T0273WiUQPI
#### Google io 2018 code labs
https://codelabs.developers.google.com/io2018/
#### Sceneform ar using arcore in android studio
https://codelabs.developers.google.com/codelabs/sceneform-intro/index.html?index=..%2F..index#0
https://medium.freecodecamp.org/how-to-build-an-augmented-reality-android-app-with-arcore-and-android-studio-43e4676cb36f
https://proandroiddev.com/building-arcore-apps-using-sceneform-part-4-9bb8374eaab4
#### Sceneform Quaternion handling for rotational orientation update
https://proandroiddev.com/arcore-cupcakes-4-understanding-quaternion-rotations-f90703f3966e
#### Pedometer
http://www.gadgetsaint.com/android/create-pedometer-step-counter-android/#.XPJx8vZuLIU
