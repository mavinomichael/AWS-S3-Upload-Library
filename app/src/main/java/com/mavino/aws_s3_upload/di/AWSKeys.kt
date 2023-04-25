package com.mavino.aws_s3_upload.di

import com.amazonaws.regions.Regions

object AWSKeys {
    const val COGNITO_POOL_ID = "cognito-pool-id"
    const val BUCKET_NAME = "bucket-name"
    val COGNITO_REGION = Regions.US_EAST_2
    val BUCKET_REGION = Regions.US_EAST_2
}