package com.anikrakib.task.gsdatask

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anikrakib.task.gsdatask.databinding.VideoItemListBinding
import com.bumptech.glide.Glide
import kotlin.collections.ArrayList

class VideoAdapter(items: ArrayList<VideoModel>,context: Context) : RecyclerView.Adapter<VideoAdapter.MyViewHolder>() {

    private var items = ArrayList<VideoModel>()
    private lateinit var context: Context

    init {
        this.items = items
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = VideoItemListBinding.inflate(layoutInflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int,
    ) {
        holder.bind(items[position],context)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class MyViewHolder(
        private val binding: VideoItemListBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(videoModel: VideoModel,context: Context) {
            binding.apply {
                videoDuration.text = videoModel.duration
                videoTitle.text = videoModel.title
                videoQuality.text = "Quality: ${videoModel.resulation}p"
                videoSize.text = "Size: ${videoModel.size}"
                Glide.with(context).load(videoModel.path).into(thumbnail);
            }
        }
    }

}



