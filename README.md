# cryptonets-android-sdk


This repository example doc for SDK.

## Table of Contents
- [Installation](#installation)
- [Usage](#usage)
- [API Documentation](#api-documentation)
- [Example App](#example-app)
    - [Installation](#example-app-installation)

## Installation

### Prerequisites
- Android Studio
- Minimum API level: 24
- ndkVersion = "26.1.10909125" 

### Instructions:

1. **Download library**: Download the `cryptonets-android-sdk.aar` file from  Release Section.
3. **Open project**: Launch Android Studio and open your project.
4. **Create libray directory**: Navigate to: `Project` → `app` → `right-click` → `New` → `Directory`, and name the new directory `libs`.
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
    - Click on the `Sync Now` button that appears on the toolbar to sync your project.

## Usage

## Initialize Session

Here's how to init the SDK :
Note: Don't call any function from UI thread


```kotlin
val config = PrivateIdentityConfig(sessionToken = "", baseUrl = "", debugLevel = DebugLevel.LEVEL_3)
try {
  val  privateIdentitySession = PrivateIdentitySDK.initialize(config)
} catch (e: Exception) {
   e.printStackTrace()
}
```

## Deinitialize Session

```kotlin
 PrivateIdentitySDK.deInitializeSession()
```

## API Documentation

The `lib` provides a set of functionalities to work with. Below are the methods available in the library:

Use `PrivateIdentitySDK.getInstantIdentitySession()` singleton to get session object when ever you need and use session object
to call any method 

### getVersion
```kotlin 
val session = PrivateIdentitySDK.getInstantIdentitySession()
val version = session.getVersion()
```

### validate
The funtion detect if face is found in image or not
```kotlin
   val result = privateIdentitySession.validate(bitmap = bitmap, ValidateConfig())
```
This method takes an image in the form of a `Bitmap` and returns its result in `json`

**Parameters:**

- `bitmap`:  It should be of type Bitmap and Make sure bitmap not rotated other then 0 degree.
- `ValidateConfig` an object for internal configurable settings
   - `skipAntispoof`  defaule value true

**Returns:**

- A `JSON` representing the face status.

**Example:**

```kotlin
 fun validImage(bitmap: Bitmap) {
   viewModelScope.launch(Dispatchers.Default) {
      _uiState.emit("Validating image")
      val privateIdentitySession = PrivateIdentitySDK.getInstantIdentitySession()
      val result = privateIdentitySession.validate(
         bitmap = bitmap, ValidateConfig()
      )
      _uiState.emit("Result ${result}")
   }
}
```



### Compare document and face

```kotlin
   val result =  privateIdentitySession.campareMugShort(userConfig = CompareFaceAndMugShortConfig(),userImage,cropIdImage)
```
This method takes an image of croped document witch have human face  in the form of a `Bitmap` and returns its result in `json`

**Parameters:**

- `bitmap`:  It should be of type Bitmap and Make sure bitmap not rotated other then 0 degree.
- `CompareFaceAndMugShortConfig` an object for internal configurable settings
   - `skipAntispoof`  defaule value true

**Returns:**

- A `JSON` representing the face status.

**Example:**

```kotlin
 fun compareTwoImage(userImage:Bitmap, cropIdImage:Bitmap){
        viewModelScope.launch(Dispatchers.IO) {
            privateIdentitySession.campareMugShort(userConfig = CompareFaceAndMugShortConfig(),userImage,cropIdImage)
        }
    }
}
```

### Compare Faces

```kotlin
   privateIdentitySession.compareFaces( compareFacesConfig= CompareFacesConfig(),faceBitmapOne,faceBitmapTwo)
```

**Parameters:**

- `bitmap`:  It should be of type Bitmap and Make sure bitmap not rotated other then 0 degree.
- `CompareFacesConfig` an object for internal configurable settings
   - `skipAntispoof`  defaule value true
   - `faceMatchingThreshold` default value 1.24

**Returns:**

- A `JSON` response.
  ```Kotlin
  ```

**Example:**

```kotlin
  fun compareTwoImage(){
        viewModelScope.launch(Dispatchers.IO) {
            val bitmapTwo = BitmapFactory.decodeResource(context.resources,R.drawable.ellon_image)
            val bitmapOne = BitmapFactory.decodeResource(context.resources, R.drawable.test_user_image)
            privateIdentitySession.compareFaces( compareFacesConfig= CompareFacesConfig(skipAntispoof = true),bitmapOne,bitmapTwo)
        }
    }
```


### Enroll
```kotlin
 val enrollConfig = EnrollConfig(mfToken = "You will get token once you give valid that have face first image/frame ")
 val resultJson = privateIdentitySession.enroll(bitmap = bitmap, enrollConfig = enrollConfig)
       
```
The method requires five consecutive valid facial images. A sequence of five images is considered consecutive if the same mf_token is received in each call. You will receive an mf_token after submitting a valid image. For each valid input, an mf_token is issued. If an empty or different mf_token is received at any point during the process, it indicates that you need to restart from the beginning. The mf_token ensures that each input belongs to the same individual and is error-free. 

**Parameters:**
- `Bitmap`: It should be valid image witch have human face and image must be  0 degree rotated.
- `EnrollConfig`
  - `skipAntispoof`  default value true
  -  `mfToken` You will get after first enrollment  

**Returns:**

- A `JSON` representing the enrollment status 

**Example**
Please check ExampleEnroll.kt in the main brach

## Predict
Perform predict (authenticate a user) after enroll user, this method return GUID/PUID if predict successful else check, check status, face  validation status, and anti spoof status code from json response you will, you can get code description from **Enroll**, However, if the user is not enrolled in the system, the predict call will return a status of -1 along with the message 'User not enrolled.'

```kotlin
 val result = privateIdentitySession.predict(bitmap = bitmap, PredictConfig())
```

**Parameters:**
- `bitmap`: Image that contain valid human face
- `PredictConfig`: Configurable params
   - `skipAntispoof` default value is true

**Returns:**
- `JSON` data that either GUID/PUID or status code, face validating status, anti spoof status  if not GUID/PUID 

**Example**
```kotlin
fun predictFace(bitmap:Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
                val result = privateIdentitySession.predict(bitmap = bitmap, PredictConfig())
                 if(result.toPOJO().guid!=null){
                     Log.d(TAG,"success")
                 }else{
                     //check 
                     Log.d(TAG,"fail")
                 }
            }
        }
    
```



## FrontDocumentScan
```kotlin
  val result = privateIdentitySession.frontDocumentScan(bitmap, IdDocumentFrontScanConfig())
```

This method accept valid photo of front side of ID document in witch have a human face, and return cropped ID, mugshot and Json response

**Parameters:**
- `bitmap`: Image that contain valid photo ID
- `IdDocumentFrontScanConfig`: Configurable params
   - `skipAntispoof` default value true
   - `thresholdDocX` default value 0.2
   - `thresholdDocY` default value 0.2
    - `documentAutoRotation` default value 0.2

**Returns:**
- ```ScanDocumentsFront``` object witch have ```JSON```,Cropped document and mugshot if available , if not available please check ```val``` from ``` result.getResponse().docFace.documentData.documentValidationStatus```


**documentValidationStatus**
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



## BackDocumentScan
```kotlin
 val result = privateIdentitySession.backDocumentScan(bitmap = bitmap, IdDocumentBackScanConfig())
```

This method valid photo of document witch have Bar code PDF 417 barcode, the higger the quilty of image the batter chance it will read data from barcode, if you want just scan bar for faster result then use `document_scan_barcode_only` , and it return `ScanDocumentsBack` Cropped bitmap, Cropped Barcode and result.

**Parameters:**
- `bitmap`: Image that contain valid id document with barcode PDF 417 format.
- `IdDocumentFrontScanConfig`: Configurable params
   - `documentScanBarcodeOnly` default value true
   - `skipAntispoof` default value false
   - `thresholdDocX` default value 0.2
   - `thresholdDocY` default value 0.2
   - `documentAutoRotation` default value 0.2

**Returns:**
Return `ScanDocumentsBack` witch have cropped document, cropped barcode,result.

**Example**

```kotlin
  val result = privateIdentitySession.frontDocumentScan(bitmap, DocumentConfig())
  val documentValidationStatus = result.getResponse().docFace.documentData.documentValidationStatus
 if (documentValidationStatus==0){
     val cropDocBitmap = result.cropDoc()
     val cropFaceBitmap = result.cropBarCode()
 }else{
     Log.d(TAG,"error  code : ${documentValidationStatus}") // Please check 
 }
```

**documentValidationStatus Code**
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


----------------------

Face Captcha Status:

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

----------------------

Antispoof Status:

* -100 Invalid Image
* -5 Greyscale Image
* -4 Invalid Face
* -2 Mobile phone detected
* -1 No Face Detected
* 0 Real
* 1 Spoof

----------------------














