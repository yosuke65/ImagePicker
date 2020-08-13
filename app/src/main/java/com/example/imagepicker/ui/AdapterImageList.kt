package com.example.imagepicker.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.imagepicker.R
import com.example.imagepicker.model.Upload
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.grid_image_adapter.view.*
import kotlin.collections.ArrayList

class AdapterImageList(var mContext: Context):RecyclerView.Adapter<AdapterImageList.MyViewHolder>() {
    private var mList:ArrayList<Upload> = ArrayList()
    inner class MyViewHolder(view: android.view.View):RecyclerView.ViewHolder(view){
        fun bind(upload:Upload){
            Picasso.get().load(upload.url).fit().centerCrop().into(itemView.image_view_gallery)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.grid_image_adapter,parent,false))
    }

    override fun getItemCount(): Int {
        return mList.size
    }



    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
       holder.bind(mList[position])
    }

    fun setData(list: ArrayList<Upload>) {
        mList = list
        notifyDataSetChanged()
    }
}