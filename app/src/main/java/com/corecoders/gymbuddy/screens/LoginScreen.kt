package com.corecoders.gymbuddy.screens

import android.app.Activity
import androidx.credentials.CredentialManager
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.navigation.NavController
import com.corecoders.gymbuddy.R // Asigură-te că pachetul tău e corect aici
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider // NOU: Import pentru GitHub
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.res.stringResource

@Composable
fun SocialLoginButton(
    text: String,
    iconResId: Int,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        border = BorderStroke(1.dp, Color(0xFFE5E5EA))
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = "$text Logo",
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LoginScreen(navController: NavController) {
    val auth: FirebaseAuth = Firebase.auth
    val context = LocalContext.current
    val activity = context as? Activity // Firebase are nevoie de Activity pentru fereastra web

    val scope = rememberCoroutineScope()

    val credentialManager = CredentialManager.create(context)
    val webClientId = stringResource(id = R.string.default_web_client_id)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // 1. Logica pentru Email/Parolă
    val onLogin = {
        if (email.isNotEmpty() && password.isNotEmpty() && !isLoading) {
            isLoading = true
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        Toast.makeText(context, task.exception?.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    // 2. NOU: Logica pentru GitHub
    val onGitHubLogin: () -> Unit = {
        if (activity != null && !isLoading) {
            isLoading = true
            // Creăm furnizorul de autentificare pentru GitHub
            val provider = OAuthProvider.newBuilder("github.com")

            // Cerem acces la adresa de email a utilizatorului
            provider.scopes = listOf("user:email")

            // Lansăm fereastra de logare
            auth.startActivityForSignInWithProvider(activity, provider.build())
                .addOnSuccessListener { authResult ->
                    isLoading = false
                    // Opțional: Poți lua numele userului cu authResult.user?.displayName
                    Toast.makeText(context, "GitHub Login Success!", Toast.LENGTH_SHORT).show()
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
                .addOnFailureListener { e ->
                    isLoading = false
                    Toast.makeText(context, "GitHub error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(context, "Eroare internă. Incearcă din nou.", Toast.LENGTH_SHORT).show()
        }
    }

    val onGoogleLogin = {
        scope.launch {
            try {
                // 1. Configurăm cererea pentru Google
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // 2. Afișăm fereastra de selecție a contului
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                    // 3. Autentificăm în Firebase
                    auth.signInWithCredential(firebaseCredential).await()

                    Toast.makeText(context, "Google Login Success!", Toast.LENGTH_SHORT).show()
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Google Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Log in to GymBuddy", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onLogin() })
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            onClick = onLogin
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Login", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Linia despărțitoare "OR"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
            Text(text = "  OR  ", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // NOU: Butonul de GitHub
        SocialLoginButton(
            text = "Continue with GitHub",
            iconResId = R.drawable.ic_github, // Vezi Pasul 2 mai jos
            onClick = onGitHubLogin
        )
        Spacer(modifier = Modifier.height(12.dp))
        SocialLoginButton(
            text = "Continue with Google",
            iconResId = R.drawable.ic_google, // Asigură-te că ai și acest logo în drawable!
            onClick = { onGoogleLogin() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("register") }) {
            Text("Don't have an account? Register", color = MaterialTheme.colorScheme.primary)
        }
    }
}