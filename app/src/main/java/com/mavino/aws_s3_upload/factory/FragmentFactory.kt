package com.mavino.aws_s3_upload.factory

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.mavino.aws_s3_upload.ui.UploadFragment

class FragmentFactory: FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(className){

            UploadFragment::class.java.name -> {
                UploadFragment()
            }

            else -> {
                super.instantiate(classLoader, className)
            }
        }
    }
}