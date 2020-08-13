package com.example.imagepicker.main

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.imagepicker.model.Upload
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.UploadTask
import java.lang.Exception

class MainRepository {

    companion object {
        const val TAG = "MainRepository"
    }

    private var storageRef = FirebaseStorage.getInstance().reference
    private var databaseRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var listener: OnRepositoryListener


    var mUpload = MutableLiveData<ArrayList<Upload>>()


    interface OnRepositoryListener {
        fun imageAdded()
    }

    fun setOnRepositoryListener(onRepositoryListener: OnRepositoryListener) {
        listener = onRepositoryListener
    }

    fun readImages() {
        var list = ArrayList<Upload>()
        databaseRef.child("uploads").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    var upload = data.getValue(Upload::class.java)
                    if (!upload!!.url.isNullOrEmpty()) {
                        list.add(upload!!)
                    }
                }
                mUpload.postValue(list)
            }
        })
    }

    fun saveImage(fileName: String, contentUri: Uri) {
        Log.d(TAG, "saveImage")
        var ref = storageRef.child("images/ + $fileName")
        var uploadTask = ref.putFile(contentUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl

        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                var uploadId = databaseRef.push().key.toString()
                databaseRef.child("uploads/ + $uploadId")
                    .setValue(Upload(fileName, downloadUri.toString()))
                Log.d(TAG, "upload session uri $downloadUri")
                mUpload.value?.add(Upload(fileName, downloadUri.toString()))
                mUpload.postValue(mUpload.value)
                listener.imageAdded()

            } else {
                // Handle failures
                // ...
            }
        }
    }
}