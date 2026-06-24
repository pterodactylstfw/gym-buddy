package com.corecoders.gymbuddy.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.corecoders.gymbuddy.data.AppDatabase
import com.corecoders.gymbuddy.data.SocialRepository
import com.corecoders.gymbuddy.data.Workout
import com.corecoders.gymbuddy.data.WorkoutSet
import com.corecoders.gymbuddy.data.UserPreferences
import com.corecoders.gymbuddy.navigation.BottomNavItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSummaryScreen(
    workoutId: Int,
    database: AppDatabase,
    navController: NavController
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context.applicationContext) }
    val isMetric by userPreferences.unitSystemMetricFlow.collectAsState(initial = true)

    val coroutineScope = rememberCoroutineScope()
    val socialRepository = remember { SocialRepository() }
    
    var workout by remember { mutableStateOf<Workout?>(null) }
    var sets by remember { mutableStateOf<List<WorkoutSet>>(emptyList()) }
    var exerciseNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var isPublishing by remember { mutableStateOf(false) }

    LaunchedEffect(workoutId) {
        val w = database.workoutDao().getWorkoutById(workoutId)
        workout = w
        if (w != null) {
            val s = database.workoutDao().getSetsForWorkoutSync(workoutId)
            sets = s
            
            // Get unique exercise names
            val exerciseIds = s.map { it.exerciseId }.distinct()
            val names = mutableListOf<String>()
            for (id in exerciseIds) {
                val name = database.exerciseDao().getExerciseNameById(id)
                if (name != null) names.add(name)
            }
            exerciseNames = names
        }
    }

    val totalKg = sets.filter { it.isCompleted }.sumOf { it.weight * it.reps }
    val duration = workout?.durationMinutes ?: 0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Workout Complete", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    TextButton(onClick = {
                        navController.navigate(BottomNavItem.Dashboard.route) {
                            popUpTo(BottomNavItem.Dashboard.route) { inclusive = true }
                        }
                    }) {
                        Text("Done", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        if (workout == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Congratulations!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Text(
                    text = "You just crushed your ${workout?.name}!",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val volumeValue = if (isMetric) {
                            "${String.format("%.1f", totalKg / 1000.0)}t"
                        } else {
                            val totalLbs = totalKg * 2.20462
                            if (totalLbs >= 1000.0) {
                                "${String.format("%.1f", totalLbs / 1000.0)}k lb"
                            } else {
                                "${totalLbs.toInt()} lb"
                            }
                        }
                        StatItem(label = "Volume", value = volumeValue)
                        StatItem(label = "Duration", value = "${duration}m")
                        StatItem(label = "Sets", value = "${sets.size}")
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    enabled = !isPublishing,
                    onClick = {
                        isPublishing = true
                        coroutineScope.launch {
                            val volumeStr = if (isMetric) {
                                "${String.format("%.1f", totalKg / 1000.0)}t"
                            } else {
                                val totalLbs = totalKg * 2.20462
                                if (totalLbs >= 1000.0) {
                                    "${String.format("%.1f", totalLbs / 1000.0)}k lb"
                                } else {
                                    "${totalLbs.toInt()} lb"
                                }
                            }
                            val statsStr = "$volumeStr volume • ${duration} min"
                            val success = socialRepository.publishPost(
                                workoutName = workout?.name ?: "Workout",
                                stats = statsStr,
                                exercises = exerciseNames
                            )
                            isPublishing = false
                            if (success) {
                                Toast.makeText(context, "Shared to Feed!", Toast.LENGTH_SHORT).show()
                                navController.navigate(BottomNavItem.Social.route) {
                                    popUpTo(BottomNavItem.Dashboard.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } else {
                                Toast.makeText(context, "Failed to share.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isPublishing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SHARE TO FEED", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = {
                        navController.navigate(BottomNavItem.Dashboard.route) {
                            popUpTo(BottomNavItem.Dashboard.route) { inclusive = true }
                        }
                    }
                ) {
                    Text("Skip", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Black, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
    }
}
