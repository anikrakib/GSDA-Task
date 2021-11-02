package com.anikrakib.task.gsdatask

data class VideoModel(
    var id: Int,
    var path: String,
    var title: String,
    var size: String,
    var resulation: String,
    var duration: String,
    var displayName: String,
    var wh: String,
    var isSelected: Boolean = false
)
