package com.corecoders.gymbuddy.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ActiveWorkoutScreen(
    viewModel: ActiveWorkoutViewModel,
    onAddExerciseClick: () -> Unit,
    onFinishClick: (Int?) -> Unit,
    onCancelClick: () -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var setToDelete by remember { mutableStateOf<Pair<Int, Int>?>(null) } // Pair(exerciseIndex, setIndex)

    val activeExercises by viewModel.activeExercises.collectAsState()
    val workoutName by viewModel.workoutName.collectAsState()
    val elapsedTime by viewModel.elapsedTime.collectAsState()
    val restTimeRemaining by viewModel.restTimeRemaining.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "ACTIVE SESSION", 
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.secondary
                        ) 
                        Text(
                            elapsedTime,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showCancelDialog = true }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel Workout", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    TextButton(onClick = { showFinishDialog = true }) {
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
                    val previousSetsMap by viewModel.previousSets.collectAsState()
                    val prevSets = previousSetsMap[activeExercise.exercise.id] ?: emptyList()

                    ExerciseCard(
                        exerciseIndex = exerciseIndex,
                        activeExercise = activeExercise,
                        viewModel = viewModel,
                        prevSets = prevSets,
                        onLongClickSet = { setIndex ->
                            if (activeExercise.sets.size > 1) {
                                setToDelete = Pair(exerciseIndex, setIndex)
                            }
                        }
                    )
                }
            }

            // Rest Timer Banner overlay at the bottom if active
            restTimeRemaining?.let { remaining ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Rest Time",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Rest Timer: ${remaining / 60}:${String.format("%02d", remaining % 60)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        TextButton(onClick = { viewModel.stopRestTimer() }) {
                            Text("SKIP", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                        }
                    }
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

        if (showCancelDialog) {
            AlertDialog(
                onDismissRequest = { showCancelDialog = false },
                title = { Text("Cancel Workout?") },
                text = { Text("Are you sure you want to cancel? All unsaved progress will be lost.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showCancelDialog = false
                            onCancelClick()
                        }
                    ) {
                        Text("Yes, Cancel", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog = false }) {
                        Text("Keep Working Out")
                    }
                }
            )
        }

        if (showFinishDialog) {
            AlertDialog(
                onDismissRequest = { showFinishDialog = false },
                title = { Text("Finish Workout?") },
                text = { Text("Are you sure you want to finish and save this workout session?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showFinishDialog = false
                            viewModel.finishWorkout { workoutId -> onFinishClick(workoutId) }
                        }
                    ) {
                        Text("Finish", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFinishDialog = false }) {
                        Text("Keep Working Out")
                    }
                }
            )
        }

        if (setToDelete != null) {
            AlertDialog(
                onDismissRequest = { setToDelete = null },
                title = { Text("Delete Set?") },
                text = { Text("Are you sure you want to delete Set ${setToDelete!!.second + 1}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val (eIdx, sIdx) = setToDelete!!
                            viewModel.removeSetFromExercise(eIdx, sIdx)
                            setToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { setToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseCard(
    exerciseIndex: Int,
    activeExercise: com.corecoders.gymbuddy.viewmodel.ActiveExercise,
    viewModel: ActiveWorkoutViewModel,
    prevSets: List<com.corecoders.gymbuddy.data.WorkoutSet>,
    onLongClickSet: (Int) -> Unit
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
                IconButton(onClick = { viewModel.removeExercise(exerciseIndex) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Exercise", tint = MaterialTheme.colorScheme.error)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))

            // Labels pentru coloane
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Text("SET", modifier = Modifier.width(36.dp), color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("TYPE", modifier = Modifier.width(44.dp), color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.weight(1f))
                Text("KG", modifier = Modifier.width(60.dp), color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(8.dp))
                Text("REPS", modifier = Modifier.width(60.dp), color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(40.dp))
            }

            // Rândurile pentru Seturi
            activeExercise.sets.forEachIndexed { setIndex, activeSet ->
                val isCompleted = activeSet.isCompleted
                val prevSet = prevSets.getOrNull(setIndex)

                val weightPlaceholder = prevSet?.let { if (it.weight % 1 == 0.0) it.weight.toInt().toString() else "%.1f".format(it.weight) } ?: ""
                val repsPlaceholder = prevSet?.reps?.toString() ?: ""
                
                // Set Type Color
                val typeColor = when(activeSet.setType) {
                    "W" -> WarningOrange
                    "D" -> SuccessGreen
                    "F" -> MaterialTheme.colorScheme.primary
                    else -> if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                }
                
                // Hairline Divider
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

                val hasData = activeSet.weight.isNotEmpty() || activeSet.reps.isNotEmpty()
                val rowAlpha = if (isCompleted) 1f else if (hasData) 0.85f else 0.5f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .alpha(rowAlpha)
                        .combinedClickable(
                            onClick = { /* managed by field focus */ },
                            onLongClick = { onLongClickSet(setIndex) }
                        ),
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
                        placeholder = weightPlaceholder,
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
                        placeholder = repsPlaceholder,
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
    placeholder: String,
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
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        color = if (isCompleted) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
                innerTextField()
            }
        }
    )
}
