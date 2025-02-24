package com.example.marcacion.data.utils

import android.media.ExifInterface
import java.io.File

class CameraUtils {
    /**
     * Lee el EXIF del [photoFile] y rota el [bitmap] si es necesario.
     */
    fun fixImageOrientation(
        photoFile: File,
        bitmap: android.graphics.Bitmap
    ): android.graphics.Bitmap {
        val exif = ExifInterface(photoFile.absolutePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap // No rotar
        }
    }

    /**
     * Rota el [bitmap] los grados indicados.
     */
    fun rotateBitmap(bitmap: android.graphics.Bitmap, degrees: Float): android.graphics.Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees)
        return android.graphics.Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    /**
     * Comprime el [bitmap] a JPEG hasta que su tamaño sea menor o igual a [maxSizeKB].
     * Retorna un arreglo de bytes (byte[]) con la imagen comprimida.
     */
    fun compressImageToMaxSize(bitmap: android.graphics.Bitmap, maxSizeKB: Int = 500): ByteArray {
        val bos = java.io.ByteArrayOutputStream()
        var quality = 100

        // Comprime inicialmente con calidad 100%
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, bos)

        // Mientras el tamaño en KB sea mayor al límite y la calidad sea > 10
        while ((bos.size() / 1024) > maxSizeKB && quality > 10) {
            bos.reset()
            quality -= 5
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, bos)
        }

        return bos.toByteArray()
    }

}