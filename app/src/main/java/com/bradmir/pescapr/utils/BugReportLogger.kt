package com.bradmir.pescapr.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.bradmir.pescapr.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BugReportLogger {
    private const val TAG = "BugReportLogger"
    private const val FILE_NAME = "debug_bug_reports.txt"
    private const val DIVIDER = "\n========================================\n"

    fun append(context: Context, title: String, description: String): Boolean = try {
        val entry = buildString {
            append(DIVIDER)
            append("Fecha y hora: ")
            append(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            append('\n')
            if (title.isNotBlank()) append("Título: ${title.trim()}\n")
            append("Descripción:\n${description.trim()}\n")
            append("Versión de la app: ${BuildConfig.VERSION_NAME}\n")
            append("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
            append("Dispositivo: ${Build.MANUFACTURER} ${Build.MODEL}\n")
        }
        File(context.filesDir, FILE_NAME).appendText(entry, Charsets.UTF_8)
        true
    } catch (exception: Exception) {
        Log.e(TAG, "No se pudo guardar el reporte de error", exception)
        false
    }

    fun read(context: Context): String = try {
        val file = File(context.filesDir, FILE_NAME)
        if (file.exists()) file.readText(Charsets.UTF_8) else ""
    } catch (exception: Exception) {
        Log.e(TAG, "No se pudo leer el registro de errores", exception)
        ""
    }

    fun clear(context: Context): Boolean = try {
        val file = File(context.filesDir, FILE_NAME)
        !file.exists() || file.delete()
    } catch (exception: Exception) {
        Log.e(TAG, "No se pudo borrar el registro de errores", exception)
        false
    }
}
