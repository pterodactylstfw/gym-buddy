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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(workoutId: Int, database: AppDatabase, onBack: () -> Unit) {
    val sets by database.workoutDao().getSetsforWorkout(workoutId).collectAsState(initial = emptyList())
    val groupedSets = sets.groupBy { it.exerciseId }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Workout Summary", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF007AFF))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF2F2F7))
            )
        },
        containerColor = Color(0xFFF2F2F7)
    ) { padding ->
        if (sets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Loading or no data...", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedSets.forEach { (exerciseId, exerciseSets) ->
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {

                                // AICI se face magia! Folosim noua noastră componentă pentru a trage numele real
                                ExerciseNameHeader(exerciseId = exerciseId, database = database)

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 8.dp, end = 8.dp)) {
                                    Text("SET", modifier = Modifier.width(40.dp), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("KG", modifier = Modifier.width(60.dp), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("REPS", modifier = Modifier.width(60.dp), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(24.dp))
                                }

                                exerciseSets.sortedBy { it.setNumber }.forEach { set ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .background(Color(0xFFF9F9F9), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${set.setNumber}", fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text("${set.weight}", fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
                                        Text("${set.reps}", fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))

                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Done",
                                            tint = Color(0xFF4CAF50),
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
}

// O componentă separată care caută numele exercițiului fără să blocheze restul ecranului
@Composable
fun ExerciseNameHeader(exerciseId: String, database: AppDatabase) {
    // Începem prin a arăta un text de încărcare scurt
    var exerciseName by remember { mutableStateOf("Loading...") }

    // Această bucată de cod rulează în fundal imediat ce componenta apare pe ecran
    LaunchedEffect(exerciseId) {
        val name = database.exerciseDao().getExerciseNameById(exerciseId)
        exerciseName = name ?: "Unknown Exercise" // Dacă l-a șters între timp, punem Unknown
    }

    Text(
        text = exerciseName,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF007AFF)
    )
}