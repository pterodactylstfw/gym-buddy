package com.corecoders.gymbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.corecoders.gymbuddy.data.Routine
import com.corecoders.gymbuddy.data.Workout
import com.corecoders.gymbuddy.viewmodel.ProfileViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onSignOutSuccess: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Workouts", "Routines", "Progress")
    val user = Firebase.auth.currentUser
    
    val workouts by viewModel.allWorkouts.collectAsState()
    val routines by viewModel.allRoutines.collectAsState()
    val totalVolume by viewModel.totalVolume.collectAsState()
    val totalWorkouts by viewModel.totalWorkouts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user?.displayName?.lowercase() ?: "profile", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground) },
                actions = {
                    IconButton(onClick = { /* TODO: Share */ }) {
                        Icon(Icons.Outlined.IosShare, contentDescription = "Share", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = { /* TODO: Edit */ }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = {
                        viewModel.signOut()
                        onSignOutSuccess()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // --- HEADER PROFIL ---
            ProfileHeader(user?.displayName ?: "User", totalWorkouts, routines.size)

            Spacer(modifier = Modifier.height(24.dp))

            // --- TAB-URI ---
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 2.dp
                        )
                    }
                },
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                        }
                    )
                }
            }

            // --- CONTINUT TAB-URI ---
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTabIndex) {
                    0 -> WorkoutsTabContent(workouts, navController)
                    1 -> RoutinesTabContent(routines, navController)
                    2 -> ProgressTabContent(totalVolume, totalWorkouts)
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(displayName: String, workoutCount: Int, routineCount: Int) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Poza de profil (Placeholder)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProfileStat(count = workoutCount.toString(), label = "Workouts")
                    ProfileStat(count = routineCount.toString(), label = "Routines")
                    ProfileStat(count = "0", label = "Followers")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Performance Athlete • gym-buddy",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 13.sp
        )
    }
}

@Composable
fun ProfileStat(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        Text(text = label, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
    }
}

@Composable
fun WorkoutsTabContent(workouts: List<Workout>, navController: NavController) {
    if (workouts.isEmpty()) {
        EmptyTabPlaceholder(Icons.Default.History, "No workouts logged yet.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(workouts.take(10)) { workout ->
                WorkoutHistoryItem(
                    workout = workout,
                    onClick = { navController.navigate("workout_details/${workout.id}") }
                )
            }
        }
    }
}

@Composable
fun RoutinesTabContent(routines: List<Routine>, navController: NavController) {
    if (routines.isEmpty()) {
        EmptyTabPlaceholder(Icons.Default.FitnessCenter, "No routines created yet.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(routines) { routine ->
                RoutineItem(
                    routine = routine,
                    onDelete = { /* Nu ștergem de aici pentru siguranță */ },
                    onClick = { navController.navigate("routine_details/${routine.id}") }
                )
            }
        }
    }
}

@Composable
fun ProgressTabContent(totalVolume: Double, totalWorkouts: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Lifetime Stats", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("TOTAL VOLUME", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${"%,.0f".format(totalVolume)} kg",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text("lifted across all time", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("CONSISTENCY", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$totalWorkouts sessions",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text("logged in GymBuddy", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun EmptyTabPlaceholder(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}
