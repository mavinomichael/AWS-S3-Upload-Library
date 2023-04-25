package com.mavino.upload.aws

import android.content.Context
import android.util.Log
import com.amazonaws.mobileconnectors.s3.transferutility.*
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.model.ObjectMetadata
import com.mavino.upload.model.UploadState
import kotlinx.coroutines.runBlocking
import java.io.File
import java.lang.Exception
import java.util.*

class S3Uploader constructor(
    val context: Context,
    private val bucketName: String,
    private val cognitoPoolId: String,
    private val cognitoRegion: Regions,
    private val bucketRegion: Regions,
) {

    private var transferUtility: TransferUtility? = null
    private var s3UploadListener: S3UploadListener? = null
    private var observer: TransferObserver? = null
    private var networkLossHandler: TransferNetworkLossHandler? = null

    init {
        transferUtility = AWSUtils.getTransferUtility(
            context = context,
            cognitoPoolId = cognitoPoolId,
            cognitoRegion = cognitoRegion,
            bucketRegion = bucketRegion,
            bucketName = bucketName
        )
        networkLossHandler = TransferNetworkLossHandler.getInstance(context)
    }

    fun uploadFile(filePath: String, mimeType: String, useFileName: Boolean): String {
        val file = File(filePath)
        val metadata = ObjectMetadata()
        metadata.contentType = mimeType

        val fileName =
            if (useFileName) file.name else UUID.randomUUID().toString() + ".mp4"
        //+ mimeType.split("/")

        try {
            Log.d(TAG, "S3uploader uploadFile: filePath passed to fun $filePath")
            Log.d(TAG, "S3uploader uploadFile: attempting upload of $fileName")
            observer = transferUtility!!.upload(
                bucketName,
                fileName,
                file
            )
        } catch (e: Exception) {
            Log.d(TAG, "uploadFile: exception $e")
        }

        observer!!.setTransferListener(UploadListener())

        val url = runBlocking {
            S3Utils.generateS3Url(
                context = context,
                path = filePath,
                bucketName = bucketName,
                cognitoPoolId = cognitoPoolId,
                cognitoRegion = cognitoRegion,
                bucketRegion = bucketRegion
            )
        }
        return url
    }

    private inner class UploadListener : TransferListener {
        override fun onError(id: Int, e: Exception) {
            Log.e(TAG, "Error during upload: $id", e)
            s3UploadListener!!.onError(id, e.toString())
            observer!!.cleanTransferListener()
        }

        override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {

            Log.d(TAG, "id: $id, totalBytes: $bytesTotal, currentBytes: $bytesCurrent")

            val progress = (bytesCurrent.toDouble() / bytesTotal.toDouble()) * 100.0

            s3UploadListener!!.onProgress(id, progress.toInt())
        }

        override fun onStateChanged(id: Int, newState: TransferState) {
            s3UploadListener?.let {
                when (newState) {
                    TransferState.FAILED -> s3UploadListener!!.onStateChanged(
                        id,
                        UploadState.FAILED
                    )

                    TransferState.COMPLETED -> s3UploadListener!!.onStateChanged(
                        id,
                        UploadState.COMPLETED
                    )

                    TransferState.WAITING -> s3UploadListener!!.onStateChanged(
                        id,
                        UploadState.WAITING
                    )

                    TransferState.WAITING_FOR_NETWORK -> s3UploadListener!!.onStateChanged(
                        id,
                        UploadState.WAITING_FOR_NETWORK
                    )

                    TransferState.IN_PROGRESS -> s3UploadListener!!.onStateChanged(
                        id,
                        UploadState.INPROGRESS
                    )

                    TransferState.CANCELED -> s3UploadListener!!.onStateChanged(
                        id,
                        UploadState.CANCELED
                    )

                    else -> {
                        s3UploadListener!!.onStateChanged(id, UploadState.FAILED)
                    }
                }
            }

            if (newState == TransferState.COMPLETED) {
                s3UploadListener!!.onSuccess(id, "Success")
                observer!!.setTransferListener(null)
            }
            if (newState == TransferState.FAILED || newState == TransferState.CANCELED) {
                observer!!.setTransferListener(null)
            }

        }
    }

    fun setOnUploadListener(s3UploadListener: S3UploadListener) {
        this.s3UploadListener = s3UploadListener
    }

    fun pauseUpload(id: Int) {
        transferUtility!!.pause(id)
    }

    fun resumeUpload(id: Int) {
        transferUtility!!.resume(id)
    }

    companion object {
        const val TAG = "s3UploadDebug"
    }
}