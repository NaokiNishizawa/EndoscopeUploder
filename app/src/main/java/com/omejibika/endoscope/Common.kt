package com.omejibika.endoscope

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class Common {
    companion object {
        /**
         * ダイアログの表示
         */
        //fun showDialog(context: Context, title: String, text: String) {
        //    val ad: AlertDialog.Builder = AlertDialog.Builder(context)
        //    ad.setTitle(title)
        //    ad.setMessage(text)
        //    ad.setPositiveButton("OK", null)
        //    ad.show()
        //}

        /**
         * SDカードのfilesディレクトリパスのリストを取得する。
         * Android5.0以上対応。
         *
         * @param context
         * @return SDカードのfilesディレクトリパスのリスト
         */
        fun getSdCardFilesDirPathListForLollipop(context: Context): List<String> {
            val sdCardFilesDirPathList = ArrayList<String>()

            // getExternalFilesDirsはAndroid4.4から利用できるAPI。
            // filesディレクトリのリストを取得できる。
            val dirArr = context.getExternalFilesDirs(null)

            for (dir in dirArr) {
                if (dir != null) {
                    var path = dir.getAbsolutePath()
                    path = path.substring(0, path.indexOf("Android") - 1)

                    // isExternalStorageRemovableはAndroid5.0から利用できるAPI。
                    // 取り外し可能かどうか（SDカードかどうか）を判定している。
                    if (Environment.isExternalStorageRemovable(dir)) {

                        // 取り外し可能であればSDカード。
                        if (!sdCardFilesDirPathList.contains(path)) {
                            sdCardFilesDirPathList.add(path)
                        }

                    } else {
                        // 取り外し不可能であれば内部ストレージ。
                    }
                }
            }
            return sdCardFilesDirPathList
        }

        fun getImageUri(inContext: Context, inImage: Bitmap, quality: Int): Uri {
            var bytes = ByteArrayOutputStream()
            inImage.compress(Bitmap.CompressFormat.JPEG, quality, bytes)
            var path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
            return Uri.parse(path)
        }

        fun getCameraPhotoOrientation(context: Context, imageUri: Uri?, imagePath: String?): Int {
            var rotate = 0
            try {
                context.contentResolver.notifyChange(imageUri, null)
                val imageFile = File(imagePath)
                val exif = ExifInterface(imageFile.absolutePath)
                val orientation: Int = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
                }
                Log.i("RotateImage", "Exif orientation: $orientation")
                Log.i("RotateImage", "Rotate value: $rotate")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return rotate
        }
    }
}