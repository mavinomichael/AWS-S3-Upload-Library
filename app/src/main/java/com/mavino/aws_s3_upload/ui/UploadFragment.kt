package com.mavino.aws_s3_upload.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.TransactionTooLargeException
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mavino.aws_s3_upload.viewmodel.MainViewModel
import com.mavino.aws_s3_upload.R
import com.mavino.aws_s3_upload.adapter.UploadAdapter
import com.mavino.aws_s3_upload.databinding.FragmentUploadBinding
import com.mavino.aws_s3_upload.model.Upload
import com.mavino.aws_s3_upload.model.VideoUploadResponse
import com.mavino.aws_s3_upload.util.Constants.Companion.TAG
import com.mavino.upload.model.UploadState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UploadFragment : Fragment(R.layout.fragment_upload) {

    private val STORAGE_PERMISSION_CODE = 100
    private val GALLERY_REQUEST_CODE_ONE = 100
    val PERMISSION_DENIED_MESSAGE =
        "Please enable permission in other to select image from gallery"
    private var videoFile: File? = null
    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private val uploads: ArrayList<Upload> = ArrayList()
    private var uploadAdapter: UploadAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)

        binding.picker.setOnClickListener {
            checkPermissionAndPickImageFromGallery(GALLERY_REQUEST_CODE_ONE)
        }

        setupRecyclerView()
        subscribeObservers()

        return binding.root
    }

    private fun pickFromGallery(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/mp4"
        startActivityForResult(intent, requestCode)
    }

    private fun checkPermissionAndPickImageFromGallery(requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when {
                !permissionGranted() -> requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )

                permissionGranted() -> pickFromGallery(requestCode)
            }
        } else {
            when {
                !permissionGranted() -> requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )

                permissionGranted() -> pickFromGallery(requestCode)
            }
        }
    }

    private fun createVideoFile(): File {
        videoFile?.delete()
        videoFile?.deleteOnExit()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val videoFileName = "VIDEO_$timeStamp"

        val storageDir = requireContext().getExternalFilesDir("video")
        storageDir?.deleteOnExit()

        if (storageDir?.exists()!!) {
            val response = storageDir.listFiles()
            response?.onEach {
                it.delete()
            }
        }

        return (File.createTempFile(
            videoFileName,
            ".mp4",
            storageDir
        ))
    }

    private fun permissionGranted() = ContextCompat.checkSelfPermission(
        requireActivity(),
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            if (requestCode == GALLERY_REQUEST_CODE_ONE && resultCode == Activity.RESULT_OK) {
                val videoUri = data?.data ?: return
                videoFile = createVideoFile()
                requireContext().contentResolver.openInputStream(videoUri)?.use { inputStream ->
                    videoFile!!.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                if (videoFile != null) {
                    Log.d(TAG, "onActivityResult: videoFile ${videoFile!!.toUri()}")
                    binding.showLoading.visibility = View.VISIBLE
                    uploadVideoTos3(videoFile!!.toUri())
                }else {
                    Log.d(TAG, "onActivityResult: videoFile is null")
                }
            }
        } catch (e: TransactionTooLargeException) {
            Log.d("upload", "video too large failed to import with exception: $e")
        }
    }

    private fun uploadVideoTos3(videoUri: Uri) {
        Log.d(TAG, "Fragment uploadVideoTos3: videoURI: $videoUri")
        viewModel.uploadVideoTos3(
            videoUri, requireContext()
        )
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled().let { viewState ->

                addToRecyclerView(
                    Upload(
                        id = viewState!!.id!!,
                        url = viewState.url!!,
                        status = "pending"
                    )
                )

                viewState!!.progress?.let {
                    setProgress(viewState.progress!!)
                    updateState(viewState.state!!)
                }

                viewState.response?.let {
                    if (viewState.response == VideoUploadResponse.Success) {
                        updateState(viewState.state!!)
                        Log.d(TAG, "subscribeObservers: updated data ${viewState.state} on success $uploads")
                        binding.showLoading.visibility = View.GONE
                        Log.d(
                            TAG, "subscribeObservers: Upload with id: ${viewState.id}" +
                                    " was successful ${viewState.message}"
                        )

                    }else if (viewState.response == VideoUploadResponse.Pending){
                        updateState(viewState.state!!)
                        Log.d(TAG, "subscribeObservers: updated data ${viewState.state}  while pending $uploads")
                    } else {
                        updateState(viewState.state!!)
                        Log.d(TAG, "subscribeObservers: updated data ${viewState.state}  on error $uploads")
                        binding.showLoading.visibility = View.GONE
                        Log.d(TAG, "subscribeObservers: Upload Error ${viewState.message}")
                    }
                }

            }

        }
    }

    private fun setProgress(progress: Int) {
        binding.progressBar.progress = progress
        binding.loadingTextValue.text = progress.toString()
    }

    private fun setupRecyclerView() {
        uploadAdapter = UploadAdapter(requireContext(), uploads)
        binding.uploadRecyclerview.setHasFixedSize(true)
        binding.uploadRecyclerview.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )
        binding.uploadRecyclerview.adapter = uploadAdapter
        binding.uploadRecyclerview.visibility = View.VISIBLE
    }

    private fun addToRecyclerView(upload: Upload) {
        if (!uploads.contains(upload)){
            uploads.add(upload)
            uploadAdapter?.let {
                it.notifyItemInserted(uploads.lastIndex + 1)
            }
        }
    }

    private fun updateUrl(id: Int, url: String) {
        uploads.let { uploads ->
            uploads.onEach {
                if (it.id == id) it.url = url
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateState(uploadState: UploadState) {
        uploads.let { uploads ->
            uploads.onEach {

                it.status = when (uploadState) {
                    is UploadState.COMPLETED -> "done"
                    is UploadState.INPROGRESS -> "pending"
                    is UploadState.FAILED -> "failed"
                    else -> {
                        "failed"
                    }
                }
                uploadAdapter?.notifyDataSetChanged()
            }
        }


    }

}