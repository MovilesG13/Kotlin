package com.example.monify_kotlin.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para crear el DataStore (Singleton)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    private val dataStore = context.dataStore

    // Keys
    companion object {
        val USER_NAME = stringPreferencesKey("user_name")
        val CURRENCY = stringPreferencesKey("currency") // USD, COP, EUR
        val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    // --- Lectura (Flows Reactivos) ---
    val userName: Flow<String> = dataStore.data.map { prefs ->
        prefs[USER_NAME] ?: "Usuario Demo" // Valor por defecto
    }

    val currency: Flow<String> = dataStore.data.map { prefs ->
        prefs[CURRENCY] ?: "USD"
    }

    val isDarkMode: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DARK_MODE] ?: false
    }

    // --- Escritura ---
    suspend fun updateUserName(name: String) {
        dataStore.edit { prefs -> prefs[USER_NAME] = name }
    }

    suspend fun updateCurrency(currency: String) {
        dataStore.edit { prefs -> prefs[CURRENCY] = currency }
    }

    suspend fun toggleDarkMode(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[DARK_MODE] = enabled }
    }
}