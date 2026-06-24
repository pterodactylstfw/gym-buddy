package com.corecoders.gymbuddy.utils

import android.graphics.BitmapFactory
import android.util.Base64
import java.io.File

/**
 * Returns a model that Coil's AsyncImage can load (Url, Bitmap, or File).
 * If the input is empty or invalid, returns null.
 */
fun getAvatarModel(uri: String): Any? {
    if (uri.isEmpty()) return null
    if (uri.startsWith("http://") || uri.startsWith("https://")) {
        return uri
    }
    if (uri.startsWith("data:image/jpeg;base64,")) {
        val base64Data = uri.substringAfter("base64,")
        return try {
            val decodedString = Base64.decode(base64Data, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        } catch (e: Exception) {
            null
        }
    }
    if (!uri.startsWith("/") && uri.length > 100) {
        return try {
            val decodedString = Base64.decode(uri, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        } catch (e: Exception) {
            null
        }
    }
    val file = File(uri)
    if (file.exists()) {
        return file
    }
    return null
}
