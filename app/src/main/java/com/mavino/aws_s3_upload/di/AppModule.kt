package com.mavino.aws_s3_upload.di

import android.content.Context
import com.mavino.upload.aws.S3Uploader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Singleton
    @Provides
    fun provideS3Uploader(@ApplicationContext context: Context): S3Uploader{
        return S3Uploader(
            context,
            AWSKeys.BUCKET_NAME,
            AWSKeys.COGNITO_POOL_ID,
            AWSKeys.COGNITO_REGION,
            AWSKeys.BUCKET_REGION
        )
    }
}