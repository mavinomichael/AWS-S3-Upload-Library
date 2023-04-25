package com.mavino.aws_s3_upload.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import com.mavino.aws_s3_upload.util.Constants.Companion.TAG
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.net.URI
import java.net.URISyntaxException


class Converter {

    fun getRealPathFromURI(uri: Uri?, context: Context): String {
        var path = ""
        try {
            if (context.contentResolver != null) {
                val cursor: Cursor? =
                    uri?.let {
                        context.contentResolver
                            .query(
                                it,
                                null,
                                null,
                                null,
                                null
                            )
                    }
                if (cursor != null) {
                    cursor.moveToFirst()
                    val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    path = cursor.getString(idx)
                    cursor.close()
                }
            }
            return path
        }catch (e:Exception){
            Log.i(TAG, "getRealPathFromURI: $e")
            return path
        }
    }

    fun getDocPath(uri: Uri?): String {
        var path = ""
        if (uri!!.scheme!!.compareTo("file") == 0) {
            try {
                val file = File(URI(uri.toString()))
                if (file.exists()) path = file.absolutePath
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }
        } else {
            path = uri.path!!
        }
        return path


    }

    fun getDocPath2(uri: Uri?, context: Context): String {
        var path = ""
        if (context.contentResolver != null) {
            val cursor: Cursor? =
                uri?.let {
                    context.contentResolver
                        .query(
                            it,
                            null,
                            null,
                            null,
                            null
                        )
                }
            if (cursor != null) {
                cursor.moveToFirst()
                val idx: Int = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path


    }

    fun createPartFromString(value: String): RequestBody {
        return RequestBody.create(
            MultipartBody.FORM, value
        )
    }
}