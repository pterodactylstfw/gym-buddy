package com.corecoders.gymbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.corecoders.gymbuddy.data.Exercise
import com.corecoders.gymbuddy.viewmodel.ActiveWorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    viewModel: ActiveWorkoutViewModel,
    onAddExerciseClick: () -> Unit,
    onFinishClick: () -> Unit // Funcția care te trimite înapoi la Dashboard
) {
    val activeExercises by viewModel.activeExercises.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Workout", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = {
                        // Salvăm antrenamentul
                        viewModel.finishWorkout("Monday Workout") {
                            onFinishClick()
                        }
                    }) {
                        Text("Finish", fontWeight = FontWeight.Bold, color = Color(0xFF007AFF))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF7F7F7))
            )
        },
        containerColor = Color(0xFFF7F7F7)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(activeExercises) { exerciseIndex, activeExercise ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Titlul Exercițiului
                            Text(
                                text = activeExercise.exercise.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF007AFF) // Culoare tip iOS
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Rândurile pentru Seturi
                            activeExercise.sets.forEachIndexed { setIndex, activeSet ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Numărul setului
                                    Text("Set ${setIndex + 1}", fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp))

                                    // Input KG
                                    OutlinedTextField(
                                        value = activeSet.weight,
                                        onValueChange = { viewModel.updateSet(exerciseIndex, setIndex, weight = it) },
                                        label = { Text("kg") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.width(80.dp).height(60.dp),
                                        singleLine = true
                                    )

                                    // Input Reps
                                    OutlinedTextField(
                                        value = activeSet.reps,
                                        onValueChange = { viewModel.updateSet(exerciseIndex, setIndex, reps = it) },
                                        label = { Text("reps") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.width(80.dp).height(60.dp),
                                        singleLine = true
                                    )

                                    // Bifa de completare (Checkbox iOS style ar fi un cerc verde)
                                    Checkbox(
                                        checked = activeSet.isCompleted,
                                        onCheckedChange = { viewModel.updateSet(exerciseIndex, setIndex, isCompleted = it) },
                                    )
                                }
                            }

                            // Buton de adăugat set nou
                            TextButton(
                                onClick = { viewModel.addSetToExercise(exerciseIndex) },
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                            ) {
                                Text("+ Add Set", color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Butonul gigant de jos (pentru test: adaugă automat un exercițiu din cod ca să ai ce vedea pe ecran)
            Button(
                onClick = onAddExerciseClick, // Deshide catalogul
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Add Exercise")
            }
        }
    }
}