package com.corecoders.gymbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.corecoders.gymbuddy.data.AppDatabase
import com.corecoders.gymbuddy.screens.DashboardScreen
import com.corecoders.gymbuddy.screens.LoginScreen
import com.corecoders.gymbuddy.screens.RegisterScreen
import com.corecoders.gymbuddy.ui.theme.GymBuddyTheme
import com.corecoders.gymbuddy.viewmodel.WorkoutViewModel
import com.corecoders.gymbuddy.viewmodel.WorkoutViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GymBuddyTheme {
                val navController = rememberNavController()
                val auth = Firebase.auth

                val database: AppDatabase = AppDatabase.getDatabase(applicationContext)
                val workoutViewModel: WorkoutViewModel = viewModel(
                    factory = WorkoutViewModelFactory(database.workoutDao())
                )


                val startDestination = if (auth.currentUser != null) "dashboard" else "login"

                NavHost(
                    navController = navController,
                    startDestination = startDestination // Numele rutei de pornire
                ) {
                    // Definim ruta pentru Login
                    composable("login") {
                        LoginScreen(navController = navController)
                    }

                    // Definim ruta pentru Register
                    composable("register") {
                        RegisterScreen(navController = navController)
                    }
                    composable("dashboard") {
                        DashboardScreen(navController, workoutViewModel)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    GymBuddyTheme {
        val navController = rememberNavController()

        // Îl pasăm funcției tale
        LoginScreen(navController = navController)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GymBuddyTheme {
        Greeting("Android")
    }
}