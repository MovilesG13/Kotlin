package com.example.monify_kotlin

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.BuildConfig
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory // <-- Importa esto
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class MonifyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
    }
}