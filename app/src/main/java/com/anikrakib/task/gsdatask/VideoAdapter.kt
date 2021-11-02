package com.anikrakib.task.gsdatask

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.MediaController
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.anikrakib.task.gsdatask.databinding.VideoItemListBinding
import com.bumptech.glide.Glide
import android.widget.AbsListView


class VideoAdapter(
    items: ArrayList<VideoModel>,
    context: Context,
    onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<VideoAdapter.MyViewHolder>() {

    private var items = ArrayList<VideoModel>()
    private var context: Context
    private var onItemClickListener: OnItemClickListener

    init {
        this.items = items
        this.context = context
        this.onItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = VideoItemListBinding.inflate(layoutInflater, parent, false)
        return MyViewHolder(this, binding, onItemClickListener)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int,
    ) {
        holder.bind(items[position], context)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class MyViewHolder(
        private val adapter: VideoAdapter,
        private val binding: VideoItemListBinding,
        private val onItemClickListener: OnItemClickListener,
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(videoModel: VideoModel, context: Context) {
            binding.apply {
                videoDuration.text = videoModel.duration
                videoTitle.text = videoModel.title
                videoQuality.text = "Quality: ${videoModel.resulation}p"
                videoSize.text = "Size: ${videoModel.size}"
                //videoItemLayout.visibility = if(videoModel.isSelected) View.GONE else View.VISIBLE
                //
                if (videoModel.isSelected) {
                    // set selected item visibility Gone
                    videoItemLayout.layoutParams = AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1
                    )
                    videoItemLayout.visibility = View.GONE
                } else {
                    // set selected item visibility Visible
                    videoItemLayout.layoutParams = AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        AbsListView.LayoutParams.WRAP_CONTENT
                    )
                    videoItemLayout.visibility = View.VISIBLE
                }

                Glide.with(context).load(videoModel.path).into(thumbnail)
                videoItemLayout.setOnClickListener {
                    onItemClickListener.onClick(adapterPosition)
                    adapter.onSelect(videoModel)
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onClick(position: Int)
    }

    private fun onSelect(data: VideoModel) {
        for (i in 0 until items.size) {
            if (items[i].id == data.id) {
                onSelect(i)
                Log.d("position", "Current Position - > $i ${data.id}")
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onSelect(position: Int) {
        items.let {
            it.forEach { countryName ->
                countryName.isSelected = false
                Log.d("position", "Current Position - > $position $countryName")
            }
            items[position].isSelected = true
            notifyDataSetChanged()
        }
    }
}



