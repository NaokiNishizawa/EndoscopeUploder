package com.omejibika.endoscope

import java.io.Serializable

data class GridDataList(
    val image: String,
    val filename: String,
    val lastModified: Long,
    var isSelected: Boolean = false
): Serializable