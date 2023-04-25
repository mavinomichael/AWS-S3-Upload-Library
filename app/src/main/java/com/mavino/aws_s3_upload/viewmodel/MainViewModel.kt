package com.mavino.aws_s3_upload.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mavino.aws_s3_upload.model.VideoUploadResponse
import com.mavino.aws_s3_upload.model.ViewState
import com.mavino.aws_s3_upload.util.Constants.Companion.TAG
import com.mavino.aws_s3_upload.util.Event
import com.mavino.upload.aws.S3UploadListener
import com.mavino.upload.aws.S3Uploader
import com.mavino.upload.model.UploadState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val s3uploaderObj: S3Uploader
) : ViewModel() {

    private var _viewState = MutableLiveData<Event<ViewState>>()
    val viewState: LiveData<Event<ViewState>> = _viewState

    fun uploadVideoTos3(videoUri: Uri, context: Context) {
        //upload file with s3UploaderObject
        val fileUrl = s3uploaderObj.uploadFile(
            videoUri.path!!,
            "video/mp4",
            true
        )

        //listen for success or exception and progress
        s3uploaderObj.setOnUploadListener(object : S3UploadListener {
            override fun onSuccess(id: Int, response: String?) {
                Log.i(TAG, "viewModel status $response for uploadId: $id")
                if (response.equals("success", ignoreCase = true)) {
                    _viewState.value = Event(
                        ViewState(
                            id = id,
                            message = response,
                            url = fileUrl,
                            state = UploadState.COMPLETED,
                            response = VideoUploadResponse.Success
                        )
                    )
                } else {
                    Log.i(TAG, "viewModel response is failure $response")
                }
            }

            override fun onError(id: Int, response: String?) {
                Log.i(TAG, "viewModel error $response for uploadId: $id")
                _viewState.value = Event(
                    ViewState(
                        id = id,
                        message = response,
                        url = fileUrl,
                        state = UploadState.FAILED,
                        response = VideoUploadResponse.Error
                    )
                )
            }

            override fun onProgress(id: Int, progress: Int) {
                Log.i(TAG, "viewModel progress $progress for uploadId: $id")
                _viewState.value = Event(
                    ViewState(
                        id = id,
                        progress = progress,
                        url = fileUrl,
                        state = UploadState.INPROGRESS,
                        response = VideoUploadResponse.Pending
                    )
                )
            }

            override fun onStateChanged(
                id: Int,
                state: UploadState?
            ) {
                Log.d(TAG, "viewModel onStateChanged: $state")
            }

        })

    }
}