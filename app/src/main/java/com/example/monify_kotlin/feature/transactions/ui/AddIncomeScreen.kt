package com.example.monify_kotlin.feature.transactions.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monify_kotlin.feature.transactions.ui.AddIncomeViewModel
import com.example.monify_kotlin.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import androidx.compose.material.icons.filled.CloudOff

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeScreen(
    onBack: () -> Unit,
    viewModel: AddIncomeViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val dateFmt = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }
    var showDatePicker by remember { mutableStateOf(false) }
    val categories = listOf("Salary", "Freelance", "Investments", "Other")
    val snackbarHostState = remember { SnackbarHostState() }

    // Navegar de vuelta cuando se guarde exitosamente
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            uiState.successMessage?.let { message ->
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
            kotlinx.coroutines.delay(1000)
            onBack()
        }
    }

    // Auto-limpiar errores
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            snackbarHostState.showSnackbar(
                message = uiState.error!!,
                duration = SnackbarDuration.Short
            )
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = White,
        topBar = { Spacer(Modifier.height(0.dp)) },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (uiState.success) Green else Red,
                    contentColor = White
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header verde claro
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Green.copy(alpha = 0.15f))
                    .padding(16.dp)
            ) {
                Text(
                    "Add Income",
                    style = MaterialTheme.typography.titleLarge,
                    color = Black
                )
                Text(
                    "Register a new income",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black
                )
            }

            // ========== OFFLINE BANNER ========== (ADD IT HERE!)
            if (uiState.isOfflineMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = Color(0xFFFF9800)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "You're offline. Changes will be saved and synced when you're back online.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF856404)
                        )
                    }
                }
            }

            Card(shape = RoundedCornerShape(20.dp)) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Título de sección
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.TrendingUp,
                            contentDescription = null,
                            tint = Green
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Income Details",
                            style = MaterialTheme.typography.titleMedium,
                            color = Black
                        )
                    }

                    // Amount
                    OutlinedTextField(
                        value = uiState.amount,
                        onValueChange = { viewModel.updateAmount(it) },
                        label = { Text("Amount *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.error?.contains("amount", ignoreCase = true) == true,
                        prefix = { Text("$") }
                    )

                    // Description
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = { Text("Description *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., Monthly salary") }
                    )

                    // Category dropdown
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = uiState.category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category *") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        viewModel.updateCategory(opt)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Date picker
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                    ) {
                        OutlinedTextField(
                            value = uiState.date?.format(dateFmt) ?: "Select a date",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Date *") },
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Filled.CalendarToday, contentDescription = "Select date")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isError = uiState.error?.contains("date", ignoreCase = true) == true,
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    // Notes
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.updateNotes(it) },
                        label = { Text("Notes (optional)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Additional details...") }
                    )

                    // Save button
                    Button(
                        onClick = { viewModel.saveIncome() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Saving...")
                        } else {
                            Icon(Icons.Filled.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save Income")
                        }
                    }
                }
            }
        }

        // Date Picker Dialog
        if (showDatePicker) {
            val pickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val millis = pickerState.selectedDateMillis
                        if (millis != null) {
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.updateDate(date)
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(pickerState)
            }
        }
    }
}
