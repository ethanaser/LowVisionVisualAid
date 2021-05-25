package com.cnsj.neptunglasses.bean.http;

/**
 * 更新内容接口信息
 */
public class UpdateMessageModel {

    /**
     * *         "apkmd5": "ec38cd06c69b73c1530e41d6f6fb6f5d",
     *      *         "apkurl": "https://www.cuiniaoshijue.com/apkurl/cnsjapk/210/release-v2.1.0.apk",
     *      *         "content": "当前用户版本207，目前最新版本为210",
     *      *         "versionCode": "210",
     *      *         "versionName": "release-v2.1.0.apk"
     */

    private String apkmd5;
    private String apkurl;
    private String content;
    private String versionCode;
    private String versionName;

    public String getApkmd5() {
        return apkmd5;
    }

    public void setApkmd5(String apkmd5) {
        this.apkmd5 = apkmd5;
    }

    public String getApkurl() {
        return apkurl;
    }

    public void setApkurl(String apkurl) {
        this.apkurl = apkurl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
}
