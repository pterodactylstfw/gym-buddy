package com.corecoders.gymbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corecoders.gymbuddy.data.AppDatabase
import com.corecoders.gymbuddy.data.UserPreferences
import androidx.compose.ui.platform.LocalContext
import com.corecoders.gymbuddy.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(workoutId: Int, database: AppDatabase, onBack: () -> Unit) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context.applicationContext) }
    val isMetric by userPreferences.unitSystemMetricFlow.collectAsState(initial = true)

    val sets by database.workoutDao().getSetsforWorkout(workoutId).collectAsState(initial = emptyList())
    val groupedSets = sets.groupBy { it.exerciseId }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Workout Summary", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (sets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Loading or no data...", color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Convertim mapul in lista de perechi
                val groupedList = groupedSets.toList()
                
                items(groupedList.size) { index ->
                    val (exerciseId, exerciseSets) = groupedList[index]
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            ExerciseNameHeader(exerciseId = exerciseId, database = database)

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 8.dp, end = 8.dp)) {
                                Text("SET", modifier = Modifier.width(40.dp), color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                Text(if (isMetric) "KG" else "LBS", modifier = Modifier.width(60.dp), color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("REPS", modifier = Modifier.width(60.dp), color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(24.dp))
                            }

                            exerciseSets.sortedBy { it.setNumber }.forEach { set ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${set.setNumber}", fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp), color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.weight(1f))
                                    val displayW = if (isMetric) set.weight else set.weight * 2.20462
                                    val weightStr = if (displayW % 1 == 0.0) displayW.toInt().toString() else "%.1f".format(displayW)
                                    Text(weightStr, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp), color = MaterialTheme.colorScheme.onSurface)
                                    Text("${set.reps}", fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp), color = MaterialTheme.colorScheme.onSurface)

                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Done",
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseNameHeader(exerciseId: String, database: AppDatabase) {
    var exerciseName by remember { mutableStateOf("Loading...") }

    LaunchedEffect(exerciseId) {
        val name = database.exerciseDao().getExerciseNameById(exerciseId)
        exerciseName = name ?: "Unknown Exercise"
    }

    Text(
        text = exerciseName,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary
    )
}
