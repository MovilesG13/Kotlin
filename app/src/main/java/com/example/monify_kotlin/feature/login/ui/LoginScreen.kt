package com.example.monify_kotlin.feature.login.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monify_kotlin.feature.login.LoginViewModel
import com.example.monify_kotlin.ui.theme.Blue
import com.example.monify_kotlin.ui.theme.LightBlue
import com.example.monify_kotlin.ui.theme.SkyBlue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction

import com.example.monify_kotlin.R                  // ← R de tu módulo




@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    vm: LoginViewModel = viewModel()
) {
    val state = vm.uiState

    // Contenedor con fondo claro y tarjeta al centro
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Branding
                Text(
                    text = "FinanceApp",
                    style = MaterialTheme.typography.titleLarge,
                    color = Blue
                )
                Text(
                    text = "Gestiona tus finanzas de manera inteligente",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Email
                OutlinedTextField(
                    value = vm.uiState.email,
                    onValueChange = vm::onEmailChange,
                    label = { Text(stringResource(R.string.email)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    // keyboardActions = KeyboardActions(onNext = { /* mover foco si quieres */ }),
                    modifier = Modifier.fillMaxWidth()
                )


                // Password
                OutlinedTextField(
                    value = vm.uiState.password,
                    onValueChange = vm::onPasswordChange,
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = if (vm.uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = vm::togglePassword) {
                            Icon(
                                imageVector = if (vm.uiState.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Error (mock)
                vm.uiState.error?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }

                // Botón login (usa LightBlue como primario y SkyBlue para estados/hover)
                Button(
                    onClick = { vm.submit(onLoginSuccess) },
                    enabled = vm.uiState.canSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LightBlue, disabledContainerColor = SkyBlue)
                ) {
                    Text("Iniciar sesión", color = MaterialTheme.colorScheme.onPrimary)
                }

                // Links mock
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { /* mock */ }) { Text("¿Olvidaste tu contraseña?") }
                    TextButton(onClick = { /* mock */ }) { Text("¿No tienes cuenta? Regístrate") }
                }
            }
        }
    }
}

