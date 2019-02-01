<div style="width:100%">
<div style="width:100%">
	<div style="width:50%; display:inline-block">
		<p align="center">
		<img align="center" width="180" height="180" alt="" src="https://github.com/cometchat-pro/ios-swift-chat-app/blob/master/Screenshots/CometChat%20Logo.png">	
		</p>	
	</div>	
</div>
</br>
</br>
</div>

CometChat Android Demo app (built using **CometChat Pro**) is a fully functional messaging app capable of **one-on-one** (private) and **group** messaging. The app enables users to send **text** and **multimedia messages like audio, video, images, documents.**

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](#)      [![Platform](https://img.shields.io/badge/Language-Kotlin-yellowgreen.svg)](#)

## Table of Contents

1. [Screenshots ](#screenshots)

2. [Installation ](#installtion)

3. [Run the Sample App ](#run-the-sample-app)

4. [Customizing the UI](#customizing-the-ui)

5. [Integrating this sample into your own app](#integrating-this-sample-into-your-own-app)

5. [Contribute](#contribute)



## ScreenShots

 <img align="left" src="https://github.com/cometchat-pro/android-kotlin-chat-app/blob/master/ScreenShot/screenshots.png">

## Installtion

   Simply Clone the project from android-kotlin-chat-app repository and open in Android Studio.
   Build the Demo App and it will be ready to Run




## Run the Sample App



   To Run to sample App you have to do the following changes by Adding **ApiKey** and **AppId**

   - Open the Project in Android Mode in Android Studio

   - Go to Under java --> com\inscripts\cometchatpulse--> StringContract

   - Under class `StringContract.kt`  go to `class` named `AppDetails`

  -  modify *APP_ID* and *API_KEY* with your own **ApiKey** and **AppId**

       `val APP_ID: String = "XXXXXXXXX"`

       `val API_KEY: String  = "XXXXXXXXX"`

## Note

   You can Obtain your  *APP_ID* and *API_KEY* from [CometChat-Pulse Dashboard](https://app.cometchat.com/)

   For more information read [CometChat-Pro Android SDK](https://prodocs.cometchat.com/docs/android-quick-start) Documentation




  ![Studio Guide](https://github.com/cometchat-pro/android-kotlin-chat-app/blob/master/ScreenShot/guide.png)

 ## Customizing the UI

 We have provided three themes with our sample app namely **PersianBlue, MountainMeadow, AzureRadiance**. To apply the themes:

   - Go to  `Application` class `CometChatPro`

   - Create constructor of  class `Appearance` and pass any of these three values

      1.PERSIAN_BLUE

      2.MOUNTAIN_MEADOW

      3.AZURE_RADIANCE

   example `Appearance(Appearance.AppTheme.PERSIAN_BLUE)`

 To make your custom appearance go to `Appearance` class under package named Utils i.e

    Go to Under java --> com\inscripts\cometchatpulse--> Utils--> Appearance

   <p align="center">
 <img align="center" width="708.5" height="680" src="https://github.com/cometchat-pro/android-kotlin-chat-app/blob/master/ScreenShot/gib.gif">
</p>


## Integrating this sample into your own app
  Copy sample app in your project

  remove login and add your own authentication method

  launch `MainActivity` and start using the app

## Contribute


 Feel free to make Pull Request.
   
