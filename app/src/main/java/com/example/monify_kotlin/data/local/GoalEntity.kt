package com.example.monify_kotlin.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val currentAmount: Double,
    val targetAmount: Double,
    val icon: String
)
