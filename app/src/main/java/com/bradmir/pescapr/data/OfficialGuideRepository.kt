package com.bradmir.pescapr.data

import android.content.Context
import android.util.Log
import com.bradmir.pescapr.FichaPez
import com.bradmir.pescapr.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class OfficialGuideRepository(private val context: Context) {

    private val gson = Gson()

    suspend fun getOfficialGuide(): List<FichaPez> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.resources.openRawResource(R.raw.oficial_guide)
            InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                val type = object : TypeToken<List<FichaPez>>() {}.type
                gson.fromJson<List<FichaPez>>(reader, type) ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("OfficialGuideRepo", "Error reading official_guide.json", e)
            emptyList()
        }
    }
}
