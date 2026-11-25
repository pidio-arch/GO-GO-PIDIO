package com.tareasverde.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import java.io.File

/** Guarda y carga la foto que el usuario elige como fondo del wallpaper. */
object WallpaperBg {
    private fun file(ctx: Context) = File(ctx.filesDir, "bg.jpg")

    /** Copia la foto elegida (reducida para no gastar memoria). Devuelve true si salió bien. */
    fun saveFrom(ctx: Context, uri: Uri): Boolean {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            val longest = maxOf(bounds.outWidth, bounds.outHeight)
            while (longest / sample > 2400) sample *= 2

            val decoded = ctx.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return false

            val orientation = runCatching {
                ctx.contentResolver.openInputStream(uri)?.use {
                    android.media.ExifInterface(it).getAttributeInt(
                        android.media.ExifInterface.TAG_ORIENTATION,
                        android.media.ExifInterface.ORIENTATION_NORMAL,
                    )
                }
            }.getOrNull() ?: android.media.ExifInterface.ORIENTATION_NORMAL
            val degrees = when (orientation) {
                android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            val finalBmp = if (degrees != 0f) {
                Bitmap.createBitmap(
                    decoded, 0, 0, decoded.width, decoded.height,
                    Matrix().apply { postRotate(degrees) }, true,
                )
            } else {
                decoded
            }

            file(ctx).outputStream().use { finalBmp.compress(Bitmap.CompressFormat.JPEG, 92, it) }
            AppPrefs.setBgVersion(ctx, AppPrefs.bgVersion(ctx) + 1)
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Carga la foto lo bastante grande para llenar reqW x reqH, o null si no hay. */
    fun load(ctx: Context, reqW: Int, reqH: Int): Bitmap? {
        val f = file(ctx)
        if (!f.exists()) return null
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(f.path, bounds)
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= reqW && bounds.outHeight / (sample * 2) >= reqH) {
                sample *= 2
            }
            BitmapFactory.decodeFile(f.path, BitmapFactory.Options().apply { inSampleSize = sample })
        } catch (e: Exception) {
            null
        }
    }
}
