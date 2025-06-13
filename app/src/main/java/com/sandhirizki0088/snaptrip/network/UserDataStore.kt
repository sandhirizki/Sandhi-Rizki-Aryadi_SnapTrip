package com.sandhirizki0088.snaptrip.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sandhirizki0088.snaptrip.model.User

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


val Context.datastore : DataStore<Preferences> by  preferencesDataStore(
    name = "user-preference"
)

class UserDataStore(private val context: Context) {
    companion object {
        private val USER_NAME = stringPreferencesKey("name")
        private val USER_EMAIL = stringPreferencesKey("email")
        private val USER_PHOTO = stringPreferencesKey("photoUrl")
    }

    val userFlow: Flow<User> = context.datastore.data.map { preferences ->
        User(
            name = preferences[USER_NAME] ?: "",
            email = preferences[USER_EMAIL] ?: "",
            photoUrl = preferences[USER_PHOTO] ?: "",

            )
    }

    suspend fun saveData(user: User){
        context.datastore.edit { preferences ->
            preferences[USER_NAME] = user.name
            preferences[USER_EMAIL] = user.email
            preferences[USER_PHOTO] = user.photoUrl
        }
    }
}