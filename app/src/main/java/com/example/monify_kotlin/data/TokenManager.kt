package com.example.monify_kotlin.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val TOKEN_TIMESTAMP_KEY = stringPreferencesKey("token_timestamp")
    }

    suspend fun saveToken(token: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token
            preferences[USER_EMAIL_KEY] = email
            preferences[TOKEN_TIMESTAMP_KEY] = System.currentTimeMillis().toString()
        }
    }

    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[JWT_TOKEN_KEY]
        }
    }

    fun getUserEmail(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_EMAIL_KEY]
        }
    }

    suspend fun isTokenValid(): Boolean {
        var isValid = false
        context.dataStore.data.map { preferences ->
            val timestamp = preferences[TOKEN_TIMESTAMP_KEY]?.toLongOrNull() ?: 0L
            val currentTime = System.currentTimeMillis()
            val tokenAge = currentTime - timestamp
            // Token valid for 30 days (in milliseconds)
            isValid = tokenAge < 30L * 24 * 60 * 60 * 1000
        }.collect { }
        return isValid
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(TOKEN_TIMESTAMP_KEY)
        }
    }
}