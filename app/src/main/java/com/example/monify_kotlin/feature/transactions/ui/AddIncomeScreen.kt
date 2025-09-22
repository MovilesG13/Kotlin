package com.example.monify_kotlin.feature.transactions.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.monify_kotlin.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeScreen(onBack: () -> Unit) {

    // ----- estado simple (mock) -----
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val categories = listOf("Salary", "Freelance", "Investments", "Other")
    var category by remember { mutableStateOf(categories.first()) }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf<LocalDate?>(null) }

    val dateFmt = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }

    Scaffold(
        containerColor = White,
        topBar = { Spacer(Modifier.height(0.dp)) } // sin título arriba
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                Text("Add Income", style = MaterialTheme.typography.titleLarge, color = Black)
                Text("Register a new income", style = MaterialTheme.typography.bodyMedium, color = Black)
            }

            // Card con el formulario
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

                    // Título de sección
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.TrendingUp, contentDescription = null, tint = Green)
                        Spacer(Modifier.width(8.dp))
                        Text("Income Details", style = MaterialTheme.typography.titleMedium, color = Black)
                    }

                    // Amount
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category (dropdown simple con Exposed)
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            categories.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = { category = opt; expanded = false }
                                )
                            }
                        }
                    }

                    // Date
                    OutlinedTextField(
                        value = date?.format(dateFmt) ?: "Select a date",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date *") },
                        trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                    )

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Save button (LightBlue)
                    Button(
                        onClick = onBack, // mock: regresa
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightBlue)
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save Income")
                    }
                }
            }
        }

        if (showDatePicker) {
            val pickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val millis = pickerState.selectedDateMillis
                        if (millis != null) {
                            date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
            ) { DatePicker(pickerState) }
        }
    }
}
