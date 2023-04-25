package com.mavino.upload.aws

import com.mavino.upload.model.UploadState


interface S3UploadListener {

    fun onSuccess(id: Int, response: String?)

    fun onError(id: Int, response: String?)

    fun onProgress(id: Int, progress: Int)

    fun onStateChanged(id: Int, state: UploadState?)
}