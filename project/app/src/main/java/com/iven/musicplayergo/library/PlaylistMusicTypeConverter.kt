package com.iven.musicplayergo.library

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iven.musicplayergo.models.Music

class PlaylistMusicTypeConverter {

    private val mGson = Gson()
    @TypeConverter
    fun fromString(value: String): Pair<Music, Int> =
        mGson.fromJson(value, object : TypeToken<Pair<Music, Int>>() {}.type)

    @TypeConverter
    fun fromModel(value: Pair<Music, Int>): String? = mGson.toJson(value)

}
