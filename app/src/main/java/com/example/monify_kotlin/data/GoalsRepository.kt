package com.example.monify_kotlin.data

import com.example.monify_kotlin.data.local.GoalDao
import com.example.monify_kotlin.data.local.GoalEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

data class Goal(
    val id: String,
    val title: String,
    val currentAmount: Double,
    val targetAmount: Double,
    val icon: String // Icon name as a string
)

class GoalsRepository(
    private val goalDao: GoalDao,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun uid(): String? = auth.currentUser?.uid

    // The UI will now observe this Flow, which is backed by the local database
    val goals: Flow<List<Goal>> = goalDao.getGoals().map { entities ->
        entities.map { it.toDomainModel() }
    }

    // Fetches fresh goals from Firebase and saves them to the local database
    suspend fun refreshGoals() {
        val userId = uid() ?: return
        try {
            val snapshot = db.collection("users").document(userId).collection("goals").get().await()
            val goalsFromNetwork = snapshot.documents.mapNotNull { doc ->
                Goal(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    currentAmount = doc.getDouble("currentAmount") ?: 0.0,
                    targetAmount = doc.getDouble("targetAmount") ?: 0.0,
                    icon = doc.getString("icon") ?: ""
                )
            }
            // Save the fresh data to Room
            goalDao.insertAll(goalsFromNetwork.map { it.toEntity() })
        } catch (e: Exception) {
            // Handle error (e.g., no internet connection)
            // The UI will continue to show data from the local database
        }
    }

    // Adds a new goal to Firebase, then refreshes the local database
    suspend fun addGoal(title: String, targetAmount: Double, icon: String) {
        val userId = uid() ?: return
        val newGoal = hashMapOf(
            "title" to title,
            "currentAmount" to 0.0,
            "targetAmount" to targetAmount,
            "icon" to icon
        )
        db.collection("users").document(userId).collection("goals").add(newGoal).await()
        // Refresh the local cache after adding a new goal
        refreshGoals()
    }
}

// Extension functions to map between domain model and database entity
fun Goal.toEntity(): GoalEntity = GoalEntity(
    id = this.id,
    title = this.title,
    currentAmount = this.currentAmount,
    targetAmount = this.targetAmount,
    icon = this.icon
)

fun GoalEntity.toDomainModel(): Goal = Goal(
    id = this.id,
    title = this.title,
    currentAmount = this.currentAmount,
    targetAmount = this.targetAmount,
    icon = this.icon
)
