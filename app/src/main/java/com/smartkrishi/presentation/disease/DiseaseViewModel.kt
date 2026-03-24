package com.smartkrishi.presentation.disease

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartkrishi.data.remote.ApiService
import com.smartkrishi.data.remote.DiseaseResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class DiseaseViewModel @Inject constructor(
    application: Application,
    private val api: ApiService
) : AndroidViewModel(application) {

    fun uploadImage(uri: Uri, onResult: (String, Float) -> Unit = { _, _ -> }) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>().applicationContext
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@launch

                val requestFile = RequestBody.create(
                    "image/*".toMediaTypeOrNull(),
                    bytes
                )

                val body = MultipartBody.Part.createFormData(
                    "image", "leaf.jpg", requestFile  // 👈 backend key name must match
                )

                val response = api.predictDisease(body)

                if (response.isSuccessful && response.body() != null) {
                    val res: DiseaseResponse = response.body()!!
                    Log.d("KRISHI-DISEASE", "SUCCESS: result=${res.result}, conf=${res.confidence}")

                    onResult(res.result, res.confidence)

                } else {
                    Log.e("KRISHI-DISEASE", "SERVER ERROR: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e("KRISHI-DISEASE", "FAIL: ${e.localizedMessage}", e)
            }
        }
    }
}
