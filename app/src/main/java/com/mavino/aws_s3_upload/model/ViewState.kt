package com.mavino.aws_s3_upload.model

import com.mavino.upload.model.UploadState

data class ViewState(
    val url: String? = null,
    val state: UploadState? = null,
    val id: Int? = null,
    val response: VideoUploadResponse? = null,
    val message: String? = null,
    var progress: Int? = null,
)

sealed class VideoUploadResponse {

    object Success : VideoUploadResponse()

    object Error : VideoUploadResponse()

    object Pending : VideoUploadResponse()

}