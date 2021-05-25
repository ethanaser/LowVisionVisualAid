package com.cnsj.neptunglasses.utils

import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
//import com.elvishew.xlog.XLog
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.roundToInt


/**
 * Created by MaFanwei on 2020/1/14.
 */
class CommonFileUtils {
    companion object {
        const val OCR_FILE_NAME = "/ocr_file_name.jpg"

        fun saveOCRFile(bitmap: Bitmap, scale: Float) {
            var bmp = cropBitmap(bitmap, scale)
            saveBitmap(bmp)
        }

        fun getOCRFile(): File {
            return File(Environment.getExternalStorageDirectory(), OCR_FILE_NAME)
        }

        private fun cropBitmap(bitmap: Bitmap, scale: Float): Bitmap {
            val w = bitmap.width
            val h = bitmap.height
            val cropWidth = h / scale
            val cropHeight = h / scale
            val startW = (w - cropWidth) / 2
            val startH = (h - cropHeight) / 2
            return Bitmap.createBitmap(bitmap, startW.toInt(), startH.toInt(), cropWidth.toInt(), cropHeight.toInt(), null, false)
        }

        private fun saveBitmap(bmp: Bitmap) {
//            XLog.d("保存图片")
            Log.d("TAG", "saveBitmap: ocrocr 保存图片")
            val f = File(Environment.getExternalStorageDirectory(), OCR_FILE_NAME)
            if (f.exists()) {
                f.delete()
            }
            try {
                val out = FileOutputStream(f)
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                out.close()
//                XLog.d("已经保存")
                Log.d("TAG", "saveBitmap: ocrcor 已经保存")
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun getFreezeImageSize(path: String): Int {
            var list = ArrayList<String>();
            val scanner5Directory = File(path)
            if (scanner5Directory.isDirectory) {
                for (file in scanner5Directory.listFiles()) {
                    val path = file.absolutePath
                    if (path.toLowerCase().endsWith(".jpg") || path.toLowerCase().endsWith(".jpeg") || path.toLowerCase().endsWith(".png")) {
                        list.add(path)
                    }
                }
                return list.size;
            }
            return 0;
        }

        fun getFreezeImageList(path: String): List<String>? {
            var list = ArrayList<String>();
            val scanner5Directory = File(path)
            if (scanner5Directory.isDirectory) {
                for (file in scanner5Directory.listFiles()) {
                    val path = file.absolutePath
                    if (path.toLowerCase().endsWith(".jpg") || path.toLowerCase().endsWith(".jpeg") || path.toLowerCase().endsWith(".png")) {
                        list.add(0, path)
                    }
                }
            }
            return list;
        }
    }


}