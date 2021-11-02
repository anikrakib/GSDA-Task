package com.anikrakib.task.gsdatask

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anikrakib.task.gsdatask.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow
import kotlinx.android.synthetic.main.custom_control.view.*
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener


class MainActivity : AppCompatActivity(), View.OnClickListener, VideoAdapter.OnItemClickListener {
    private var activityMainBinding: ActivityMainBinding? = null
    var videoList = ArrayList<VideoModel>()
    private lateinit var videoAdapter: VideoAdapter
    private val REQUEST_CODE_PERMISSION = 123
    private val dirName = "Download"
    var isPause = true


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        activityMainBinding?.videoView.apply {
            customControl.apply {
                videoView_rewind.setOnClickListener(this@MainActivity)
                videoView_forward.setOnClickListener(this@MainActivity)
                videoView_play_pause_btn.setOnClickListener(this@MainActivity)
            }
            videoView.setOnClickListener(this@MainActivity)
            videoView.setOnClickListener {
                hideCustomControl()
            }
        }

        hideCustomControl()
        permission()
        loadVideos()
        /*initializeSeekBars()
        setHandler()*/
    }

    private fun hideCustomControl() {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                hideDefaultControls()
            }, 5000
        )
        showDefaultControls()
    }

    private fun permission() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "denied", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_PERMISSION
                )
            }
        }
    }

    private fun loadVideos() {
        videoList = getAllVideoFromFolder(this)
        if (videoList.size > 0) {
            videoAdapter =
                VideoAdapter(videoList, applicationContext, this)
            activityMainBinding!!.videoRecyclerView.apply {
                setHasFixedSize(true)
                setItemViewCacheSize(20)
                isNestedScrollingEnabled = false
                adapter = videoAdapter
                layoutManager = LinearLayoutManager(
                    context,
                    RecyclerView.VERTICAL, false
                )
            }
        } else {
            Toast.makeText(this, "can't find any videos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAllVideoFromFolder(context: Context): ArrayList<VideoModel> {
        val list: ArrayList<VideoModel> = ArrayList()
        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val orderBy = MediaStore.Video.Media.DATE_ADDED + " DESC"
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.RESOLUTION
        )
        val selection = MediaStore.Video.Media.DATA + " like?"
        val selectionArgs = arrayOf("%$dirName%")

        val cursor: Cursor =
            context.contentResolver.query(
                uri, projection, selection, selectionArgs,
                orderBy
            )!!
        while (cursor.moveToNext()) {
            val id: String = cursor.getString(0)
            val path: String = cursor.getString(1)
            val title: String = cursor.getString(2)
            val size: Int = cursor.getInt(3)
            val resolution: String = cursor.getString(4)
            val duration: Int = cursor.getInt(5)
            val disName: String = cursor.getString(6)
            val displayName: String = cursor.getString(7)
            val widthHeight: String = cursor.getString(8)

            //this method convert 1204 in 1MB
            var convertSize: String?
            convertSize = if (size < 1024) {
                java.lang.String.format(context.getString(R.string.size_in_b), size.toDouble())
            } else if (size < 1024.0.pow(2.0)) {
                java.lang.String.format(
                    context.getString(R.string.size_in_kb),
                    (size / 1024).toDouble()
                )
            } else if (size < 1024.0.pow(3.0)) {
                java.lang.String.format(
                    context.getString(R.string.size_in_mb),
                    size / 1024.0.pow(2.0)
                )
            } else {
                java.lang.String.format(
                    context.getString(R.string.size_in_gb),
                    size / 1024.0.pow(3.0)
                )
            }

            //this method convert any random video duration like 1331533132 into 1:21:12
            var durationFormatted: String
            val sec = duration / 1000 % 60
            val min = duration / (1000 * 60) % 60
            val hrs = duration / (1000 * 60 * 60)
            durationFormatted = if (hrs == 0) {
                min.toString() + ":" + java.lang.String.format(Locale.UK, "%02d", sec)
            } else {
                "$hrs:" + java.lang.String.format(
                    Locale.UK,
                    "%02d",
                    min
                ) + ":" + java.lang.String.format(Locale.UK, "%02d", sec)
            }
            val files = VideoModel(
                id.toInt(),
                path,
                title,
                convertSize,
                resolution,
                durationFormatted,
                disName,
                widthHeight
            )
            if (dirName.endsWith(displayName)) list.add(files)
        }
        cursor.close()
        return list
    }

    private fun hideDefaultControls() {
        activityMainBinding?.customControl?.apply {
            videoViewOneLayout.visibility = View.GONE
            videoViewTwoLayout.visibility = View.GONE
            videoViewThreeLayout.visibility = View.GONE
        }
    }

    private fun showDefaultControls() {
        activityMainBinding?.customControl?.apply {
            videoViewOneLayout.visibility = View.VISIBLE
            videoViewTwoLayout.visibility = View.VISIBLE
            videoViewThreeLayout.visibility = View.VISIBLE
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.videoView_rewind -> {
                videoView.seekTo(videoView.currentPosition - 10000)
            }
            R.id.videoView_forward -> {
                videoView.seekTo(videoView.currentPosition + 10000)
            }
            R.id.videoView_play_pause_btn -> {
                if (videoView.isPlaying) {
                    videoView.pause()
                    isPause = true
                    activityMainBinding?.customControl?.videoView_play_pause_btn?.setImageResource(R.drawable.ic_baseline_play_arrow)
                } else {
                    videoView.start()
                    isPause = false
                    activityMainBinding?.customControl?.videoView_play_pause_btn?.setImageResource(R.drawable.ic_baseline_pause)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onClick(position: Int) {
        activityMainBinding?.customControl?.apply {
            videoView.setVideoPath(videoList[position].path)
            videoView.setOnPreparedListener {
                videoView_seekbar?.max = videoView.getDuration();
                videoView.start()
                isPause = false
                videoView_play_pause_btn.setImageResource(R.drawable.ic_baseline_pause)
            }
            videoView_title.text = videoList[position].title
        }
        initializeSeekBars()
        setHandler()
        hideCustomControl()
    }

    private fun initializeSeekBars() {
        activityMainBinding!!.customControl.apply {
            videoView_seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (videoView_seekbar.id == R.id.videoView_seekbar) {
                        if (fromUser) {
                            videoView.seekTo(progress)
                            if (isPause) {
                                videoView.pause()
                            } else {
                                videoView.start()
                            }
                            val currentPosition = videoView.currentPosition
                            videoView_endtime.text =
                                "" + convertIntoTime(videoView.duration - currentPosition)
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }
    }

    private fun convertIntoTime(ms: Int): String {
        val time: String
        val seconds: Int
        val minutes: Int
        val hours: Int
        var x: Int = ms / 1000
        seconds = x % 60
        x /= 60
        minutes = x % 60
        x /= 60
        hours = x % 24
        time = if (hours != 0) String.format("%02d", hours) + ":" + String.format(
            "%02d",
            minutes
        ) + ":" + String.format("%02d", seconds) else String.format(
            "%02d",
            minutes
        ) + ":" + String.format("%02d", seconds)
        return time
    }

    @SuppressLint("SetTextI18n")
    private fun setHandler() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (videoView.duration > 0) {
                val currentPosition = videoView.currentPosition
                activityMainBinding?.customControl?.videoView_seekbar?.progress =
                    currentPosition
                activityMainBinding?.customControl?.videoView_endtime?.text =
                    "" + convertIntoTime(videoView.duration - currentPosition)
            }
        }, 500)
    }

}