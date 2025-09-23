package com.example.monify_kotlin.feature.login.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.monify_kotlin.R // Asegúrate de que R se importe correctamente
import com.example.monify_kotlin.ui.theme.SkyBlue // Asumo que tienes este color definido
import com.example.monify_kotlin.ui.theme.LightBlue // Asumo que tienes este color definido
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun MainLoginScreen(
    onLoginClicked: () -> Unit,
    onSignInClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlue)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.monify_logo),
                contentDescription = "Monify Logo",
                modifier = Modifier
                    .size(300.dp) // más grande que antes
                    .padding(bottom = 16.dp)
            )



            // Botón Log in
            Button(
                onClick = onLoginClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF0A4D8C)
                ),
                shape = RoundedCornerShape(50)
            ) {
                Text("Log in")
            }

            // Botón Sign up
            Button(
                onClick = onSignInClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF0A4D8C)
                ),
                shape = RoundedCornerShape(50)
            ) {
                Text("Sign up")
            }
        }
    }
}
