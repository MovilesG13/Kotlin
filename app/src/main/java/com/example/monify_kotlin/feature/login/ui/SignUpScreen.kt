package com.example.monify_kotlin.feature.login.ui
import androidx.compose.ui.text.input.PasswordVisualTransformation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.res.painterResource
import com.example.monify_kotlin.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.example.monify_kotlin.core.navigation.Routes
import androidx.compose.foundation.shape.RoundedCornerShape
@Composable
fun SignUpScreen(
    navController: NavController,
    onSignUpSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB3E5FC)) // fondo azul claro
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.monify_logo),
                contentDescription = "Monify Logo",
                modifier = Modifier.size(300.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Título
            Text(
                text = "Create Account",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Sign up
            Button(
                onClick = { onSignUpSuccess() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF3E5F5),
                    contentColor = Color(0xFF6A1B9A)
                )
            ) {
                Text("Sign up")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Link para Log in
            TextButton(
                onClick = { navController.navigate(Routes.LOGIN) },
            ) {
                Text(
                    text = "Already have an account? Log in",
                    color = Color(0xFF6A1B9A),
                    fontSize = 14.sp
                )
            }
        }
    }
}