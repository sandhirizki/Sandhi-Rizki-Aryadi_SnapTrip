package com.sandhirizki0088.snaptrip.network

data class ImgbbResponse(
    val data: ImgbbData,
    val success: Boolean,
    val status: Int
)

data class ImgbbData(
    val url: String,
    val display_url: String,
    val image: ImageInfo
)

data class ImageInfo(
    val filename: String,
    val name: String,
    val mime: String,
    val extension: String,
    val url: String
)
