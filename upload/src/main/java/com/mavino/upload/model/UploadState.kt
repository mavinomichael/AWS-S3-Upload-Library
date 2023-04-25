package com.mavino.upload.model

sealed class UploadState {

    object FAILED : UploadState()

    object COMPLETED : UploadState()

    object WAITING : UploadState()

    object WAITING_FOR_NETWORK : UploadState()

    object INPROGRESS : UploadState()

    object CANCELED : UploadState()

}


