package com.innopage.core.activity

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import java.util.*

/**
 * Created by Benny on 4/24/2017.
 */

class MyContextWrapper(base: Context) : ContextWrapper(base) {

    companion object {
        fun wrap(context: Context, locale: Locale, fontScale: Float = 1.0F): ContextWrapper {
            val config = context.resources.configuration

            // Set language
            Locale.setDefault(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocale(locale)
            } else {
                @Suppress("DEPRECATION")
                config.locale = locale
            }
            config.setLayoutDirection(locale)

            // Set font scale
            config.fontScale = fontScale

            return MyContextWrapper(context.createConfigurationContext(config))
        }
    }
}
