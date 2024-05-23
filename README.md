# Cryptonets Android SDK

## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
- [API Documentation](#api-documentation)

## Overview

Private ID Android SDK supports user registration with identity proofing, and user face login with FIDO Passkey, using Cryptonets fully homomorphically encrypted (FHE) for privacy and security.

Features:
- Biometric face registration and authentication compliant with IEEE 2410-2021 Standard for Biometric Privacy, and exempt from GDPR, CCPA, BIPA, and HIPPA privacy law obligations.
- Face registration and 1:n face login in 200ms constant time
- Biometric age estimation with full privacy, on-device in 20ms
- Unlimited users (unlimited gallery size)
- Fair, accurate and unbiased
- Operates online or offline, using local cache for hyper-scalability

Builds
- Verified Identity
- Identity Assurance
- Authentication Assurance
- Federation Assurance
- Face Login
- Face Unlock
- Biometric Access Control
- Account Recovery
- Face CAPTCHA

## Installation

### Requirements
- Android Studio
- Minimum API level: 24
- ndkVersion = "26.1.10909125" 

### Steps:

1. **Download library**: Download the `cryptonets-android-sdk.aar` file from the [Release]([https://drive.google.com/file/](https://github.com/prividentity/cryptonets-android-sdk/releases)) section.
3. **Open project**: Launch Android Studio and open your project.
4. **Create library directory**: Navigate to: `Project` → `app` → `right-click` → `New` → `Directory`, and name the new directory `libs`.
5. **Copy library file**: Copy the downloaded `cryptonets-android-sdk.aar` file into the `libs` directory.
6. **Update Build Configuration**
    - Open  your app’s `build.gradle`  or `build.gradle.kts`
    - Add the following implementation according to your script type:

   Groovy
   ```groovy
   implementation files('libs/cryptonets-android-sdk.aar')
   ```

   Kotlin
   ```kotlin
   implementation(files("$projectDir/libs/cryptonets-android-sdk.aar"))
   ```

7. **Sync project**
    - Click the `Sync Now` button on the toolbar to sync your project.

## API Documentation

Use the `PrivateIdentitySDK.getInstantIdentitySession()` singleton to get the session object whenever you need it, and use the session object to call any method. 

### Version

A function that returns the current SDK version.

**Returns:**

- `String`: value for the current version.

**Example:**

```kotlin 
val session = PrivateIdentitySDK.getInstantIdentitySession()
val version = session.getVersion()
```

### Initialize Session

A method that creates the session for SDK work. It saves the session pointer inside the SDK and can be used for other methods. Please use it before making any other calls.
**NB:** Don't call any function from the UI thread during the session initialization.

```kotlin
fun initialize(privateIdentityConfig: PrivateIdentityConfig): PrivateIdentitySession
```

**Parameters:**

- `privateIdentityConfig: PrivateIdentityConfig`: session initialization parameters.

**Returns:**

- `PrivateIdentitySession` session wrapper for calling other functions. 

**Example:**

```kotlin
val config = PrivateIdentityConfig(sessionToken = "", baseUrl = "", debugLevel = DebugLevel.LEVEL_3)
try {
  val  privateIdentitySession = PrivateIdentitySDK.initialize(config)
} catch (e: Exception) {
   e.printStackTrace()
}
```

### Deinitialize Session

A method that deinitializes the session created before. You can call this function when you no longer need SDK in your work, so it frees memory and closes the session.

```kotlin
fun deInitializeSession()
```

**Example:**

```kotlin
PrivateIdentitySDK.deInitializeSession()
```

### Validate Face

A function that detects if there is a valid face on the input image.

```kotlin
fun validate(bitmap: Bitmap, baseUserConfig: ValidateConfig): String
```

**Parameters:**

- `bitmap: Bitmap`: input image for validation. Make sure the bitmap doesn't rotate other than 0 degrees.
- `baseUserConfig: ValidateConfig`: user's config for changing settings.

The `ValidateConfig` has default values:

1) `skipAntispoof` - `true`: anti-spoof is not enabled by default.

**Returns:**

- `String`: a `JSON` representing the face status.

**Example:**

```kotlin
fun validImage(bitmap: Bitmap) {
   viewModelScope.launch(Dispatchers.Default) {
      // _uiState.emit("Validating image")
      val privateIdentitySession = PrivateIdentitySDK.getInstantIdentitySession()
      val result = privateIdentitySession.validate(
         bitmap = bitmap, ValidateConfig()
      )
      // _uiState.emit("Result ${result}")
   }
}
```

### Enroll Person

Perform a new enrollment (register a new user) using the enroll function. The function will collect 5 consecutive, valid faces to be able to enroll. Using configuration, we must pass the same `mfToken` (Multiframe token) on success. If the `mfToken` value changes, we will have an invalid enrollment image and start again from the beginning. **Note:** 5 consecutive faces are needed. When enrollment is successful after 5 consecutive valid faces, enroll returns the enrollment result.

```kotlin
fun enroll(bitmap: Bitmap, enrollConfig: EnrollConfig): String
```

**Parameters:**
- `bitmap: Bitmap`: input image for enrolment. Make sure the bitmap doesn't rotate other than 0 degrees.
- `enrollConfig: EnrollConfig`: user's config for changing settings.

The `EnrollConfig` has default values:

1) `skipAntispoof` - `true`: anti-spoof is not enabled by default.
2) `mfToken`: you will get it after first enrollment.

**Returns:**

- `String`: a `JSON` representing the face status. 

**Example:**

```kotlin
 val enrollConfig = EnrollConfig(mfToken = "You will get token once you give valid that have face first image/frame ")
 val resultJson = privateIdentitySession.enroll(bitmap = bitmap, enrollConfig = enrollConfig)
       
```

**Note:** Please check ExampleEnroll.kt in the `main` branch.

### Predict Person

Perform predict (authenticate a user) after enrolling the user. This method returns a GUID/PUID if the prediction is successful; otherwise, face validation status and anti-spoof status code from the JSON response. You can get code descriptions at the end of the documentation. However, if the user is not enrolled in the system, this call will return a status of -1 and the message "User not enrolled."

```kotlin
fun predict(bitmap: Bitmap, preidctConfig: PredictConfig): String
```

**Parameters:**
- `bitmap: Bitmap`: input image for enrolment.
- `preidctConfig: PredictConfig`: user's config for changing settings.

The `PredictConfig` has default values:

1) `skipAntispoof` - `true`: anti-spoof is not enabled by default.  

**Returns:**

- `String`: a `JSON` data that either GUID/PUID or status code, face validating status, anti spoof status.

**Example:**

```kotlin
fun predictFace() {
    viewModelScope.launch(Dispatchers.IO) {
        val result = privateIdentitySession.predict(bitmap = bitmap, PredictConfig())
        if (result.toPOJO().guid != null) {
            Log.d(TAG,"success")
        } else {
            //check 
            Log.d(TAG,"fail")
        }
    }
}
```

### Compare Document and Face

```kotlin
fun campareMugShort(userConfig: CompareFaceAndMugShortConfig, salfieBitmap: Bitmap, cropIdDocument: Bitmap): String
```

**Parameters:**

- `cropIdDocument: Bitmap`: user's document image.
- `salfieBitmap: Bitmap`: user's face image.
- `userConfig: CompareFaceAndMugShortConfig`: user's config for changing settings.

The `DocumentAndFaceConfig` has default values:

1) `skipAntispoof` - `true`: anti-spoof is not enabled by default.

**Returns:**

- `String`: a `JSON` representing the face status.

**Example:**

```kotlin
fun compareTwoImage(userImage:Bitmap, cropIdImage:Bitmap){
    viewModelScope.launch(Dispatchers.IO) {
        privateIdentitySession.compareDocumentAndFace(
            userConfig = CompareFaceAndMugShortConfig(), userImage, cropIdImage)
    }
}
```

A sample JSON result:

```json
{
 "call_status": {
  "return_status": 0,
  "operation_tag": "compare_mugshot_and_face",
  "return_message": "",
  "mf_token": "",
  "operation_id": 17,
  "operation_type_id": 12
 },
 "face_compare": {
  "result": 0,
  "a_face_validation_status": 0,
  "b_face_validation_status": 0,
  "distance_min": 0.964277208,
  "distance_mean": 0.964277208,
  "distance_max": 0.964277208,
  "conf_score": 56.8121185,
  "face_thresholds": [],
  "document_data": {
   "document_conf_level": 0,
   "cropped_document_image": {
    "info": {
     "width": 112,
     "height": 112,
     "channels": 4,
     "depths": 0,
     "color": 4
    },
    "data": ""
   },
   "document_validation_status": 0,
   "status_message": "",
   "mrz_text": []
  },
  "cropped_face_image": {
   "info": {
    "width": 112,
    "height": 112,
    "channels": 4,
    "depths": 0,
    "color": 4
   },
   "data": ""
  }
 }
}
```

### Front Document Scan

This function allows you to scan data from the front side of the document (government ID or driver's license). This method accepts a valid image of the front side of the ID document with a mugshot and returns a cropped document and mugshot images as well as a resulting JSON document.

```kotlin
fun frontDocumentScan(bitmap: Bitmap, idDocumentFrontScanConfig: IdDocumentFrontScanConfig): ScanDocumentsFront
```

**Parameters:**

- `bitmap: Bitmap`: input image for scanning document.
- `idDocumentFrontScanConfig: IdDocumentFrontScanConfig`: user's config for changing settings.
  
The `IdDocumentFrontScanConfig` has default values:

2) `skipAntispoof` - `true`: anti-spoof is not enabled by default.
3) `thresholdDocX` - `0.2`: the minimal allowed distance (as the ratio of input image width) between the detected document edge and the left/right sides of the input image.
4) `thresholdDocY` - `0.2`: the minimal allowed distance (as the ratio of input image height) between the detected document edge and the top/bottom sides of the input image.
5) `documentAutoRotation` - `true`: If the value is 'true,' the function will rotate the input image several times for better detection results.

**Returns:**

- `ScanDocumentsFront`: an object that contains JSON result, recognized document image, and mugshot image (face image of the front document).

**Example:**

```kotlin
fun scanDocumentsFront(bitmap: Bitmap) {
    val result = privateIdentitySession.frontDocumentScan(
        bitmap = bitmap, IdDocumentFrontScanConfig(
            skipAntispoof = true, thresholdDocY = 0.0, thresholdDocX = 0.0))
    updateUi(result)
}
```

### Back Document Scan

This function allows you to scan data from the back side of the document (government ID or driver's license). This method accepts a valid image of the back side of the ID document with a PTD417 barcode. It returns a cropped document and barcode images, as well as a resulting JSON document that contains barcode parsing results, if any. Note: high input image resolution is important for better barcode parsing results.

```kotlin
fun backDocumentScan(bitmap: Bitmap, documentConfig: IdDocumentBackScanConfig): ScanDocumentsBack
```

If you want to scan the barcode, use the `documentScanBarcodeOnly` parameter equal to `true` for faster processing.

**Parameters:**

- `image: UIImage`: input image for scanning document.
- `config: DocumentBackScanConfig`: user's config for changing settings.

The `DocumentBackScanConfig` has default values:

1) `skipAntispoof` - `true`: anti-spoof is not enabled by default.
2) `documentScanBarcodeOnly` - `true`: if you need to scan the whole document, you should use the `false` value here.
3) `thresholdDocX` - `0.2`: the minimal allowed distance (as the ratio of input image width) between the detected document edge and the left/right sides of the input image.
4) `thresholdDocY` - `0.2`: the minimal allowed distance (as the ratio of input image height) between the detected document edge and top/bottom sides of the input image.

**Returns:**

- `ScanDocumentsBack`: an object that contains a `JSON` result, recognized document image (if any), and barcode image.

**Example:**

```kotlin
fun scanDocumentsBack(bitmap: Bitmap) {
    val result = privateIdentitySession.backDocumentScan(
        bitmap = bitmap, IdDocumentBackScanConfig(
            skipAntispoof = true, thresholdDocY = 0.0, thresholdDocX = 0.0))
    updateUi(result)
}
```

## SDK Status Codes

### Face Validation Status

* -100 Internal Error
* -1 No Face Found
* 0 Valid Face
* 1 Image Spoof (Not Used)
* 2 Video Spoof (Not Used)
* 3 Too Close
* 4 Too Close
* 5 Too far to right (Close to right edge of image)
* 6 Too far to left (Close to left edge of image)
* 7 Too far up (Close to top edge of image)
* 8 Too far down (Close to bottom edge of image)
* 9 Too Blurry
* 10 Glasses Detected
* 11 Facemask Detected
* 12 Chin too far left
* 13 Chin too far right
* 14 Chin too far up
* 15 Chin too far down
* 16 Image too dim
* 17 Image too bright
* 18 Face low confidence value
* 19 Invalid face background (Not used)
* 20 Eyes blink
* 21 Mouth Open
* 22 Face tilted right
* 23 Face rotated left

### Anti-spoof Status

* -100 Invalid Image
* -5 Greyscale Image
* -4 Invalid Face
* -2 Mobile phone detected
* -1 No Face Detected
* 0 Real
* 1 Spoof

### Document Validation Status

```kotlin
val INVALID_IMAGE = -100
val SYSTEM_ERROR = -2
val MOVE_CLOSE/INVALID_DOCUMENT = -1
val SUCCESS =0
val DOCUMENT_IS_BLURRY = 2 
val UNABLE_TO_PARSE_DOCUMENT = 3
val TOO_FAR = 4
val TOO_FAR_LEFT =5 
val TOO_FAR_RIGHT = 6 
val TOO_FAR_UP = 7 
val TOO_FAR_DOWN = 8
val MUGSHOT_IS_BLURRY_OR_FINGER_BLOCKING_THE_DOCUMENT = 9 
val DOCUMENT_FOUND_IS_NOT_VALID_FRONT_DOCUMENT = 18
```
