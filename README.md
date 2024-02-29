# cryptonets-android-sdk

# Cryptonets Android Library

This repository contains the `lib` and an example application that demonstrates its usage.

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

1. **Download library**: Download the `lib-release.aar` file from this [link](https://drive.google.com/file/).
3. **Open project**: Launch Android Studio and open your project.
4. **Create libray directory**: Navigate to: `Project` → `app` → `right-click` → `New` → `Directory`, and name the new directory `libs`.
5. **Copy library file**: Copy the downloaded `lib-release.aar` file into the `libs` directory.
6. **Update Build Configuration**
    - Open  your app’s `build.gradle`  or `build.gradle.kts`
    - Add the following implementation according to your script type:

   Groovy
   ```groovy
   implementation files('libs/lib-release.aar')
   ```

   Kotlin
   ```kotlin
   implementation(files("$projectDir/libs/lib-release.aar"))
   ```

7. **Sync project**
    - Click on the `Sync Now` button that appears on the toolbar to sync your project.

## Usage

Here's how to init the SDK :
Note: Don't call any function from UI thread

```kotlin
val config = PrivateIdentityConfig(key = "", url = "")
try {
  val  privateIdentitySession = PrivateIdentitySDK.initialize(config)
} catch (e: Exception) {
   e.printStackTrace()
}
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
```kotlin
   val result = privateIdentitySession.validate(bitmap = bitmap, ValidateConfig())
```
This method takes an image in the form of a `Bitmap` and returns its result in `json`

**Parameters:**

- `bitmap`:  It should be of type Bitmap and Make sure bitmap not rotated other then 0 degree.
- `ValidateConfig` an object for internal configurable settings

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



### Enroll
```kotlin
 val enrollConfig = EnrollConfig(mf_token = "You will get token once you give valid first image/frame ")
 val resultJson = privateIdentitySession.enroll(bitmap = bitmap, enrollConfig = enrollConfig)
       
```
The method accept 5 consecutive valid image of face, if you pass any invalid you will get empty `mf_token`, you have to start from start, this method is sync method that mean it will run same thread where you call it 

**Parameters:**
- `bitmap`: It should be valid image witch have human face and image must be  0 degree rotated.
- `enrollConfig` this enrollConfig have  non optional property `mf_token` witch is can't be null, first time `mf_token` can be empty once you have fist valid enroll you will get `mf_token` in response now pass this token to call 2nd time.

**Returns:**

- A `JSON` representing the enrollment status 
 ```kotlin 
{"status":0,"message":"Success","validation_status":[{"status":0,"face_confidence_score":0.6835715770721436,"anti_spoof_status":0,"anti_spoof_performed":true,"enroll_performed":false,"enroll_token":"2024-02-09_22-58-00-550830"}]} 
```

From response check if you have `enroll_token` that mean every thing goes right, if you received null/empty in this case check  `status`, `validation_status[index].status` and `anti_spoof_status`

**status codes**

```kotlin
private var noError = 0
private val faceNotDetected = 1
private val invalidSessionHandler = 2
private val invalidJsonConfiguration = 4
private val invalidInputImageData = 5
private val networkError = 6
private val faceFoundWithInvalidFaceStatus = 7
private val unhandledException = 8
private val SpoofDetected = 10
```

 ***if status == 0 then check **face status**, you can get face status from validation_status[index].status***

```kotlin
private val Err = -100
private val faceNotDetected = -1
private val Ok = 0
private val faceTooClose = 3
private val faceTooFar = 4
private val faceRight = 5
private val faceLeft = 6
private val faceUp = 7
private val faceDown = 8
private val imageBlurr = 9
private val faceWithGlass = 10
private val faceWithMask = 11
private val lookingLeft = 12
private val lookingRight = 13
private val lookingHigh = 14
private val lookingDown = 15
private val faceTooDark = 16
private val faceTooBright = 17
private val faceLowValConf = 18
private val invalidFaceBackground = 19
private val eyeBlink = 20
private val mouthOpened = 21
private val faceRotatedRight = 22
private val faceRotatedLeft = 23 
```

 ***if `validation_status[index].status==0`  then check **anti spoof status** `anti_spoof_status`

```kotlin
val RESULT_GENERIC_ERROR = -100
val RESULT_GRAYSCALE = -5
val RESULT_INVALID_FACE = -4
val RESULT_FACE_TOO_CLOSE_TO_EDGE = -3
val RESULT_MOBILE_PHONE_DETECTED = -2
val RESULT_NO_FACE_DETECTED = -1
val RESULT_NO_SPOOF_DETECTED = 0
val RESULT_SPOOF_DETECTED = 1 
```

**Example:**
```kotlin
EnrollScreenViewModel.kt

    private var enroll1FAProgressStatus: Progress1FAProgressStatus =
   Progress1FAProgressStatus(0, null)
    fun enrollFace(bitmap: Bitmap) {
        if (stillProcesingOldImage || stopEnroll) {
            return
        }
        viewModelScope.launch(Dispatchers.Default) {
            stillProcesingOldImage = true
            val enrollConfig = EnrollConfig(mf_token = enroll1FAProgressStatus?.enrolFaceResponse?.validationStatus?.firstOrNull()?.enroll_token ?: "")
            val resultJson = privateIdentitySession.enroll(bitmap = bitmap, enrollConfig = enrollConfig)
            if (resultJson.isNotEmpty()) {
                val enrolFaceResponse = EnrolFaceResponse.fromJson(resultJson)
                val enrolFaceValidationStatus = enrolFaceResponse.validationStatus?.firstOrNull()
                if (enroll1FAProgressStatus.progress == 0 && !enrolFaceValidationStatus?.enroll_token.isNullOrEmpty()) {
                    enroll1FAProgressStatus =
                        Progress1FAProgressStatus(progress = 20, enrolFaceResponse)
                } else if (enrolFaceResponse.guid.isNotEmpty() && enrolFaceResponse.puid.isNotEmpty()) {
                    enroll1FAProgressStatus = Progress1FAProgressStatus(
                        100,
                        enrolFaceResponse
                    )
                    SharedPref.saveString(SharedPref.KEY_PUID,enrolFaceResponse.puid)
                } else if (enrolFaceValidationStatus?.enroll_token.isNullOrEmpty() || enroll1FAProgressStatus?.enrolFaceResponse?.validationStatus?.firstOrNull()?.enroll_token != enrolFaceValidationStatus?.enroll_token) {
                    enroll1FAProgressStatus = Progress1FAProgressStatus(0, enrolFaceResponse)

                } else {
                    enroll1FAProgressStatus = Progress1FAProgressStatus(
                        enroll1FAProgressStatus!!.progress + 20,
                        enrolFaceResponse
                    )
                }

            } else {
                throw EnrollUserFailException("Unknown error while getting response from backend ")
            }
            stillProcesingOldImage = false
            _progressMutableLiveData.postValue(
                enroll1FAProgressStatus
            )
        }
    }
```
```kotlin
Progress1FAProgressStatus.kt

class Progress1FAProgressStatus ( val progress: Int, val enrolFaceResponse: EnrolFaceResponse?)
```



## Predict
Perform predict (authenticate a user) after enroll user, this method return GUID/PUID if predict successful else check, check status, face status, and anti spoof status code, you can get code description from **Enroll**, However, if the user is not enrolled in the system, the predict call will return a status of -1 along with the message 'User not enrolled.'

```kotlin
 val result = privateIdentitySession.predict(bitmap = rotatedBitmap, PredictConfig())
```

**Parameters:**
- `bitmap`: Image that contain valid human face
- `DocumentConfig`: Configurable params

**Returns:**
- `JSON` data that either GUID/PUID or status code, face validating status, anti spoof status  if not successfully 

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
  val result = privateIdentitySession.frontDocumentScan(bitmap, DocumentConfig())
```

This method accept valid photo of front side of ID document in witch have a human face, and return cropped ID, mugshot and Json response

**Parameters:**
- `bitmap`: Image that contain valid photo ID
- `DocumentConfig`: Configurable params  

**Returns:**
- ```ScanDocumentsFront``` object witch have ```JSON```,Cropped document and mugshot if available , if not available please check ```op_status``` from ```JSON```

```JSON
{"error":0,"payload_type":"face_id","image_width":1440,"image_height":1920,"doc_center_x":675.0,"doc_center_y":648.0,"doc_x1":82.0,"doc_y1":321.0,"doc_x2":1222.0,"doc_y2":265.0,"doc_x3":1266.0,"doc_y3":972.0,"doc_x4":120.0,"doc_y4":1028.0,"conf_level":0.9587152600288391,"cropped_doc_width":0,"cropped_doc_height":0,"cropped_doc_channels":1,"doc_validation_status":2,"puid":"","guid":"","predict_message":"","face_validity_message":"Not requested","op_message":"Document is blurry","predict_status":-1,"enroll_level":0,"face_valid":0,"op_status":2,"cropped_face_width":0,"cropped_face_height":0,"cropped_face_size":0,"cropped_face_channels":1}
```
**Op_Status Code**
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
 val result = privateIdentitySession.backDocumentScan(bitmap = bitmap, IdDocumentBackScanConfig(false))
```

This method valid photo of document witch have Bar code PDF 417 barcode, the higger the quilty of image the batter chance it will read data from barcode, if you want just scan bar for faster result then use `document_scan_barcode_only` , and it return `ScanDocumentsBack` witch contain three byteArray (Cropped Document, Cropped Barcode, and result)  and Cropped bitmap, Cropped Barcode and JSON.

**Parameters:**
- `bitmap`: Image that contain valid id document with barcode PDF 417 format.
- `IdDocumentBackScanConfig`: Configurable params witch required `document_scan_barcode_only` .

**Returns:**
Return `ScanDocumentsBack` witch have cropped document byteArray and Bitmap, cropped barcode byteArray and bitmap, result  byteArray and json.

**Example**

```kotlin
  val result = privateIdentitySession.frontDocumentScan(bitmap, DocumentConfig())
  val op_status = result.getJsonData().toPOJO().op_status
 if (op_status==0){
     val cropDocBitmap = result.cropDoc()
     val cropFaceBitmap = result.cropBarCode()
 }else{
     Log.d(TAG,"error  code : ${op_status}") // Please check 
 }
```

**Op_Status Code**
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















