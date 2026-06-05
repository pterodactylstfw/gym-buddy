package com.corecoders.gymbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corecoders.gymbuddy.ui.theme.SuccessGreen
import com.corecoders.gymbuddy.ui.theme.WarningOrange
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
                title = { 
                    Text(
                        "ACTIVE SESSION", 
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.finishWorkout { onFinishClick() }
                    }) {
                        Text("FINISH", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Input pentru numele antrenamentului (Minimalist)
            BasicTextField(
                value = workoutName,
                onValueChange = { viewModel.updateWorkoutName(it) },
                textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                decorationBox = { innerTextField ->
                    if (workoutName.isEmpty()) {
                        Text("Workout Name", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.titleLarge)
                    }
                    innerTextField()
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(activeExercises) { exerciseIndex, activeExercise ->
                    ExerciseCard(
                        exerciseIndex = exerciseIndex,
                        activeExercise = activeExercise,
                        viewModel = viewModel
                    )
                }
            }

            // Butonul gigant de jos (Modern Red)
            Button(
                onClick = onAddExerciseClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("ADD EXERCISE", style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exerciseIndex: Int,
    activeExercise: com.corecoders.gymbuddy.viewmodel.ActiveExercise,
    viewModel: ActiveWorkoutViewModel
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Exercițiu
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = activeExercise.exercise.name.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))

            // Labels pentru coloane
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Text("SET", modifier = Modifier.width(36.dp), color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("TYPE", modifier = Modifier.width(44.dp), color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.weight(1f))
                Text("KG", modifier = Modifier.width(60.dp), color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("REPS", modifier = Modifier.width(60.dp), color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(40.dp))
            }

            // Rândurile pentru Seturi
            activeExercise.sets.forEachIndexed { setIndex, activeSet ->
                val isCompleted = activeSet.isCompleted
                
                // Set Type Color
                val typeColor = when(activeSet.setType) {
                    "W" -> WarningOrange
                    "D" -> SuccessGreen
                    "F" -> MaterialTheme.colorScheme.primary
                    else -> if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                }
                
                // Hairline Divider
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .alpha(if (isCompleted) 1f else 0.5f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Index
                    Text(
                        "${setIndex + 1}", 
                        fontWeight = FontWeight.Bold, 
                        modifier = Modifier.width(36.dp),
                        color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )

                    // Type Toggle (N, W, D, F)
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .clickable {
                                val nextType = when (activeSet.setType) {
                                    "N" -> "W"
                                    "W" -> "D"
                                    "D" -> "F"
                                    else -> "N"
                                }
                                viewModel.updateSet(exerciseIndex, setIndex, setType = nextType)
                            }
                            .background(
                                typeColor.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            activeSet.setType,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = typeColor
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Input KG
                    SetInputField(
                        value = activeSet.weight,
                        onValueChange = { newValue ->
                            val filtered = newValue.filter { it.isDigit() || it == '.' }
                            viewModel.updateSet(exerciseIndex, setIndex, weight = filtered)
                        },
                        isCompleted = isCompleted
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Input Reps
                    SetInputField(
                        value = activeSet.reps,
                        onValueChange = { newValue ->
                            val filtered = newValue.filter { it.isDigit() }
                            viewModel.updateSet(exerciseIndex, setIndex, reps = filtered)
                        },
                        isCompleted = isCompleted
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Status Check (Custom Red Circle)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                if (isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent,
                                CircleShape
                            )
                            .border(
                                1.5.dp,
                                if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                CircleShape
                            )
                            .clickable {
                                viewModel.updateSet(exerciseIndex, setIndex, isCompleted = !isCompleted)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Buton de adăugat set nou (Minimalist)
            TextButton(
                onClick = { viewModel.addSetToExercise(exerciseIndex) },
                modifier = Modifier.align(Alignment.Start).padding(top = 8.dp)
            ) {
                Text("+ ADD SET", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 0.5.sp)
            }
        }
    }
}

@Composable
fun SetInputField(
    value: String,
    onValueChange: (String) -> Unit,
    isCompleted: Boolean
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .width(60.dp)
            .height(36.dp)
            .background(
                if (isCompleted) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant, 
                RoundedCornerShape(8.dp)
            )
            .border(
                0.5.dp, 
                if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline, 
                RoundedCornerShape(8.dp)
            )
            .padding(top = 8.dp),
        textStyle = TextStyle(
            color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        ),
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
    )
}
