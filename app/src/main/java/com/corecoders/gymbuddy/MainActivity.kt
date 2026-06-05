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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.corecoders.gymbuddy.data.AppDatabase
import com.corecoders.gymbuddy.data.UserPreferences
import com.corecoders.gymbuddy.navigation.BottomNavItem
import com.corecoders.gymbuddy.navigation.BottomNavigationBar
import com.corecoders.gymbuddy.screens.*
import com.corecoders.gymbuddy.ui.theme.GymBuddyTheme
import com.corecoders.gymbuddy.viewmodel.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userPreferences = remember { UserPreferences(applicationContext) }
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(userPreferences)
            )
            val darkMode by settingsViewModel.darkMode.collectAsState()

            GymBuddyTheme(darkTheme = darkMode) {
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
                    factory = ProfileViewModelFactory(database.workoutDao(), database.routineDao())
                )
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModelFactory(database.workoutDao())
                )
                val routinesViewModel: RoutinesViewModel = viewModel(
                    factory = RoutinesViewModelFactory(database.routineDao(), database.exerciseDao())
                )

                val startDestination = if (auth.currentUser != null) BottomNavItem.Dashboard.route else "login"

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = currentRoute in listOf(
                    BottomNavItem.Dashboard.route,
                    BottomNavItem.Catalog.route,
                    BottomNavItem.Profile.route,
                    BottomNavItem.Stats.route,
                    BottomNavItem.Social.route
                )

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
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
                                workoutViewModel = workoutViewModel,
                                routinesViewModel = routinesViewModel,
                                onStartWorkout = {
                                    activeWorkoutViewModel.resetWorkout()
                                    navController.navigate("active_workout")
                                },
                                onStartRoutine = { routine, exercises ->
                                    activeWorkoutViewModel.startWorkoutFromRoutine(routine, exercises)
                                    navController.navigate("active_workout")
                                }
                            )
                        }

                        composable(BottomNavItem.Catalog.route) {
                            ExerciseCatalogScreen(
                                viewModel = exerciseViewModel,
                                onExerciseSelected = { exercise ->
                                    activeWorkoutViewModel.addExercise(exercise)
                                    navController.popBackStack()
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("catalog_selection/{routineId}") { backStackEntry ->
                            val routineId = backStackEntry.arguments?.getString("routineId")?.toIntOrNull() ?: 0
                            ExerciseCatalogScreen(
                                viewModel = exerciseViewModel,
                                onExerciseSelected = { exercise ->
                                    routinesViewModel.addExerciseToRoutine(routineId, exercise)
                                    navController.popBackStack()
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("profile") {
                            ProfileScreen(
                                navController = navController,
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

                        composable(BottomNavItem.Social.route) {
                            SocialScreen(navController = navController)
                        }

                        composable("history") {
                            HistoryScreen(navController = navController, viewModel = historyViewModel)
                        }

                        composable("routines") {
                            RoutinesScreen(navController = navController, viewModel = routinesViewModel)
                        }

                        composable("routine_details/{routineId}") { backStackEntry ->
                            val routineId = backStackEntry.arguments?.getString("routineId")?.toIntOrNull() ?: 0
                            RoutineDetailScreen(
                                routineId = routineId,
                                viewModel = routinesViewModel,
                                onAddExercise = { navController.navigate("catalog_selection/$routineId") },
                                onBack = { navController.popBackStack() }
                            )
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
                            val workoutId = backStackEntry.arguments?.getString("workoutId")?.toIntOrNull() ?: 0
                            WorkoutDetailScreen(workoutId = workoutId, database = database, onBack = { navController.popBackStack() })
                        }

                        composable("settings") {
                            SettingsScreen(navController = navController, viewModel = settingsViewModel)
                        }
                    }
                }
            }
        }
    }
}
