package com.example.monify_kotlin.feature.login.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.monify_kotlin.R

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB3E5FC)) // Azul claro del fondo
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo y texto
        Spacer(modifier = Modifier.height(40.dp))
        Image(
            painter = painterResource(id = R.drawable.monify_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(300.dp)
        )


        Spacer(modifier = Modifier.height(40.dp))

        // Username
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            placeholder = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Log in
        Button(
            onClick = { onLoginSuccess() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFCE7F3), // Rosado claro
                contentColor = Color(0xFF4A148C)    // Púrpura oscuro
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Log In")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Forgot password
        Text(
            text = "Forgot password?",
            color = Color(0xFF6A1B9A), // morado
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.clickable {
                // acción si quieres recuperar contraseña
            }
        )
    }
}


