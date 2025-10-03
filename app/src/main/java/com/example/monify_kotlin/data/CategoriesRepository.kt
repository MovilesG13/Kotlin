package com.example.monify_kotlin.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CategoriesRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun uid(): String = auth.currentUser?.uid ?: throw IllegalStateException("No auth")

    suspend fun ensureDefaultCategories(): List<String> {
        val ref = db.collection("users").document(uid()).collection("categories")
        val snap = ref.limit(1).get().await()
        if (!snap.isEmpty) return getCategories()

        val defaults = listOf("Food", "Transport", "Bills", "Shopping", "Other")
        val batch = db.batch()
        defaults.forEach { name ->
            val doc = ref.document(name.lowercase())
            batch.set(doc, mapOf("name" to name))
        }
        batch.commit().await()
        return defaults
    }

    suspend fun getCategories(): List<String> {
        val ref = db.collection("users").document(uid()).collection("categories")
        val snap = ref.get().await()
        return snap.documents.mapNotNull { it.getString("name") }.ifEmpty { ensureDefaultCategories() }
    }
}
