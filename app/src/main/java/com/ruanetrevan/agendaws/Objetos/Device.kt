package com.ruanetrevan.agendaws.Objetos

import android.content.Context
import android.provider.Settings

class Device {
    companion object {
        @JvmStatic
        fun getSecureId(context: Context): String {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }
    }
}
