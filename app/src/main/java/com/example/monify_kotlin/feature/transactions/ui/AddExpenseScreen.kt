package com.example.monify_kotlin.feature.transactions.ui

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.monify_kotlin.feature.transactions.ui.AddExpenseViewModel
import com.example.monify_kotlin.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddExpenseScreen(
    onBack: () -> Unit,
    viewModel: AddExpenseViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val dateFmt = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }

    // Estados locales
    var showDatePicker by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val categories = listOf("Food", "Transport", "Bills", "Shopping", "Other")
    val snackbarHostState = remember { SnackbarHostState() }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Función para crear URI temporal
    fun createTempImageUri(): Uri {
        val tempFile = File.createTempFile(
            "receipt_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        ).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    // Launcher para tomar foto con cámara
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            viewModel.uploadReceiptImage(tempPhotoUri!!)
        }
    }

    // Launcher para seleccionar de galería
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadReceiptImage(it) }
    }

    // Efecto: Navegar al guardar exitosamente
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

    // Efecto: Mostrar errores
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ========== HEADER ==========
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Red.copy(alpha = 0.12f))
                    .padding(16.dp)
            ) {
                Text(
                    "Add Expense",
                    style = MaterialTheme.typography.titleLarge,
                    color = Black
                )
                Text(
                    "Register a new expense",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black
                )
            }

            // ========== FORMULARIO ==========
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Título de sección
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.TrendingDown, contentDescription = null, tint = Red)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Expense Details",
                            style = MaterialTheme.typography.titleMedium,
                            color = Black
                        )
                    }

                    // Campo: Amount
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

                    // Campo: Description
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = { Text("Description") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., Lunch at restaurant") }
                    )

                    // Campo: Category (Dropdown)
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
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
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

                    // Campo: Date (Clickeable)
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

                    // Campo: Notes
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.updateNotes(it) },
                        label = { Text("Notes (optional)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Additional details...") }
                    )

                    // ========== SECCIÓN DE FOTO DE RECIBO ==========
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Header con loading indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Receipt Photo (optional)",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Black
                            )
                            if (uiState.isUploadingImage) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Red
                                )
                            }
                        }

                        // Mostrar imagen o botón para agregar
                        if (uiState.receiptImageUri != null) {
                            // Preview de la imagen capturada
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Gray, RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model = uiState.receiptImageUri,
                                    contentDescription = "Receipt",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Botón para eliminar imagen
                                IconButton(
                                    onClick = { viewModel.removeReceiptImage() },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(Red, CircleShape)
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Remove",
                                        tint = White
                                    )
                                }
                            }
                        } else {
                            // Botón para agregar foto
                            OutlinedButton(
                                onClick = { showImageSourceDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Red)
                            ) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Add Receipt Photo")
                            }
                        }
                    }

                    // ========== BOTÓN GUARDAR ==========
                    Button(
                        onClick = { viewModel.saveExpense() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Red),
                        enabled = !uiState.isLoading && !uiState.isUploadingImage
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
                            Text("Save Expense")
                        }
                    }
                }
            }
        }

        // ========== DIÁLOGOS ==========

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

        // Image Source Dialog (Camera o Gallery)
        if (showImageSourceDialog) {
            AlertDialog(
                onDismissRequest = { showImageSourceDialog = false },
                title = { Text("Add Receipt Photo") },
                text = { Text("Choose how to add your receipt photo") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            if (cameraPermissionState.status.isGranted) {
                                tempPhotoUri = createTempImageUri()
                                takePictureLauncher.launch(tempPhotoUri!!)
                            } else {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        }
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Camera")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            pickImageLauncher.launch("image/*")
                        }
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Gallery")
                    }
                }
            )
        }
    }
}
