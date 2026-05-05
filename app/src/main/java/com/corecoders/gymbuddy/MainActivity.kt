package com.corecoders.gymbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.corecoders.gymbuddy.data.AppDatabase
import com.corecoders.gymbuddy.navigation.BottomNavItem
import com.corecoders.gymbuddy.navigation.BottomNavigationBar
import com.corecoders.gymbuddy.screens.ActiveWorkoutScreen
import com.corecoders.gymbuddy.screens.DashboardScreen
import com.corecoders.gymbuddy.screens.ExerciseCatalogScreen
import com.corecoders.gymbuddy.screens.LoginScreen
import com.corecoders.gymbuddy.screens.ProfileScreen
import com.corecoders.gymbuddy.screens.RegisterScreen
import com.corecoders.gymbuddy.screens.StatsScreen
import com.corecoders.gymbuddy.screens.WorkoutDetailScreen
import com.corecoders.gymbuddy.ui.theme.GymBuddyTheme
import com.corecoders.gymbuddy.viewmodel.ActiveWorkoutViewModel
import com.corecoders.gymbuddy.viewmodel.ActiveWorkoutViewModelFactory
import com.corecoders.gymbuddy.viewmodel.ExerciseViewModel
import com.corecoders.gymbuddy.viewmodel.ExerciseViewModelFactory
import com.corecoders.gymbuddy.viewmodel.ProfileViewModel
import com.corecoders.gymbuddy.viewmodel.ProfileViewModelFactory
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

                // ViewModels
                val workoutViewModel: WorkoutViewModel = viewModel(
                    factory = WorkoutViewModelFactory(database.workoutDao())
                )
                val exerciseViewModel: ExerciseViewModel = viewModel(
                    factory = ExerciseViewModelFactory(database.exerciseDao())
                )
                val activeWorkoutViewModel: ActiveWorkoutViewModel = viewModel(
                    factory = ActiveWorkoutViewModelFactory(database.workoutDao())
                )
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModelFactory(database.workoutDao())
                )

                val startDestination = if (auth.currentUser != null) BottomNavItem.Dashboard.route else "login"

                // Aflăm ruta curentă pentru a ști dacă arătăm bara de jos[cite: 5]
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Rutele unde bara de navigare este vizibilă[cite: 5]
                val showBottomBar = currentRoute in listOf(
                    BottomNavItem.Dashboard.route,
                    BottomNavItem.Catalog.route,
                    BottomNavItem.Profile.route,
                    BottomNavItem.Stats.route,
                    BottomNavItem.Store.route
                )

                // Învelim NavHost-ul în Scaffold pentru bara de navigare[cite: 5]
                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    // Aplicăm padding-ul barei de navigare la NavHost[cite: 5]
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(navController = navController)
                        }

                        composable("register") {
                            RegisterScreen(navController = navController)
                        }

                        composable(BottomNavItem.Dashboard.route) {
                            DashboardScreen(
                                navController = navController,
                                viewModel = workoutViewModel,
                                onStartWorkout = {
                                    activeWorkoutViewModel.resetWorkout()
                                    navController.navigate("active_workout")
                                }
                            )
                        }

                        composable(BottomNavItem.Catalog.route) {
                            ExerciseCatalogScreen(
                                viewModel = exerciseViewModel,
                                onExerciseSelected = { exercise ->
                                    // Comportamentul la click depinde de unde am venit.
                                    // Momentan îl lăsăm să adauge în antrenament și să dea pop.
                                    activeWorkoutViewModel.addExercise(exercise)
                                    navController.popBackStack()
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(BottomNavItem.Profile.route) {
                            ProfileScreen(
                                viewModel = profileViewModel,
                                onBack = { navController.popBackStack() },
                                onSignOutSuccess = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(BottomNavItem.Stats.route) {
                            StatsScreen(navController, workoutViewModel)
                        }

                        composable(BottomNavItem.Store.route) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Store Screen") }
                        }

                        composable("active_workout") {
                            ActiveWorkoutScreen(
                                viewModel = activeWorkoutViewModel,
                                onAddExerciseClick = { navController.navigate(BottomNavItem.Catalog.route) },
                                onFinishClick = {
                                    activeWorkoutViewModel.resetWorkout()
                                    navController.navigate(BottomNavItem.Dashboard.route) {
                                        popUpTo(BottomNavItem.Dashboard.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("workout_details/{workoutId}") { backStackEntry ->
                            val workoutId = backStackEntry.arguments?.getString("workoutId")?.toInt() ?: 0
                            WorkoutDetailScreen(workoutId = workoutId, database = database, onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}