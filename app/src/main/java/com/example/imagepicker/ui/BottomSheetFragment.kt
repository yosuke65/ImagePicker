package com.example.imagepicker.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.example.imagepicker.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
const val TAG = "BottomSheetFragment"
const val REQUEST_CAMERA_CODE = 100
const val REQUEST_GALLERY_CODE = 102
const val APPLICATION_SETTING_CODE = 101



class BottomSheetFragment : BottomSheetDialogFragment() {


    @BindView(R.id.button_camera_frag)
    lateinit var btnCamera: Button
    @BindView(R.id.button_upload_frag)
    lateinit var btnUpload: Button

    private lateinit var unBinder: Unbinder
    private lateinit var listener: OnFragmentListener
    private lateinit var currentPhotoPath: String

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false)
        unBinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBinder.unbind()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CAMERA_CODE -> {
                if (resultCode == RESULT_OK) {
                    var file = File(currentPhotoPath)
                    var contentUri = Uri.fromFile(file)
                    galleryAddPic(file)
                    listener.imageSelected(file.name,contentUri)

                } else {
                    Toast.makeText(activity, "Result Fail", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_GALLERY_CODE -> {
                if (resultCode == RESULT_OK) {
                    var contentUri:Uri = data?.data as Uri
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                    val imageFileName = "JPEG_" + timeStamp + "." + getFileExtension(contentUri)
                    listener.imageSelected(imageFileName,contentUri)
                } else {
                    Toast.makeText(activity, "Result Fail", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getFileExtension(contentUri: Uri): String? {
        val contentResolver = activity?.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver?.getType(contentUri))
    }

    private fun galleryAddPic(file:File) {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            mediaScanIntent.data = Uri.fromFile(file)
            activity?.sendBroadcast(mediaScanIntent)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val storageDir:File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(activity!!.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        activity!!,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_CAMERA_CODE)
                }
            }
        }
    }

    interface OnFragmentListener{
        fun imageSelected(fileName:String, uri: Uri)

    }

    fun setOnFragmentListener(onFragmentListener: OnFragmentListener){
        listener = onFragmentListener
    }



    @OnClick(R.id.button_camera_frag)
    fun onButtonCameraClicked() {
        requestPermission(android.Manifest.permission.CAMERA)
    }


    private fun requestPermission(permission: String) {
        when (permission) {
            android.Manifest.permission.CAMERA -> {
                Dexter
                    .withContext(activity)
                    .withPermission(android.Manifest.permission.CAMERA)
                    .withListener(object : PermissionListener {
                        override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                            openCamera()
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            request: PermissionRequest?,
                            token: PermissionToken?
                        ) {
                            token?.continuePermissionRequest()
                        }

                        override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                            showSettingDialog()
                        }

                    }).check()
            }
        }
    }

    private fun showSettingDialog() {
        AlertDialog.Builder(activity!!)
            .setTitle("Permission needed")
            .setMessage("Go to settings and allow accessibility")
            .setNegativeButton("Dismiss") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Setting") { dialog, _ -> openSetting(); dialog.dismiss() }
            .create().show()
    }

    private fun openSetting() {
        var intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        var uri = Uri.fromParts("package", activity?.packageName, null)
        intent.data = uri
        startActivityForResult(intent,
            APPLICATION_SETTING_CODE
        )
    }

    private fun openCamera() {
        dispatchTakePictureIntent()
    }

    @OnClick(R.id.button_upload_frag)
    fun onButtonUploadClicked() {
        requestReadAndWriteStorage()
    }

    private fun requestReadAndWriteStorage() {
        Dexter
            .withContext(activity)
            .withPermissions(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        openGallery()
                    } else {
                        showSettingDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    list: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token!!.continuePermissionRequest()
                }

            }).check()
    }

    private fun openGallery() {
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,
            REQUEST_GALLERY_CODE
        )
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}
