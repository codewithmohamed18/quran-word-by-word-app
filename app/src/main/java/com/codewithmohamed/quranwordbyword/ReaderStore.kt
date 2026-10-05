package com.codewithmohamed.quranwordbyword

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*
import java.io.IOException

private val Context.readerDataStore by preferencesDataStore("quran_reader_v3")
data class ReadingPreferences(
    val lastPage: Int = 1,
    val bookmarks: Set<Int> = emptySet(),
    val theme: String = "system",
    val keepAwake: Boolean = false
)
class ReaderStore(context: Context) {
    private val store = context.applicationContext.readerDataStore
    private val last = intPreferencesKey("last_page")
    private val saved = stringSetPreferencesKey("bookmarks")
    private val mode = stringPreferencesKey("theme")
    private val awake = booleanPreferencesKey("keep_awake")
    val preferences: Flow<ReadingPreferences> = store.data.catch {
        if (it is IOException) emit(emptyPreferences()) else throw it
    }.map { data ->
        ReadingPreferences(
            (data[last] ?: 1).coerceIn(1, 960),
            (data[saved] ?: emptySet()).mapNotNull { it.toIntOrNull()?.takeIf { n -> n in 1..960 } }.toSet(),
            data[mode] ?: "system", data[awake] ?: false
        )
    }
    suspend fun setPage(page: Int) { store.edit { it[last] = page.coerceIn(1,960) } }
    suspend fun toggleBookmark(page: Int) {
        require(page in 1..960)
        store.edit { prefs ->
            val bookmarks = (prefs[saved] ?: emptySet()).toMutableSet()
            if (!bookmarks.remove(page.toString())) bookmarks.add(page.toString())
            prefs[saved] = bookmarks
        }
    }
    suspend fun setTheme(theme: String) { require(theme in listOf("system","light","dark")); store.edit { it[mode] = theme } }
    suspend fun setKeepAwake(value: Boolean) { store.edit { it[awake] = value } }
    suspend fun migrateLegacy(context: Context) {
        val marker = booleanPreferencesKey("migrated")
        store.edit { target ->
            if (target[marker] != true) {
                val old = context.getSharedPreferences("reader", Context.MODE_PRIVATE)
                val id = "bundled_quran_960_v1"
                target[last] = old.getInt("$id.last", 1).coerceIn(1,960)
                target[saved] = old.getStringSet("$id.bookmarks", emptySet()) ?: emptySet()
                target[marker] = true
            }
        }
    }
}
