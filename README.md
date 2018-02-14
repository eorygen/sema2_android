# SEMA2 Android App

## Project Summary:

SEMA (Smartphone Ecological Momentary Assessment) is a suite of software for designing and conducting smartphone-based survey research. SEMA v2 involves administering a survey several times per day (at random times) over several days. However, SEMA2 can also be used to administer surveys less frequently (e.g., once daily, as in daily diary studies), or on an ad hoc basis (i.e., participants launch the survey manually at any time). Following extensive testing of the initial version of SEMA in 2013-2014, SEMA2 was developed in 2015 by researchers at Australian Catholic University and Orygen-The National Centre of Excellence in Youth Mental Health, in collaboration with a private software developer, Boosted Human. SEMA2 includes a multitude of features that enable researchers to easily and intuitively create and administer smartphone surveys and easily access and analyse collected data.


## Authors:

Harrison, A., Harrsion, S., Koval, P., Gleeson, J., Alvarez, M. (2017). SEMA: Smartphone Ecological Momentary Assessment (Version 2). [Computer software]. https://github.com/eorygen

## Project hosting page:

https://github.com/eorygen/sema2_android

Alternatively, users who do not have git installed on their machine may download it by clicking on Clone or download button, click on Download ZIP. Note an uncompress tool is required to extract the project files from the ZIP file.


## Issue Tracker:

https://github.com/eorygen/sema2_android/issues


## Forum & Mailing List:

https://groups.google.com/forum/#!forum/sema-surveys


## Documentation:

https://github.com/eorygen/sema2_android/wiki


## Requirements:

 - Android Studio
 - Android SDK Tools


## Installation:

This installation guide assumes Android Studio and SDK Tools have been installed. For a guide on how to this, click on https://developer.android.com/studio/install.html and https://developer.android.com/studio/intro/update.html

It is recommended that you install Genymotion emulator free version to test the android app. Genymotion requires that you install VirtualBox VMWare (free) https://docs.genymotion.com/Content/01_Get_Started/Installation.htm

In Android Studio, import the android project. Click File > New > Import Project.

Open file local.properties and update the sdk path:

sdk.dir=/Path_to/Android/sdk

Click Build > Clean Project > Rebuild Project > Run. Select the emulator or device to deploy the app.

If you get project dependencies, that is, third party libraries sync issues in the app build.gradle. Please resolve them before moving on.

If you want to build and trace errors, open Terminal and type: 

$ ./gradlew assembleDebug --info --stacktrace
