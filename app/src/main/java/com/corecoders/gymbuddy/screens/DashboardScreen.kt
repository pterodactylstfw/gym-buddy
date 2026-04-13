package com.corecoders.gymbuddy.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun DashboardScreen(navController: NavController) {
    val auth = Firebase.auth
    val userEmail = auth.currentUser?.email ?: "User"

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Dashboard", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Welcome back, $userEmail!")

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            auth.signOut()
            navController.navigate("login") {
                popUpTo("dashboard") {inclusive = true}
            }
        }) {
            Text("Sign Out")
        }
    }
}