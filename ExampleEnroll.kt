package com.privateid.privateid.demo.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.privateid.facematch.nativelib.PrivateIdentitySDK
import com.privateid.facematch.nativelib.model.EnrollConfig


// Response class for face enrollment
class EnrolFaceResponse(
    @SerializedName("call_status") val callStatus: CallStatus,
    @SerializedName("enroll_onefa") val enrollOneFa: EnrollOneFa? = null,
) {

    // Data class representing the status of a call
    class CallStatus(
        @SerializedName("return_status") val returnStatus: Int = Int.MIN_VALUE,
        @SerializedName("operation_tag") val operationTag: String? = null,
        @SerializedName("return_message") val returnMessage: String? = null,
        @SerializedName("mf_token") val mfToken: String? = null,
    )

    // Data class representing face validation data
    data class FaceValidationData(
        @SerializedName("message") var message: String? = null,
        @SerializedName("face_validation_status") var faceValidationStatus: Int? = null,
        @SerializedName("face_confidence_score") var confScore: Double? = null,
        @SerializedName("antispoofing_status") var antiSpoofStatus: Int? = null
    )

    // Data class representing enrollment of one face
    data class EnrollOneFa(
        @SerializedName("face_validation_data") val faceValidationData: FaceValidationData,
        @SerializedName("api_response") val enrollFaceApiResponse: EnrollFaceApiResponse,
        @SerializedName("encrypted_embeddings") val encryptedEmbeddings: String? = null
    )

    // Class representing the API response for face enrollment
    class EnrollFaceApiResponse(
        @SerializedName("status") var status: Int? = null,
        @SerializedName("message") var message: String = "",
        @SerializedName("puid") var puid: String = "",
        @SerializedName("guid") var guid: String = ""
    )

    // Convert the response to a JSON string
    override fun toString(): String {
        return Gson().toJson(this)
    }

    companion object {
        // Create an EnrolFaceResponse object from a JSON string
        fun fromJson(jsonString: String): EnrolFaceResponse {
            val gson = Gson()
            return gson.fromJson(jsonString, EnrolFaceResponse::class.java)
        }
    }
}

// Data class representing the progress status of 1FA enrollment
class Progress1FAProgressStatus(
    val progress: Int,
    val enrolFaceResponse: EnrolFaceResponse?
)

// ViewModel for managing the enrollment process
private var stillProcessingOldImage = false
public var stopEnroll = false

// LiveData for observing the progress of 1FA enrollment
private val _progressMutableLiveData: MutableLiveData<Progress1FAProgressStatus> = MutableLiveData()
val progressLiveData: LiveData<Progress1FAProgressStatus> = _progressMutableLiveData

// Current progress status of 1FA enrollment
private var enroll1FAProgressStatus: Progress1FAProgressStatus? = Progress1FAProgressStatus(0, null)

// Method to initiate the enrollment process with a given bitmap
fun enroll(bitmap: Bitmap) {
    if (stillProcessingOldImage || stopEnroll) {
        return
    } else {
        processEnrollment(bitmap)
    }
}

// Private method to process the enrollment
private fun processEnrollment(bitmap: Bitmap) {
    stillProcessingOldImage = true
    val enrollConfig = EnrollConfig(
        skipAntispoof = false,
        mfToken = enroll1FAProgressStatus?.enrolFaceResponse?.callStatus?.mfToken ?: ""
    )
    val enrolFaceResponseString = PrivateIdentitySDK.getInstantIdentitySession().enroll(bitmap = bitmap, enrollConfig = enrollConfig)
    val enrolFaceResponse = EnrolFaceResponse.fromJson(enrolFaceResponseString)
    updateProgressBasedOnResponse(enrolFaceResponse, bitmap)
    stillProcessingOldImage = false
}

// Private method to update the progress based on the response
private fun updateProgressBasedOnResponse(enrolFaceResponse: EnrolFaceResponse, bitmap: Bitmap) {
    val apiResponse = enrolFaceResponse.enrollOneFa?.enrollFaceApiResponse
    when {
        shouldInitializeProgress(enrolFaceResponse.callStatus.mfToken) -> {
            enroll1FAProgressStatus = Progress1FAProgressStatus(
                progress = 20,
                enrolFaceResponse = enrolFaceResponse
            )
        }
        isEnrollmentComplete(apiResponse?.guid, apiResponse?.puid) -> {
            enroll1FAProgressStatus = Progress1FAProgressStatus(
                100,
                enrolFaceResponse
            )
            enrolFaceResponse.enrollOneFa?.enrollFaceApiResponse?.puid?.let { savePuid(it) }
            enrolFaceResponse.enrollOneFa?.enrollFaceApiResponse?.guid?.let { saveGuid(it) }
            stopEnroll = true
        }
        shouldResetProgress(enrolFaceResponse.callStatus.mfToken) -> {
            enroll1FAProgressStatus = Progress1FAProgressStatus(
                0,
                enrolFaceResponse
            )
        }
        else -> {
            incrementProgress(enrolFaceResponse)
        }
    }

    val faceValidationData = enrolFaceResponse.enrollOneFa?.faceValidationData
    if (faceValidationData?.antiSpoofStatus == 0 || faceValidationData?.antiSpoofStatus == -6) {
        // Handle invalid face cases, e.g., person looking up, left, right
    } else {
        // Handle anti-spoof status detection
    }

    _progressMutableLiveData.postValue(enroll1FAProgressStatus!!)
}

// Helper method to determine if progress should be initialized
private fun shouldInitializeProgress(newMfToken: String?): Boolean =
    enroll1FAProgressStatus?.progress == 0 && !newMfToken.isNullOrEmpty()

// Helper method to check if enrollment is complete
private fun isEnrollmentComplete(guid: String?, puid: String?): Boolean =
    !guid.isNullOrEmpty() && !puid.isNullOrEmpty()

// Helper method to determine if progress should be reset
private fun shouldResetProgress(newMfToken: String?): Boolean =
    newMfToken.isNullOrEmpty() || enroll1FAProgressStatus?.enrolFaceResponse?.callStatus?.mfToken != newMfToken

// Helper method to increment progress
private fun incrementProgress(response: EnrolFaceResponse) {
    enroll1FAProgressStatus = enroll1FAProgressStatus?.let {
        Progress1FAProgressStatus(it.progress + 20, response)
    }
}

// Placeholder methods for saving PUID and
