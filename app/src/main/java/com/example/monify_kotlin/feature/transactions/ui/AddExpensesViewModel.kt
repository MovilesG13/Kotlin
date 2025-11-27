package com.example.monify_kotlin.feature.transactions.ui

import android.app.Application
import android.net.Uri
import android.os.Build
import android.util.ArrayMap
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monify_kotlin.core.util.ConnectivityObserver
import com.example.monify_kotlin.data.ExpenseRepository
import com.example.monify_kotlin.data.cache.AppDatabase
import com.example.monify_kotlin.data.cache.ImageCacheManager
import com.example.monify_kotlin.data.cache.PendingExpense
import com.example.monify_kotlin.data.sync.TransactionSyncWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class AddExpenseUiState(
    val amount: String = "",
    val description: String = "",
    val category: String = "Food",
    val notes: String = "",
    val date: LocalDate? = null,
    val receiptImageUri: Uri? = null,
    val receiptImageUrl: String? = null,
    val isUploadingImage: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val successMessage: String? = null,
    val isConnected: Boolean = true,
    val isOfflineMode: Boolean = false
)

class AddExpenseViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ExpenseRepository()
    private val database = AppDatabase.getDatabase(application)
    private val imageCacheManager = ImageCacheManager(application)
    private val connectivityObserver = ConnectivityObserver(application)

    // ArrayMap cache for storing expense metadata
    private val expenseCache = ArrayMap<String, Any>()

    var uiState by mutableStateOf(AddExpenseUiState())
        private set

    init {
        observeConnectivity()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            var wasOffline = false
            connectivityObserver.isConnected.collect { isConnected ->
                uiState = uiState.copy(
                    isConnected = isConnected,
                    isOfflineMode = !isConnected
                )

                // Trigger sync when coming back online
                if (isConnected && wasOffline) {
                    android.util.Log.d("AddExpenseViewModel", "Back online - triggering sync")
                    TransactionSyncWorker.scheduleImmediate(getApplication())
                }
                wasOffline = !isConnected
            }
        }
    }

    fun updateAmount(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
            uiState = uiState.copy(amount = value, error = null)
            expenseCache["amount"] = value
        }
    }

    fun updateDescription(value: String) {
        uiState = uiState.copy(description = value)
        expenseCache["description"] = value
    }

    fun updateCategory(value: String) {
        uiState = uiState.copy(category = value)
        expenseCache["category"] = value
    }

    fun updateNotes(value: String) {
        uiState = uiState.copy(notes = value)
        expenseCache["notes"] = value
    }

    fun updateDate(value: LocalDate?) {
        uiState = uiState.copy(date = value, error = null)
        value?.let { expenseCache["date"] = it }
    }

    fun uploadReceiptImage(imageUri: Uri) {
        uiState = uiState.copy(isUploadingImage = true, error = null)

        viewModelScope.launch {
            try {
                if (uiState.isConnected) {
                    // Online: Upload to Firebase
                    val imageUrl = repository.uploadReceiptImage(imageUri)
                    uiState = uiState.copy(
                        isUploadingImage = false,
                        receiptImageUrl = imageUrl,
                        receiptImageUri = imageUri
                    )
                } else {
                    // Offline: Save locally
                    val localPath = imageCacheManager.saveImageLocally(getApplication(), imageUri)
                    uiState = uiState.copy(
                        isUploadingImage = false,
                        receiptImageUri = imageUri
                    )
                    expenseCache["receiptLocalPath"] = localPath
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isUploadingImage = false,
                    error = "Failed to process image: ${e.message}"
                )
            }
        }
    }

    fun removeReceiptImage() {
        expenseCache["receiptLocalPath"]?.let { path ->
            imageCacheManager.deleteImage(path as String)
        }
        uiState = uiState.copy(
            receiptImageUri = null,
            receiptImageUrl = null
        )
        expenseCache.remove("receiptLocalPath")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveExpense() {
        val amount = uiState.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            uiState = uiState.copy(error = "Please enter a valid amount")
            return
        }

        if (uiState.date == null) {
            uiState = uiState.copy(error = "Please select a date")
            return
        }

        uiState = uiState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val dateString = uiState.date!!.atStartOfDay()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                if (uiState.isConnected) {
                    // Online: Save directly to Firebase
                    repository.createExpense(
                        amount = amount,
                        currency = "USD",
                        categoryId = uiState.category.lowercase(),
                        description = if (uiState.description.isNotBlank()) uiState.description else null,
                        note = if (uiState.notes.isNotBlank()) uiState.notes else null,
                        date = dateString,
                        receiptImageUrl = uiState.receiptImageUrl
                    )

                    uiState = uiState.copy(
                        isLoading = false,
                        success = true,
                        successMessage = "Expense saved successfully!"
                    )
                } else {
                    // Offline: Save to local database
                    val pendingExpense = PendingExpense(
                        amount = amount,
                        currency = "USD",
                        categoryId = uiState.category.lowercase(),
                        description = if (uiState.description.isNotBlank()) uiState.description else null,
                        note = if (uiState.notes.isNotBlank()) uiState.notes else null,
                        date = dateString,
                        receiptImageLocalPath = expenseCache["receiptLocalPath"] as? String,
                        receiptImageUrl = uiState.receiptImageUrl
                    )

                    database.pendingExpenseDao().insert(pendingExpense)

                    uiState = uiState.copy(
                        isLoading = false,
                        success = true,
                        successMessage = "Expense saved offline. Will sync when online."
                    )
                }

                expenseCache.clear()
            } catch (e: Exception) {
                android.util.Log.e("TransactionError", "Error saving expense", e)
                e.printStackTrace()
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Error: ${e.message?: "Verifica el Logcat para el stack trace"}"
                )
            }
        }
    }
    fun clearError() {
        uiState = uiState.copy(error = null)
    }
}
