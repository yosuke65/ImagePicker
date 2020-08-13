package com.example.imagepicker.ui

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SnapHelper
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.imagepicker.R
import com.example.imagepicker.main.MainRepository
import com.example.imagepicker.main.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), MainRepository.OnRepositoryListener, BottomSheetFragment.OnFragmentListener {

    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var sheetBehavior: BottomSheetBehavior<View>
    private var bottomSheetFragment = BottomSheetFragment()
    private lateinit var viewModel: MainViewModel
    private lateinit var myAdapter: AdapterImageList

    //ButterKnife
    @BindView(R.id.floating_button_open)
    lateinit var btnOpenBottomSheet: FloatingActionButton
    @BindView(R.id.bottom_sheet)
    lateinit var layoutBottomSheet: ConstraintLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        //ViewModel
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.repository.setOnRepositoryListener(this)
        viewModel.getData()
        observeData()
        //ButterKnife
        ButterKnife.bind(this)
        //BottomSheet
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet)
        bottomSheetFragment.setOnFragmentListener(this)
        //Recyclerview
        myAdapter = AdapterImageList(this)
        recyclerview_main.layoutManager = GridLayoutManager(this, 2)
        recyclerview_main.adapter = myAdapter
    }

    private fun observeData() {
        Log.v(TAG, "observeData()")
        viewModel.getObservableImages().observe(this, Observer {
            for (upload in it) {
                Log.v(TAG, "Observed data: $upload")
            }
            Log.v(TAG, "List:${it.size}")
            myAdapter.setData(it)
        })
    }

    @OnClick(R.id.floating_button_open)
    fun onOpenBottomSheetButtonClicked() {
        showBottomSheetDialogFragment(supportFragmentManager)
    }

    override fun imageAdded() {
        Snackbar.make(
            activity_main_layout,
            "Image uploaded successfully",
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun showBottomSheetDialogFragment(supportFragmentManager: FragmentManager) {
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

   private  fun hideBottomSheetDialogFragment() {
        bottomSheetFragment.dismiss()
    }

    override fun imageSelected(fileName: String, uri: Uri) {
        viewModel.imageSelected(fileName,uri)
        hideBottomSheetDialogFragment()
    }


}