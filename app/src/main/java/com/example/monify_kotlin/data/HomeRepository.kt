package com.example.monify_kotlin.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class HomeRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val fx: FirebaseFunctions = FirebaseFunctions.getInstance(),
) {
    private fun uid(): String = auth.currentUser?.uid ?: throw IllegalStateException("No auth")

    suspend fun getProfileName(): String {
        val doc = db.collection("users").document(uid()).collection("profile").document("profile").get().await()
        return doc.getString("displayName") ?: (auth.currentUser?.email ?: "User")
    }

    suspend fun getMonthlySummary(yyyymm: String): Triple<Double, Double, Double> {
        val res = fx.getHttpsCallable("getMonthlySummary").call(mapOf("month" to yyyymm)).await()
        val m = res.data as Map<*, *>
        val income = (m["income"] as Number?)?.toDouble() ?: 0.0
        val expenses = (m["expenses"] as Number?)?.toDouble() ?: 0.0
        val balance = income - expenses
        return Triple(income, expenses, balance)
    }
}
