package com.sandhirizki0088.snaptrip.ui.theme.screen

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandhirizki0088.snaptrip.model.Travel
import com.sandhirizki0088.snaptrip.network.ApiStatus
import com.sandhirizki0088.snaptrip.network.ImgbbApi
import com.sandhirizki0088.snaptrip.network.TravelApi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class MainViewModel : ViewModel() {

    var data = mutableStateOf(emptyList<Travel>())
        private set

    var status = MutableStateFlow(ApiStatus.LOADING)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set


    fun retrieveData(userId: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            status.value = ApiStatus.LOADING
            try {
                val allTravel = TravelApi.service.getTravelAll()
                Log.d("MainViewModel", "allTravel: $allTravel")

                val dummyData = allTravel.filter { it.userId.isBlank() }
                Log.d("MainViewModel", "dummyData: $dummyData")

                val finalList = if (userId.isNullOrBlank()) {
                    dummyData
                } else {
                    val userData = try {
                        TravelApi.service.getTravel(userId)
                    } catch (e: Exception) {
                        Log.d("MainViewModel", "getTravel failed: ${e.message}")
                        emptyList()
                    }

                    if (userData.isEmpty()) {
                        dummyData
                    } else {
                        dummyData + userData
                    }
                }

                Log.d("MainViewModel", "finalList: $finalList")
                data.value = finalList
                status.value = ApiStatus.SUCCESS
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                status.value = ApiStatus.FAILED
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun deleteData(userId: String, resepId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                TravelApi.service.deleteTravel(resepId)
                retrieveData(userId)
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    private fun saveData(tempat: String, tanggal: String, imageId: String, userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                TravelApi.service.postTravel(
                    tempat, tanggal, imageId, userId
                )
                retrieveData(userId)
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }


    fun clearMessage() {
        errorMessage.value = null
    }


    private suspend fun uploadImageToImgBBViaRetrofit(bitmap: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()

        val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val multipart = MultipartBody.Part.createFormData("image", "upload.jpg", requestBody)

        return try {
            val response = ImgbbApi.services.uploadImage(image = multipart)
            if (response.success) {
                response.data.url
            } else {
                Log.e("UploadError", "Upload gagal: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e("UploadError", "Exception: ${e.message}")
            null
        }
    }

    fun uploadAndSave(
        tempat: String,
        tanggal: String,
        userId: String,
        bitmap: Bitmap,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val imageId = uploadImageToImgBBViaRetrofit(bitmap)
            if (imageId != null) {
                saveData(tempat, tanggal, imageId, userId)
                onSuccess()
            } else {
                onError("Gagal upload gambar ke ImgBB")
            }
        }
    }

    fun updateData(
        id: String,
        tempat: String,
        tanggal: String,
        imageId: String,
        userId: String

    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                TravelApi.service.updateTravel(id, tempat, tanggal,imageId, userId)
                retrieveData(userId)
            } catch (e: Exception) {
                Log.d("MainViewModel", "Update gagal: ${e.message}")
                errorMessage.value = "Gagal update: ${e.message}"
            }
        }
    }


}