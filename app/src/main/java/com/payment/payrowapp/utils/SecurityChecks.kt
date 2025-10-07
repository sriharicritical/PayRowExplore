package com.payment.payrowapp.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi

class SecurityChecks {
    companion object {
        // emulator check
        fun checkEmulator(context: Context?): Boolean {
            try {
                val goldfish = getSystemProperty("ro.hardware")!!.contains("goldfish")
                val emu = getSystemProperty("ro.kernel.qemu")!!.isNotEmpty()
                val sdk = getSystemProperty("ro.product.model") == "sdk"
                if (emu || goldfish || sdk) {
                    return true
                }
            } catch (e: Exception) {
            }
            return false
        }

        @Throws(java.lang.Exception::class)
        private fun getSystemProperty(name: String): String? {
            val systemPropertyClazz = Class.forName("android.os.SystemProperties")
            return systemPropertyClazz.getMethod("get", *arrayOf<Class<*>>(String::class.java))
                .invoke(systemPropertyClazz, *arrayOf<Any>(name)) as String
        }

        // debug check
        fun checkDebuggable(context: Context): Boolean {
            return context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        }

        // for developer option
        fun isDevOption(context: Context) {
            if (isDevMode(context)) {
                try {
                    val intent = Intent()
                    intent.action =
                        Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e: android.content.ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        fun isDevMode(context: Context): Boolean {
            return when {
                Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN -> {
                    Settings.Secure.getInt(
                        context.contentResolver,
                        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
                    ) != 0
                }
                Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN -> {
                    @Suppress("DEPRECATION")
                    Settings.Secure.getInt(
                        context.contentResolver,
                        Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED, 0
                    ) != 0
                }
                else -> false
            }
        }
    }
}