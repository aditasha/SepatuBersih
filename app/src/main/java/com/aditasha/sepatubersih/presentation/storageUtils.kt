package com.aditasha.sepatubersih.presentation

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*

fun uriToFile(uri: Uri, context: Context): File {
    val contentResolver = context.contentResolver
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val myFile = File.createTempFile(Date().time.toString(), ".jpg", storageDir)

    val inputStream = contentResolver.openInputStream(uri) as InputStream
    val outputStream: OutputStream = FileOutputStream(myFile)
    val buf = ByteArray(1024)
    var len: Int
    while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
    outputStream.close()
    inputStream.close()


    return myFile
}