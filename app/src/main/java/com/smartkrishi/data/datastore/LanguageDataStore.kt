package com.smartkrishi.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("language_pref")

class LanguageDataStore(private val context: Context) {

    companion object {
        val LANGUAGE_KEY = stringPreferencesKey("selected_language")
    }

    val selectedLanguage: Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[LANGUAGE_KEY] ?: "en"
        }

    suspend fun saveLanguage(langCode: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = langCode
        }
    }
}
