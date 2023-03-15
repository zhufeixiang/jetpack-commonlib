package com.zfx.commonlib.util

import android.content.Context

import android.content.pm.PackageManager
import android.os.Environment
import android.text.TextUtils
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import com.blankj.utilcode.util.AppUtils
import java.io.File
import java.math.BigDecimal


/**
 * author : zhufeixiang
 * date : 2021/12/2
 * des :
 */
object SystemUtil {
    /**
     * 检查手机上是否安装了指定的软件
     *
     * @param context
     * @param packageName：应用包名
     * @return
     */
    fun isAvilible(context: Context, packageName: String): Boolean {
        //获取packagemanager
        val packageManager: PackageManager = context.packageManager
        //获取所有已安装程序的包信息
        val packageInfos = packageManager.getInstalledPackages(0)
        //用于存储所有已安装程序的包名
        val packageNames: MutableList<String> = ArrayList()
        //从pinfo中将包名字逐一取出，压入pName list中
        if (packageInfos != null) {
            for (i in packageInfos.indices) {
                val packName = packageInfos[i].packageName
                packageNames.add(packName)
            }
        }
        //判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
        return packageNames.contains(packageName)
    }

    /**
     * 版本号比较
     *
     * @param v1 服务端版本号
     * @param v2 本地版本号
     * @return 0代表相等，1代表左边大，-1代表右边大
     * Utils.compareVersion("1.0.358_20180820090554","1.0.358_20180820090553")=1
     */
    private fun compareVersion(v1: String, v2: String): Int {
        if ( TextUtils.isEmpty(v1)  && TextUtils.isEmpty(v1)) return 0

        if (v1 == v2) return 0

        if (TextUtils.isEmpty(v1)) return  -1

        if (TextUtils.isEmpty(v2)) return  1

        val version1Array : List<String> = v1.split(".")
        val version2Array : List<String> = v2.split(".")
        val minLen = version1Array.size.coerceAtMost(version2Array.size)
        for (i in 0 until minLen){
            if (version1Array[i].toInt() != version2Array[i].toInt()){
                return if (version1Array[i].toInt() >  version2Array[i].toInt())  1 else -1
            }
        }

        if (version1Array.size == version2Array.size) return 0

        if (version1Array.size > version2Array.size) return -1

        return 1
    }

    fun hasNewVersion(version: String): Boolean {
        return compareVersion(version, AppUtils.getAppVersionName()) > 0
    }

    /**
     * 获取缓存大小
     *
     * @param context
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getTotalCacheSize(context: Context): String {
        var cacheSize = getFolderSize(context.getCacheDir())
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cacheSize += context.getExternalCacheDir()?.let { getFolderSize(it) }!!
        }
        return getFormatSize(cacheSize.toDouble())
    }

    /**
     * 清空缓存
     *
     * @param context
     */
    fun clearAllCache(context: Context): Boolean {
        var b = false
        deleteDir(context.getCacheDir())
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            b = deleteDir(context.getExternalCacheDir())
        }
        return b
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory()) {
            val children: Array<String> = dir.list()
            for (i in children.indices) {
                val success =
                    deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }
        if (dir != null) {
            return dir.delete()
        }
        return false
    }

    // 获取文件
    //Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
    //Context.getExternalCacheDir() --> SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
    @Throws(Exception::class)
    fun getFolderSize(file: File): Long {
        var size: Long = 0
        try {
            val fileList: Array<File> = file.listFiles()
            for (i in fileList.indices) {
                // 如果下面还有文件
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i])
                } else {
                    size = size + fileList[i].length()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }

    /**
     * 格式化单位
     *
     * @param size
     * @return
     */
    fun getFormatSize(size: Double): String {
        val kiloByte = size / 1024
        if (kiloByte < 1) {
//            return size + "Byte";
            return "0.0KB"
        }
        val megaByte = kiloByte / 1024
        if (megaByte < 1) {
            val result1 = BigDecimal(java.lang.Double.toString(kiloByte))
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                .toPlainString().toString() + "KB"
        }
        val gigaByte = megaByte / 1024
        if (gigaByte < 1) {
            val result2 = BigDecimal(java.lang.Double.toString(megaByte))
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                .toPlainString().toString() + "M"
        }
        val teraBytes = gigaByte / 1024
        if (teraBytes < 1) {
            val result3 = BigDecimal(java.lang.Double.toString(gigaByte))
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                .toPlainString().toString() + "GB"
        }
        val result4 = BigDecimal(teraBytes)
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
            .toString() + "TB"
    }

    fun clearWebCache(context: Context) {
        CookieManager.getInstance().removeAllCookies(null)
        WebView(context).clearCache(true)
        WebStorage.getInstance().deleteAllData()
    }

}