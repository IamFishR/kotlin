package com.win11launcher.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toStringList(json: String?): List<String>? {
        return Gson().fromJson(json, object : TypeToken<List<String>>() {}.type)
    }
}