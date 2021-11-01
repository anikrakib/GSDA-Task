package com.anikrakib.task.gsdatask

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anikrakib.task.gsdatask.databinding.ActivityMainBinding
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow


class MainActivity : AppCompatActivity() {
    private var activityMainBinding: ActivityMainBinding? = null
    var videoList = ArrayList<VideoModel>()
    private lateinit var videoAdapter: VideoAdapter
    private val REQUEST_CODE_PERMISSION = 123
    private val dirName = "Download"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        permission()
        loadVideos()

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
        videoList = getallVideoFromFolder(this)
        if (videoList.size > 0) {
            videoAdapter = VideoAdapter(videoList, applicationContext)
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

    private fun getallVideoFromFolder(context: Context): ArrayList<VideoModel> {
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
            var convertSize: String? = null
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
            var duration_formatted: String
            val sec = duration / 1000 % 60
            val min = duration / (1000 * 60) % 60
            val hrs = duration / (1000 * 60 * 60)
            if (hrs == 0) {
                duration_formatted =
                    min.toString() + ":" + java.lang.String.format(Locale.UK, "%02d", sec)
            } else {
                duration_formatted =
                    "$hrs:" + java.lang.String.format(
                        Locale.UK,
                        "%02d",
                        min
                    ) + ":" + java.lang.String.format(Locale.UK, "%02d", sec)
            }
            val files = VideoModel(
                path,
                title,
                convertSize,
                resolution,
                duration_formatted,
                disName,
                widthHeight
            )
            if (dirName.endsWith(displayName)) list.add(files)
        }
        cursor.close()
        return list
    }

}