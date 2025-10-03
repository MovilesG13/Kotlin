package com.example.monify_kotlin.data

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class FunctionsRepository(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
) {
    suspend fun createExpense(amount: Double, currency: String, categoryId: String) {
        val data = mapOf("amount" to amount, "currency" to currency, "categoryId" to categoryId)
        functions.getHttpsCallable("createExpense").call(data).await()
    }

    suspend fun createIncome(amount: Double, currency: String, source: String?) {
        val data = mapOf("amount" to amount, "currency" to currency, "source" to (source ?: ""))
        functions.getHttpsCallable("createIncome").call(data).await()
    }

    suspend fun getMonthlySummary(month: String): Map<String, Any?> {
        val res = functions.getHttpsCallable("getMonthlySummary").call(mapOf("month" to month)).await()
        @Suppress("UNCHECKED_CAST")
        return res.data as Map<String, Any?>
    }

    // Para tu pregunta (Top categorías) desde backend:
    suspend fun getTopCategories(month: String? = null, topN: Int = 5): List<Pair<String, Double>> {
        val payload = mutableMapOf<String, Any>("topN" to topN)
        month?.let { payload["month"] = it }
        val res = functions.getHttpsCallable("getTopCategories").call(payload).await()
        val map = res.data as Map<*, *>
        val list = map["top"] as List<Map<String, Any>>
        return list.map { (it["categoryId"] as String) to (it["total"] as Number).toDouble() }
    }
}
