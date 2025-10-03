package com.example.monify_kotlin.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ExpenseRepository(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    /**
     * Sube una imagen de recibo a Firebase Storage
     * @param imageUri URI de la imagen capturada
     * @return URL pública de la imagen subida
     */
    suspend fun uploadReceiptImage(imageUri: Uri): String {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val fileName = "receipt_${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference
            .child("users")
            .child(uid)
            .child("receipts")
            .child(fileName)

        // Subir la imagen
        storageRef.putFile(imageUri).await()

        // Obtener la URL de descarga
        return storageRef.downloadUrl.await().toString()
    }

    /**
     * Crea un nuevo gasto en Firebase
     * @param amount Cantidad del gasto
     * @param currency Moneda (ej: "USD")
     * @param categoryId ID de la categoría (ej: "food", "transport")
     * @param description Descripción opcional del gasto
     * @param note Notas adicionales opcionales
     * @param date Fecha del gasto en formato ISO 8601 (ej: "2025-10-03T10:30:00")
     * @param receiptImageUrl URL de la imagen del recibo (opcional)
     */
    suspend fun createExpense(
        amount: Double,
        currency: String,
        categoryId: String,
        description: String? = null,
        note: String? = null,
        date: String? = null,
        receiptImageUrl: String? = null
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
        receiptImageUrl?.let { data["receiptImageUrl"] = it }

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