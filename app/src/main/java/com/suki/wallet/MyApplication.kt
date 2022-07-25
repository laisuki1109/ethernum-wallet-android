package com.suki.wallet

import android.content.SharedPreferences
import android.os.Build
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.innopage.core.webservice.retrofit.RetrofitProviderImp
import com.innopage.core.webservice.retrofit.createApiService
import com.suki.wallet.webservice.api.ApiService
import com.suki.wallet.webservice.config.Configurations
import timber.log.Timber
import java.util.*

class MyApplication : MultiDexApplication() {

    enum class FontSize(val value: Int) {
        NORMAL(1), LARGE(2), EXTRA_LARGE(3);

        companion object {
            fun from(findValue: Int): FontSize = values().first { it.value == findValue }
        }
    }

    private lateinit var sharedPreferences: SharedPreferences

    private val defaultLanguage = when {
        Locale.getDefault().toString().contains("en") -> "en"
        Locale.getDefault().toString().contains("zh_TW") || Locale.getDefault().toString().contains(
            "#Hant"
        ) -> "tc"
        else -> "en"
    }

    var appLanguage: String = defaultLanguage
        set(value) {
            field = value
            val editor = sharedPreferences.edit()
            editor.putString("app_language", value)
            editor.apply()
        }

    val appLocale: Locale
        get() {
            return when (appLanguage) {
                "en" -> Locale.ENGLISH
                "tc" -> Locale.TRADITIONAL_CHINESE
                "sc" -> Locale.TRADITIONAL_CHINESE
                else -> Locale.ENGLISH
            }
        }

    var fontSize: FontSize = FontSize.NORMAL
        set(value) {
            field = value
            val editor = sharedPreferences.edit()
            editor.putInt("font_size", value.value)
            editor.apply()
        }

    val fontScale: Float
        get() {
            return when (fontSize) {
                FontSize.NORMAL -> 1.0F
                FontSize.LARGE -> 1.2F
                FontSize.EXTRA_LARGE -> 1.4F
            }
        }

    val osVersion: String = Build.VERSION.RELEASE

    val appVersion: String = BuildConfig.VERSION_NAME

    var pushToken: String = ""
        set(value) {
            field = value
            val editor = sharedPreferences.edit()
            editor.putString("push_token", value)
            editor.apply()
        }

    var pushEnabled: Boolean = true
        set(value) {
            field = value
            val editor = sharedPreferences.edit()
            editor.putBoolean("push_enabled", value)
            editor.apply()
        }

    val deviceId by lazy {
        sharedPreferences.getString("device_id", null).run {
            if (isNullOrEmpty()) {
                val editor = sharedPreferences.edit()
                editor.putString("device_id", UUID.randomUUID().toString())
                editor.apply()
            }
            return@run sharedPreferences.getString("device_id", null)
        }
    }

    var accessToken: String = ""
        set(value) {
            field = value
            val editor = sharedPreferences.edit()
            editor.putString("access_token", value)
            editor.apply()
        }

    var addressWords: String = ""
        set(value) {
            field = value
            val editor = sharedPreferences.edit()
            editor.putString("address_words", value)
            editor.apply()
        }

    lateinit var apiService: ApiService

    companion object {
        lateinit var INSTANCE: MyApplication
    }

    override fun onCreate() {
        super.onCreate()

        // Build debug tree if debug mode
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Set up single instance
        INSTANCE = this

        // Set up shared preferences
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

            sharedPreferences = EncryptedSharedPreferences.create(
                "secret_shared_prefs",
                masterKeyAlias,
                baseContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            // use the shared preferences and editor as you normally would
            val editor = sharedPreferences.edit()

        }else {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
        }


        // Set up values
        appLanguage =
            sharedPreferences.getString("app_language", defaultLanguage) ?: defaultLanguage
        pushToken = sharedPreferences.getString("push_token", null).orEmpty()
        pushEnabled = sharedPreferences.getBoolean("push_enabled", true)
        accessToken = sharedPreferences.getString("access_token", null).orEmpty()
        fontSize = FontSize.from(sharedPreferences.getInt("font_size", 1))
        addressWords = sharedPreferences.getString("address_words", null).orEmpty()

        // Set up api service with domain
        apiService = RetrofitProviderImp(this).createApiService(Configurations.DOMAIN)
    }
}