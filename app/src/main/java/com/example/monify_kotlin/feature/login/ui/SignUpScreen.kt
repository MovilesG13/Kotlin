package com.example.monify_kotlin.feature.login.ui

import android.app.Application // <-- (1) Importar
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext // <-- (2) Importar
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider // <-- (3) Importar
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.monify_kotlin.R
import com.example.monify_kotlin.core.navigation.Routes
import com.example.monify_kotlin.feature.login.LoginViewModel
import com.example.monify_kotlin.ui.theme.Blue

@Composable
fun SignUpScreen(
    navController: NavController,
    onSignUpSuccess: () -> Unit
) {
    // --- (4) INICIO DE LA CORRECCIÓN ---
    // Necesitamos el contexto para crear la "fábrica" de AndroidViewModel
    val context = LocalContext.current
    val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
        context.applicationContext as Application
    )
    // Ahora creamos el ViewModel usando esa fábrica
    val vm: LoginViewModel = viewModel(factory = factory)
    // --- FIN DE LA CORRECCIÓN ---

    val ui by vm.state.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(ui.loggedIn) {
        if (ui.loggedIn) onSignUpSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFC8E0E4))
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Connectivity Banner
            if (!ui.isConnected) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "⚠️ Offline - Please connect to sign up",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFFC62828),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
            Image(
                painter = painterResource(id = R.drawable.monify_logo),
                contentDescription = "Monify Logo",
                modifier = Modifier.size(300.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Create Account",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Blue,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                enabled = ui.isConnected
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                enabled = ui.isConnected
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                enabled = ui.isConnected
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    // (5) CAMBIO AQUÍ: Ahora también pasamos el nombre
                    if (name.isNotBlank() && email.isNotBlank() && password.length >= 6) {
                        vm.signUp(name.trim(), email.trim(), password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue,
                    contentColor = Color.White
                ),
                enabled = ui.isConnected && !ui.loading
            ) {
                Text(if (ui.loading) "Creando..." else "Sign up")
            }

            Spacer(Modifier.height(8.dp))
            if (ui.error != null) {
                Text(ui.error!!, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { navController.navigate(Routes.LOGIN) }) {
                Text(
                    text = "Already have an account? Log in",
                    color = Color(0xFF6A1B9A),
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
