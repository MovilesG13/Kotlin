package com.example.monify_kotlin.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Goal(
    val id: String,
    val title: String,
    val currentAmount: Double,
    val targetAmount: Double,
    val icon: String // Icon name as a string
)

class GoalsRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun uid(): String? = auth.currentUser?.uid

    suspend fun getGoals(): List<Goal> {
        val userId = uid() ?: return emptyList()
        val snapshot = db.collection("users").document(userId).collection("goals").get().await()
        return snapshot.documents.mapNotNull { doc ->
            Goal(
                id = doc.id,
                title = doc.getString("title") ?: "",
                currentAmount = doc.getDouble("currentAmount") ?: 0.0,
                targetAmount = doc.getDouble("targetAmount") ?: 0.0,
                icon = doc.getString("icon") ?: ""
            )
        }
    }

    suspend fun addGoal(title: String, targetAmount: Double, icon: String) {
        val userId = uid() ?: return
        val newGoal = hashMapOf(
            "title" to title,
            "currentAmount" to 0.0,
            "targetAmount" to targetAmount,
            "icon" to icon
        )
        db.collection("users").document(userId).collection("goals").add(newGoal).await()
    }
}
