package com.cnsj.neptunglasses.bean.http;

/**
 * 后台更新接口返回
 */
public class UpdateModel {
    /**
     * {
     *     "errmsg": "成功更新到版本2.1.0",
     *     "errno": "0",
     *     "message": {
     *         "apkmd5": "ec38cd06c69b73c1530e41d6f6fb6f5d",
     *         "apkurl": "https://www.cuiniaoshijue.com/apkurl/cnsjapk/210/release-v2.1.0.apk",
     *         "content": "当前用户版本207，目前最新版本为210",
     *         "versionCode": "210",
     *         "versionName": "release-v2.1.0.apk"
     *     }
     * }
     */

    private String errmsg;
    private Integer errno;
    private UpdateMessageModel message;


    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public Integer getErrno() {
        return errno;
    }

    public void setErrno(Integer errno) {
        this.errno = errno;
    }

    public UpdateMessageModel getMessage() {
        return message;
    }

    public void setMessage(UpdateMessageModel message) {
        this.message = message;
    }
}
