package com.mavino.upload.aws

import android.content.Context
import android.util.Log
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.auth.CognitoCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.mavino.upload.aws.S3Uploader.Companion.TAG

object AWSUtils {

    private var amazonS3Client: AmazonS3Client? = null
    private var cognitoCredProvider: CognitoCredentialsProvider? = null
    private var transferUtility: TransferUtility? = null


    private fun getCognitoCredProvider(context: Context?, cognitoPoolId: String, cognitoRegion: Regions): CognitoCredentialsProvider? {
        if (cognitoCredProvider == null) {
            cognitoCredProvider = CognitoCachingCredentialsProvider(
                context,
                cognitoPoolId,
                cognitoRegion
            )
        }

        return cognitoCredProvider
    }

    fun getS3Client(
        context: Context?,
        cognitoRegion: Regions,
        bucketRegion: Regions,
        cognitoPoolId: String,
        bucketName: String
    ): AmazonS3Client? {
        if (amazonS3Client == null) {
            amazonS3Client = AmazonS3Client(
                getCognitoCredProvider(context, cognitoPoolId, cognitoRegion),
                Region.getRegion(cognitoRegion)
            )
            amazonS3Client!!.setRegion(Region.getRegion(bucketRegion))
        }

        return amazonS3Client
    }


    fun getTransferUtility(
        context: Context?,
        cognitoRegion: Regions,
        bucketRegion: Regions,
        cognitoPoolId: String,
        bucketName: String
    ): TransferUtility? {
        Log.d(TAG, "getTransferUtility: cognitoRegion:$cognitoRegion, bucketRegion:$bucketRegion, cognitoPoolId:$cognitoPoolId, bucketName:$bucketName")
        if (transferUtility == null) {
            transferUtility = TransferUtility.builder()
                .s3Client(getS3Client(
                    context,
                    cognitoRegion,
                    bucketRegion,
                    cognitoPoolId,
                    bucketName
                ))
                .context(context).build()
        }

        return transferUtility
    }
}