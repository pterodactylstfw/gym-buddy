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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corecoders.gymbuddy.viewmodel.ActiveWorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    viewModel: ActiveWorkoutViewModel,
    onAddExerciseClick: () -> Unit,
    onFinishClick: () -> Unit
) {
    val activeExercises by viewModel.activeExercises.collectAsState()
    val workoutName by viewModel.workoutName.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Active Workout", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = {
                        viewModel.finishWorkout { onFinishClick() }
                    }) {
                        Text("Finish", fontWeight = FontWeight.Bold, color = Color(0xFF007AFF))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF2F2F7) // Gri specific iOS pentru background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Input pentru numele antrenamentului
            TextField(
                value = workoutName,
                onValueChange = { viewModel.updateWorkoutName(it) },
                textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Workout Name", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold) }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(activeExercises) { exerciseIndex, activeExercise ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Titlul Exercițiului
                            Text(
                                text = activeExercise.exercise.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF007AFF)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Header pentru coloane
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 8.dp, end = 8.dp)) {
                                Text("SET", modifier = Modifier.width(40.dp), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("KG", modifier = Modifier.width(70.dp), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("REPS", modifier = Modifier.width(70.dp), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(48.dp)) // Spațiu pentru checkbox
                            }

                            // Rândurile pentru Seturi
                            activeExercise.sets.forEachIndexed { setIndex, activeSet ->
                                // Dacă e completat, facem fundalul verde deschis
                                val rowBackgroundColor = if (activeSet.isCompleted) Color(0xFFE8F5E9) else Color.Transparent

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(rowBackgroundColor, RoundedCornerShape(8.dp))
                                        .padding(vertical = 4.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Numărul setului
                                    Text("${setIndex + 1}", fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))

                                    Spacer(modifier = Modifier.weight(1f))

                                    // Input KG
                                    OutlinedTextField(
                                        value = activeSet.weight,
                                        onValueChange = { viewModel.updateSet(exerciseIndex, setIndex, weight = it) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.width(70.dp).height(50.dp),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Input Reps
                                    OutlinedTextField(
                                        value = activeSet.reps,
                                        onValueChange = { viewModel.updateSet(exerciseIndex, setIndex, reps = it) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.width(70.dp).height(50.dp),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                                    )

                                    // Checkbox completare (Design customizat)
                                    Checkbox(
                                        checked = activeSet.isCompleted,
                                        onCheckedChange = { viewModel.updateSet(exerciseIndex, setIndex, isCompleted = it) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFF4CAF50), // Verde
                                            uncheckedColor = Color.LightGray
                                        )
                                    )
                                }
                            }

                            // Buton de adăugat set nou
                            TextButton(
                                onClick = { viewModel.addSetToExercise(exerciseIndex) },
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                            ) {
                                Text("+ Add Set", color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Butonul gigant de jos pentru a adăuga exerciții
            Button(
                onClick = onAddExerciseClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
            ) {
                Text("+ Add Exercise", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}