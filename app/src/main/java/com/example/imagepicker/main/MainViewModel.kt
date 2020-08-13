package com.example.imagepicker.main

import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import com.example.imagepicker.ui.BottomSheetFragment
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel : ViewModel() {

    companion object {
        const val TAG = "MainViewModel"
    }

    val repository = MainRepository()



    fun getData() {
        repository.readImages()
    }


    fun getObservableImages() = repository.mUpload


    fun imageSelected(fileName: String, contentUri: Uri) {
        repository.saveImage(fileName, contentUri)
    }

    override fun onCleared() {
        super.onCleared()
        Log.v(TAG, "onCleared()")
    }


}