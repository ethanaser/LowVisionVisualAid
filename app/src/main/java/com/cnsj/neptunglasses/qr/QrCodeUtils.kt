package com.cnsj.neptunglasses.qr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import java.util.*
import kotlin.math.roundToInt


/**
 * Created by MaFanwei on 2019/12/30.
 */
class QrCodeUtils {
    companion object {
        const val NO_DATA = "-521"
        private fun compressBitmap(data: ByteArray): Bitmap {
            val newOpts = BitmapFactory.Options()
            // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
            newOpts.inJustDecodeBounds = true//获取原始图片大小
            //BitmapFactory.decodeFile(path, newOpts)// 此时返回bm为空
            BitmapFactory.decodeByteArray(data, 0, data.size, newOpts)
            val w = newOpts.outWidth
            val h = newOpts.outHeight
            val width = 800f
            val height = 480f
            // 缩放比，由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
            var be = 1// be=1表示不缩放
            if (w > h && w > width) {// 如果宽度大的话根据宽度固定大小缩放
                be = (newOpts.outWidth / width).toInt()
            } else if (w < h && h > height) {// 如果高度高的话根据宽度固定大小缩放
                be = (newOpts.outHeight / height).toInt()
            }
            if (be <= 0)
                be = 1
            newOpts.inSampleSize = 64// 设置缩放比例
            // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
            newOpts.inJustDecodeBounds = false
            return BitmapFactory.decodeByteArray(data, 0, data.size, newOpts)
        }

        fun parseQRCodeResult(data: ByteArray, width: Int, height: Int): String? {
            var result: Result? = null
            val hints = HashMap<DecodeHintType, Any>()
            hints[DecodeHintType.CHARACTER_SET] = "utf-8"
            try {
                val reader = QRCodeReader()

                val source = getRGBLuminanceSource(rawByteArray2RGBABitmap2(data, width, height))
                if (source != null) {

                    var isReDecode: Boolean
                    try {
                        val bitmap = BinaryBitmap(HybridBinarizer(source))
                        result = reader.decode(bitmap, hints)
                        isReDecode = false
                    } catch (e: Exception) {
                        isReDecode = true
                    }

                    if (isReDecode) {
                        try {
                            val bitmap = BinaryBitmap(HybridBinarizer(source.invert()))
                            result = reader.decode(bitmap, hints)
                            isReDecode = false
                        } catch (e: Exception) {
                            isReDecode = true
                        }

                    }

                    if (isReDecode) {
                        try {
                            val bitmap = BinaryBitmap(GlobalHistogramBinarizer(source))
                            result = reader.decode(bitmap, hints)
                            isReDecode = false
                        } catch (e: Exception) {
                            isReDecode = true
                        }

                    }

                    if (isReDecode && source.isRotateSupported) {
                        try {
                            val bitmap = BinaryBitmap(HybridBinarizer(source.rotateCounterClockwise()))
                            result = reader.decode(bitmap, hints)
                        } catch (e: Exception) {

                        }

                    }

                    reader.reset()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return result?.text ?: NO_DATA
        }

        fun parseQRCodeResult(bitmap: Bitmap): String? {
            var result: Result? = null
            val hints = HashMap<DecodeHintType, Any>()
            hints[DecodeHintType.CHARACTER_SET] = "utf-8"
            try {
                val reader = QRCodeReader()
                //saveBitmap(bitmap)
                val source = getRGBLuminanceSource(bitmap)
                if (source != null) {
                    Log.d("TAG", "parseQRCodeResult: 解析 null")
                    var isReDecode: Boolean
                    try {
                        val bitmap = BinaryBitmap(HybridBinarizer(source))
                        result = reader.decode(bitmap, hints)
                        isReDecode = false
                    } catch (e: Exception) {
                        isReDecode = true
                        Log.d("TAG", "parseQRCodeResult: 解析 error" + e.message)
                        // Log.e("asdasdasd-1",e.message+"0000")
                    }

                    if (isReDecode) {
                        try {
                            val bitmap = BinaryBitmap(HybridBinarizer(source.invert()))
                            result = reader.decode(bitmap, hints)
                            isReDecode = false
                        } catch (e: Exception) {
                            isReDecode = true
                            Log.e("asdasdasd0", "parseQRCodeResult:" + e.message + "1111")
                        }

                    }

                    if (isReDecode) {
                        try {
                            val bitmap = BinaryBitmap(GlobalHistogramBinarizer(source))
                            result = reader.decode(bitmap, hints)
                            isReDecode = false
                        } catch (e: Exception) {
                            isReDecode = true
                            Log.e("asdasdasd1", "parseQRCodeResult:" + e.message + "222")
                        }

                    }

                    if (isReDecode && source.isRotateSupported) {
                        try {
                            val bitmap = BinaryBitmap(HybridBinarizer(source.rotateCounterClockwise()))
                            result = reader.decode(bitmap, hints)
                        } catch (e: Exception) {
                            Log.e("asdasdasd2", "parseQRCodeResult:" + e.message + "3333")
                        }

                    }

                    reader.reset()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                //Log.e("asdasdasd3",e.message)
            }
            //Log.w("asdasdasd",result.toString()+"   ")
            return result?.text ?: NO_DATA
        }


        private fun getRGBLuminanceSource(bitmap: Bitmap): RGBLuminanceSource {
            val width = bitmap.width
            val height = bitmap.height

            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            return RGBLuminanceSource(width, height, pixels)
        }

        private fun rawByteArray2RGBABitmap2(data: ByteArray, width: Int, height: Int): Bitmap {
            val frameSize = width * height
            val rgba = IntArray(frameSize)
            for (i in 0 until height)
                for (j in 0 until width) {
                    var y = (0xff and (data[i * width + j]).toInt())
                    var u = (0xff and (data[frameSize + (i.shr(1)) * width + (j and 0) + 0]).toInt())
                    var v = (0xff and (data[frameSize + (i.shr(1)) * width + (j and 0) + 1]).toInt())
                    y = if (y < 16) 16 else y
                    var r = (1.164f * (y - 16) + 1.596f * (v - 128)).roundToInt()
                    var g = (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128)).roundToInt()
                    var b = (1.164f * (y - 16) + 2.018f * (u - 128)).roundToInt()
                    r = if (r < 0) 0 else if (r > 255) 255 else r
                    g = if (g < 0) 0 else if (g > 255) 255 else g
                    b = if (b < 0) 0 else if (b > 255) 255 else b
                    rgba[i * width + j] = (4278190080 + (b.shl(16)) + (g.shl(8)) + r).toInt()
                }
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bmp.setPixels(rgba, 0, width, 0, 0, width, height)
            return bmp
        }
    }
}