package com.corecoders.gymbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import kotlinx.coroutines.launch
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
            val isSystemDark = isSystemInDarkTheme()
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val activeTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemDark
            }

            GymBuddyTheme(darkTheme = activeTheme) {
                val onboardingCompleted by userPreferences.onboardingCompletedFlow.collectAsState(initial = null)
                val auth = Firebase.auth
                if (themeMode == null || (auth.currentUser != null && onboardingCompleted == null)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    )
                } else {
                    val navController = rememberNavController()
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
                    factory = ProfileViewModelFactory(database, database.workoutDao(), database.routineDao(), userPreferences)
                )
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModelFactory(database.workoutDao())
                )
                val routinesViewModel: RoutinesViewModel = viewModel(
                    factory = RoutinesViewModelFactory(database.routineDao(), database.exerciseDao())
                )
                val onboardingViewModel: OnboardingViewModel = viewModel(
                    factory = OnboardingViewModelFactory(userPreferences)
                )
                val socialViewModel: SocialViewModel = viewModel()

                val onboardingCompleted by userPreferences.onboardingCompletedFlow.collectAsState(initial = null)

                LaunchedEffect(auth.currentUser) {
                    val user = auth.currentUser
                    if (user != null) {
                        val repository = com.corecoders.gymbuddy.data.SocialRepository()
                        val profile = repository.getUserProfile(user.uid)
                        if (profile != null) {
                            userPreferences.updateProfileData(
                                age = profile.age,
                                weight = profile.weight,
                                height = profile.height,
                                targetWeight = profile.targetWeight,
                                trainingFrequency = profile.trainingFrequency,
                                fitnessGoal = profile.fitnessGoal,
                                experienceLevel = profile.experienceLevel,
                                gender = profile.gender
                            )
                            userPreferences.updateBodyComposition(
                                bodyFat = profile.bodyFat,
                                muscleMass = profile.muscleMass,
                                waistSize = profile.waistSize
                            )
                            userPreferences.updateProfilePictureUri(profile.avatarUri)
                            userPreferences.updateOnboardingCompleted(profile.onboardingCompleted)
                        } else {
                            userPreferences.updateOnboardingCompleted(false)
                        }
                    }
                }

                LaunchedEffect(auth.currentUser, onboardingCompleted) {
                    if (auth.currentUser != null && onboardingCompleted == false) {
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute != "onboarding" && currentRoute != "login" && currentRoute != "register") {
                            navController.navigate("onboarding") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }

                val startDestination = when {
                    auth.currentUser == null -> "login"
                    onboardingCompleted == false -> "onboarding"
                    else -> BottomNavItem.Dashboard.route
                }

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
                            LoginScreen(
                                navController = navController,
                                onLoginSuccess = {
                                    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
                                    scope.launch {
                                        val user = auth.currentUser
                                        if (user != null) {
                                            val repository = com.corecoders.gymbuddy.data.SocialRepository()
                                            val profile = repository.getUserProfile(user.uid)
                                            if (profile != null) {
                                                userPreferences.updateProfileData(
                                                    age = profile.age,
                                                    weight = profile.weight,
                                                    height = profile.height,
                                                    targetWeight = profile.targetWeight,
                                                    trainingFrequency = profile.trainingFrequency,
                                                    fitnessGoal = profile.fitnessGoal,
                                                    experienceLevel = profile.experienceLevel,
                                                    gender = profile.gender
                                                )
                                                userPreferences.updateBodyComposition(
                                                    bodyFat = profile.bodyFat,
                                                    muscleMass = profile.muscleMass,
                                                    waistSize = profile.waistSize
                                                )
                                                userPreferences.updateProfilePictureUri(profile.avatarUri)
                                                userPreferences.updateOnboardingCompleted(profile.onboardingCompleted)
                                                
                                                if (profile.onboardingCompleted) {
                                                    navController.navigate(BottomNavItem.Dashboard.route) {
                                                        popUpTo("login") { inclusive = true }
                                                    }
                                                } else {
                                                    navController.navigate("onboarding") {
                                                        popUpTo("login") { inclusive = true }
                                                    }
                                                }
                                            } else {
                                                userPreferences.updateOnboardingCompleted(false)
                                                navController.navigate("onboarding") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                        } else {
                                            navController.navigate(BottomNavItem.Dashboard.route) {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        composable("register") {
                            RegisterScreen(
                                navController = navController,
                                onRegisterSuccess = {
                                    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
                                    scope.launch {
                                        userPreferences.clearProfileData()
                                        userPreferences.updateOnboardingCompleted(false)
                                        navController.navigate("onboarding") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable("onboarding") {
                            OnboardingScreen(
                                navController = navController,
                                viewModel = onboardingViewModel,
                                onFinish = {
                                    navController.navigate(BottomNavItem.Dashboard.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
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
                                onExerciseSelected = null,
                                onBack = null
                            )
                        }

                        composable("workout_exercise_selection") {
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
                            StatsScreen(navController, workoutViewModel, profileViewModel)
                        }

                        composable(BottomNavItem.Social.route) {
                            SocialScreen(navController = navController, viewModel = socialViewModel)
                        }

                        composable("find_friends") {
                            FindFriendsScreen(navController = navController, viewModel = socialViewModel)
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
                                onAddExerciseClick = { navController.navigate("workout_exercise_selection") },
                                onFinishClick = { workoutId ->
                                    activeWorkoutViewModel.resetWorkout()
                                    if (workoutId != null) {
                                        navController.navigate("workout_summary/$workoutId") {
                                            popUpTo(BottomNavItem.Dashboard.route) { inclusive = false }
                                        }
                                    } else {
                                        navController.navigate(BottomNavItem.Dashboard.route) {
                                            popUpTo(BottomNavItem.Dashboard.route) { inclusive = true }
                                        }
                                    }
                                },
                                onCancelClick = {
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

                        composable("other_user_profile/{userId}") { backStackEntry ->
                            val targetUserId = backStackEntry.arguments?.getString("userId") ?: ""
                            OtherUserProfileScreen(navController = navController, userId = targetUserId)
                        }

                        composable("settings") {
                            SettingsScreen(
                                navController = navController,
                                viewModel = settingsViewModel,
                                profileViewModel = profileViewModel
                            )
                        }

                        composable("edit_profile") {
                            EditProfileScreen(
                                navController = navController,
                                viewModel = profileViewModel
                            )
                        }

                        composable("workout_summary/{workoutId}") { backStackEntry ->
                            val workoutId = backStackEntry.arguments?.getString("workoutId")?.toIntOrNull() ?: 0
                            WorkoutSummaryScreen(
                                workoutId = workoutId,
                                database = database,
                                navController = navController
                            )
                        }
                    }
                }
                }
            }
        }
    }
}
