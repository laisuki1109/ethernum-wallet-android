package com.innopage.core.utility

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


class RootUtils {

    fun isDeviceRooted(): Boolean {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3() || checkRootMethod4()
    }

    private fun checkRootMethod1(): Boolean {
        val buildTags = android.os.Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkRootMethod2(): Boolean {
        // Some XiaoMi devices have "/system/xbin/busybox"
        val paths = arrayOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su",
            "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su",
            "/su/bin/su", "/xbin/su", "/system/xbin/busybox", "/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    private fun checkRootMethod3(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val inputStream = BufferedReader(InputStreamReader(process?.inputStream))
            inputStream.readLine() != null
        } catch (t: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }

    private fun checkRootMethod4(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec("mount -o remount, rw /system")
            val inputStream = BufferedReader(InputStreamReader(process?.inputStream))
            inputStream.readLine() != null
        } catch (t: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }
}