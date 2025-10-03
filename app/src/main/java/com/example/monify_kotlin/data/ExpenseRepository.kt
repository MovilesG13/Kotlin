package com.example.monify_kotlin.data

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class ExpenseRepository(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
) {

    /**
     * Crea un nuevo gasto en Firebase
     * @param amount Cantidad del gasto
     * @param currency Moneda (ej: "USD")
     * @param categoryId ID de la categoría (ej: "food", "transport")
     * @param description Descripción opcional del gasto
     * @param note Notas adicionales opcionales
     * @param date Fecha del gasto en formato ISO 8601 (ej: "2025-10-03T10:30:00")
     */
    suspend fun createExpense(
        amount: Double,
        currency: String,
        categoryId: String,
        description: String? = null,
        note: String? = null,
        date: String? = null
    ) {
        val data = hashMapOf<String, Any>(
            "amount" to amount,
            "currency" to currency,
            "categoryId" to categoryId
        )

        // Agregar campos opcionales solo si no son nulos
        description?.let { data["description"] = it }
        note?.let { data["note"] = it }
        date?.let { data["date"] = it }

        functions.getHttpsCallable("createExpense")
            .call(data)
            .await()
    }

    /**
     * Obtiene el resumen mensual de gastos e ingresos
     * @param month Mes en formato YYYY-MM (ej: "2025-10")
     * @return Map con totalExpenses, totalIncome y byCategory
     */
    suspend fun getMonthlySummary(month: String): Map<String, Any?> {
        val data = hashMapOf("month" to month)
        val result = functions.getHttpsCallable("getMonthlySummary")
            .call(data)
            .await()

        @Suppress("UNCHECKED_CAST")
        return result.data as Map<String, Any?>
    }
}