package com.cnsj.neptunglasses.utils

import com.xuexiang.xupdate.proxy.IUpdateHttpService
import com.zhy.http.okhttp.OkHttpUtils
import com.zhy.http.okhttp.callback.FileCallBack
import com.zhy.http.okhttp.callback.StringCallback
import com.cnsj.neptunglasses.app.Constant
import okhttp3.*
import org.json.JSONObject

import java.io.File
import java.io.IOException
import java.util.*


/**
 * Created by MaFanwei on 2019/12/24.
 */
class OKHttpUpdateHttpService : IUpdateHttpService {
    override fun asyncGet(url: String, params: MutableMap<String, Any>, callBack: IUpdateHttpService.Callback) {
        val okHttpClient = OkHttpClient()
        val request = Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build()
        val call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callBack.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                callBack.onSuccess(response.toString())
            }
        })
    }

    fun asyncPost(url: String, params: RequestBody, callBack: IUpdateHttpService.Callback) {
        val okHttpClient = OkHttpClient();
        val request = Request.Builder()
                .url(url)
                .post(params)
                .build();
        val call = okHttpClient.newCall(request);
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callBack.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                callBack.onSuccess(response.body()?.string())
            }
        });

    }

    override fun asyncPost(url: String, params: MutableMap<String, Any>, callBack: IUpdateHttpService.Callback) {
        OkHttpUtils.post()
                .url(url)
                .params(transform(params))
                .build()
                .execute(object : StringCallback() {
                    override fun onError(call: Call, e: Exception, id: Int) {
                        callBack.onError(e)
                    }

                    override fun onResponse(response: String, id: Int) {
                        callBack.onSuccess(response)
                        // callBack.onSuccess(fakeJson())
                    }
                })
    }

    /**
     * {"Msg":"",
     * "Code":0,
     * "UpdateStatus":0,
     * "VersionCode":null,
     * "VersionName":null,
     * "ModifyContent":null,
     * "DownloadUrl":"http:\/\/server.cuiniaoshijue.com:9001\/update\/apk\/null",
     * "ApkSize":null,
     * "ApkMd5":null,
     * "UploadTime":null}
     */

    private fun convertJson(s: String): String {
        val result = JSONObject()
        var json = JSONObject(s)
        result.put("Msg", json["msg"])
        result.put("Code", json["code"])
        json = json["data"] as JSONObject
        result.put("UpdateStatus", json["updateStatus"])
        result.put("VersionCode", json["versionCode"])
        result.put("VersionName", json["versionName"])
        result.put("ModifyContent", json["modifyContent"])
        result.put("DownloadUrl", Constant.SNAP_DOWNLOAD_URL + json["downloadUrl"])
        result.put("ApkSize", json["apkSize"])
        result.put("ApkMd5", json["apkMd5"])
        result.put("UploadTime", json["uploadTime"])
        return result.toString()
    }

    override fun cancelDownload(url: String) {
        OkHttpUtils.get()
                .url(url)
                .build().cancel();
    }

    override fun download(url: String, path: String, fileName: String, callback: IUpdateHttpService.DownloadCallback) {
        OkHttpUtils.get()
                .url(url)
                .build()
                .execute(object : FileCallBack(path, fileName) {
                    override fun inProgress(progress: Float, total: Long, id: Int) {
                        callback.onProgress(progress, total)
                    }

                    override fun onError(call: Call, e: Exception, id: Int) {
                        callback.onError(e)
                    }

                    override fun onResponse(response: File, id: Int) {
                        callback.onSuccess(response)
                    }

                    override fun onBefore(request: Request?, id: Int) {
                        super.onBefore(request, id)
                        callback.onStart()
                    }
                })
    }

    private fun transform(params: Map<String, Any>): Map<String, String> {
        val map = TreeMap<String, String>()
        for ((key, value) in params) {
            map[key] = value.toString()
        }
        return map
    }

}
