package com.example.monify_kotlin.data

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class IncomeRepository(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
) {

    /**
     * Crea un nuevo ingreso en Firebase
     * @param amount Cantidad del ingreso
     * @param currency Moneda (ej: "USD")
     * @param source Fuente del ingreso (ej: "Salary", "Freelance")
     * @param description Descripción opcional del ingreso
     * @param date Fecha del ingreso en formato ISO 8601 (ej: "2025-10-03T10:30:00")
     */
    suspend fun createIncome(
        amount: Double,
        currency: String,
        source: String,
        description: String? = null,
        date: String? = null
    ) {
        val data = hashMapOf<String, Any>(
            "amount" to amount,
            "currency" to currency,
            "source" to source
        )

        // Agregar campos opcionales solo si no son nulos
        description?.let { data["description"] = it }
        date?.let { data["date"] = it }

        functions.getHttpsCallable("createIncome")
            .call(data)
            .await()
    }
}