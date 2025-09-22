package com.example.monify_kotlin.feature.login.ui

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monify_kotlin.R
import com.example.monify_kotlin.feature.login.LoginViewModel
import com.example.monify_kotlin.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    vm: LoginViewModel = viewModel()
) {
    val state = vm.uiState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlue)               // ← fondo sky blue
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            // Logo centrado
            Image(
                painter = painterResource(id = R.drawable.monify_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(96.dp)
            )

            // Título "Monify" centrado, negro y bold (Nunito Bold desde el theme)
            Text(
                text = "Monify",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
            )

            // Card con el formulario
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Descripción en gris
                    Text(
                        text = "Gestiona tus finanzas de manera inteligente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray
                    )

                    // Email
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = vm::onEmailChange,
                        label = { Text(stringResource(R.string.email)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Password
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = vm::onPasswordChange,
                        label = { Text(stringResource(R.string.password)) },
                        singleLine = true,
                        visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = vm::togglePassword) {
                                Icon(
                                    imageVector = if (state.isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Error (mock)
                    state.error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Botón login: Light Blue
                    Button(
                        onClick = { vm.submit(onLoginSuccess) },
                        enabled = state.canSubmit,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LightBlue,          // ← botón Light Blue
                            disabledContainerColor = LightBlue.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("Iniciar sesión", color = MaterialTheme.colorScheme.onPrimary)
                    }

                    // Links: en negro
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { /* mock */ },
                            colors = ButtonDefaults.textButtonColors(contentColor = Black)
                        ) { Text("¿Olvidaste tu contraseña?") }

                        TextButton(
                            onClick = { /* mock */ },
                            colors = ButtonDefaults.textButtonColors(contentColor = Black)
                        ) { Text("¿No tienes cuenta? Regístrate") }
                    }
                }
            }
        }
    }
}

