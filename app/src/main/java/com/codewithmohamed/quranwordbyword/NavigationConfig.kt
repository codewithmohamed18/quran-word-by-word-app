package com.codewithmohamed.quranwordbyword

import android.content.Context
import org.json.JSONObject

data class Chapter(val number: Int, val arabic: String, val name: String, val page: Int)
data class NavigationConfig(val juz: List<Chapter>, val surahs: List<Chapter>) {
    companion object {
        fun load(context: Context): NavigationConfig = context.assets.open("navigation.json")
            .bufferedReader().use { parse(it.readText()) }
        fun parse(text: String): NavigationConfig {
            val root = JSONObject(text)
            require(root.getInt("pageCount") == 960)
            fun chapters(key: String, expected: Int): List<Chapter> {
                val array = root.getJSONArray(key)
                require(array.length() == expected)
                return (0 until array.length()).map { i ->
                    val obj = array.getJSONObject(i)
                    Chapter(obj.getInt("number"), obj.optString("arabic"), obj.getString("name"), obj.getInt("page"))
                }.also { list ->
                    require(list.map { it.number } == (1..expected).toList())
                    require(list.all { it.page in 1..960 && it.name.isNotBlank() })
                    require(list.zipWithNext().all { (a,b) -> a.page <= b.page })
                }
            }
            return NavigationConfig(chapters("juz", 30), chapters("surahs", 114))
        }
    }
}
