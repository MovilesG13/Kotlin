package com.example.monify_kotlin.feature.login.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monify_kotlin.R
import com.example.monify_kotlin.feature.login.LoginViewModel
import com.example.monify_kotlin.ui.theme.Blue

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val vm: LoginViewModel = viewModel()
    val ui by vm.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(ui.loggedIn) {
        if (ui.loggedIn) onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFC8E0E4))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Connectivity Banner
        if (!ui.isConnected) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "⚠️ Offline - Please connect to log in",
                    modifier = Modifier.padding(12.dp),
                    color = Color(0xFFC62828),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(40.dp))
        Image(
            painter = painterResource(id = R.drawable.monify_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(300.dp)
        )
        Spacer(Modifier.height(40.dp))

        // "Login" Title
        Text(
            text = "Login",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0048C4),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Email") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            enabled = ui.isConnected
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            enabled = ui.isConnected
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.length >= 6) {
                    vm.signIn(email.trim(), password)
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = ui.isConnected && !ui.loading
        ) {
            Text(if (ui.loading) "Ingresando..." else "Log In")
        }

        Spacer(Modifier.height(8.dp))
        if (ui.error != null) {
            Text(ui.error!!, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = "Forgot password?",
            color = Color(0xFF6A1B9A),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.clickable { /* TODO */ }
        )

        Spacer(Modifier.height(40.dp))
    }
}