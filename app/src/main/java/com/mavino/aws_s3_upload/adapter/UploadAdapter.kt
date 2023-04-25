package com.mavino.aws_s3_upload.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mavino.aws_s3_upload.R
import com.mavino.aws_s3_upload.model.Upload

class UploadAdapter(
    private val context: Context,
    private var uploads: List<Upload>,
) : RecyclerView.Adapter<UploadAdapter.UploadViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadViewHolder {
        return UploadViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.upload_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return uploads.size
    }

    override fun onBindViewHolder(holder: UploadViewHolder, position: Int) {
        holder.bind(uploads[position])
    }

    inner class UploadViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val fileUrl: TextView = view.findViewById(R.id.url)
        private val status: TextView = view.findViewById(R.id.status)
        private val bg: RelativeLayout = view.findViewById(R.id.status_bg)
        private val copy: RelativeLayout = view.findViewById(R.id.copy_bg)

        fun bind(upload: Upload) {
            fileUrl.text = upload.url
            status.text = upload.status
            if (status.text == "done") {
                bg.background = ContextCompat.getDrawable(context, R.drawable.success)
            } else if (status.text == "failed"){
                bg.background = ContextCompat.getDrawable(context, R.drawable.failure)
            } else if (status.text == "pending"){
                bg.background = ContextCompat.getDrawable(context, R.drawable.pending)
            }

            copy.setOnClickListener{
                context.copyToClipboard(upload.url)
                Toast.makeText(context, "URL copied", Toast.LENGTH_LONG).show()
            }
        }

        private fun Context.copyToClipboard(text: CharSequence){
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label",text)
            clipboard.setPrimaryClip(clip)
        }

    }

}