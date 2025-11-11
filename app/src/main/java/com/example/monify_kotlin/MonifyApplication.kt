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

        // 1. Initialize Firebase
        FirebaseApp.initializeApp(this)

        // 2. Initialize App Check
        /*
        val firebaseAppCheck = FirebaseAppCheck.getInstance()

        // 3. (ESTE ES EL CAMBIO)
        // Instala el proveedor de DEPURACIÓN si estás en un build de debug,
        // de lo contrario, instala Play Integrity para release.
        if (BuildConfig.DEBUG) {
            // MODO DEBUG
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            // MODO RELEASE (producción)
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
         */
    }
}