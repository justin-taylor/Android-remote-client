# Remote Desktop Controller v1.5

Copyright 2010 Justin Taylor
This software can be distributed under the terms of the
GNU General Public License. 

This project was created for Android devices running 2.1 and higher.

The Remote Desktop controller can control the mouse and keyboard of a dekstop
computer from an Android device. There are two pieces of software needed for
this to run properly. The DesktopRemoteServer which runs on the users desktop
machine, and the Touchapp which runs on the user's Android device

## Remote Desktop Server
https://github.com/justin-taylor/Remote-Desktop-Server
This project must be imported into the eclipse workspace for the android
application to be ran.

## Android Remote Client
The Android app that send messages over wifi to the receiving server. The app is
divided into three classes.
	
2. Touch
    This view allows the user to adjust the settings of he app. The
    first setting is the port that the messages are sent over. This 
    must match the port set from the server UI on the DekstopRemoteServer.
    There is also a setting to control mouse sensitivity.

2. Controller
    This view is shown after the settings in touch are accepted. Listners
    receive user interactions, such as taps, movement and keyboard 
    interactions, and are then translated into messages to be sent over the
    UDP socket established in the AppDelegate (See section 2.C).

2. AppDelegate
    The AppDelegate bridges the gap between the Touch view (2.A) and the
    Controller view (2.B). The settings from the Touch view are used to
    create a UDP socket that will send messages from the Controller view
    to the receiveing DesktopRemoteServer (1). If there is a connection
    issue this class will close the Controller view and present the touch
    view displaying and message about the issue.

## Known Bugs

* Key Board Support:
    Not all Keys on the Android Keyboard are supported.
    Not entirely sure why. Some keys return the same
    key code in the onKey method Controller.java

* Sever Connection Test:
    There should be a way to ensure server connectivity
    before switching to the Controller view. Currently 
    a test message is sent to the server and listens for
    a message back (similar to a ping request), however
    the connection takes a couple of tries before connecting.
