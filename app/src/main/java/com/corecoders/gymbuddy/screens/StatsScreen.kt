package com.corecoders.gymbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.corecoders.gymbuddy.viewmodel.WorkoutViewModel
import com.corecoders.gymbuddy.viewmodel.ProfileViewModel
import com.corecoders.gymbuddy.data.dao.MuscleSetCount

@Composable
fun StatsScreen(navController: NavController, workoutViewModel: WorkoutViewModel, profileViewModel: ProfileViewModel) {
    val scrollState = rememberScrollState()
    val workoutsCount by workoutViewModel.workoutsCount.collectAsState()
    val totalVolume by workoutViewModel.totalVolume.collectAsState()
    val weight by profileViewModel.weight.collectAsState()
    val bodyFat by profileViewModel.bodyFat.collectAsState()
    val muscleMass by profileViewModel.muscleMass.collectAsState()
    val waistSize by profileViewModel.waistSize.collectAsState()

    val weeklyDays by workoutViewModel.weeklyAttendanceDays.collectAsState()
    val maxWeight by workoutViewModel.maxWeightLifted.collectAsState()
    val totalDuration by workoutViewModel.totalDurationMinutes.collectAsState()
    val muscleCounts by workoutViewModel.muscleSetCounts.collectAsState()
    val trainingFrequency by profileViewModel.trainingFrequency.collectAsState()

    var showBodyCompDialog by remember { mutableStateOf<String?>(null) }
    var bodyCompInputValue by remember { mutableStateOf("") }

    val formattedVolume = if (totalVolume >= 1000) {
        "%.1fK".format(totalVolume / 1000)
    } else {
        totalVolume.toInt().toString()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Performance",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Lift Card Analytics",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- TIME FILTER ---
            TimeFilterSelector()

            Spacer(modifier = Modifier.height(24.dp))

            // --- MAIN STATS GRID ---
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    title = "WORKOUTS LOGGED",
                    value = workoutsCount.toString(),
                    footer = "Total sessions",
                    modifier = Modifier.weight(1f)
                )
                val goal = if (trainingFrequency > 0) trainingFrequency else 4
                StatCard(
                    title = "WEEKLY GOAL",
                    value = "$weeklyDays/$goal",
                    footer = "${(goal - weeklyDays).coerceAtLeast(0)} more to go → ${((weeklyDays.toFloat() / goal.toFloat()) * 100).toInt()}%",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    title = "WEIGHT",
                    value = if (weight > 0f) {
                        if (weight == weight.toInt().toFloat()) weight.toInt().toString() else weight.toString()
                    } else "--",
                    unit = "kg",
                    footer = "From Profile",
                    hasEditIcon = true,
                    modifier = Modifier.weight(1f)
                )
                val relStrength = if (weight > 0f && maxWeight > 0f) {
                    "%.2f".format(maxWeight / weight)
                } else "--"

                StatCard(
                    title = "RELATIVE STRENGTH",
                    value = relStrength,
                    footer = "times your bodyweight",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- BODY COMPOSITION ---
            Text(
                text = "BODY COMPOSITION",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BodyCompCard("BODY FAT", if (bodyFat.isNotEmpty()) "$bodyFat %" else "Tap to add") {
                    showBodyCompDialog = "BODY FAT"
                    bodyCompInputValue = bodyFat
                }
                BodyCompCard("MUSCLE", if (muscleMass.isNotEmpty()) "$muscleMass kg" else "Tap to add") {
                    showBodyCompDialog = "MUSCLE"
                    bodyCompInputValue = muscleMass
                }
                BodyCompCard("WAIST", if (waistSize.isNotEmpty()) "$waistSize cm" else "Tap to add") {
                    showBodyCompDialog = "WAIST"
                    bodyCompInputValue = waistSize
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- MUSCLE FOCUS ---
            MuscleFocusCard(muscleCounts, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(24.dp))

            // --- VOLUME & CALORIES ---
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    title = "VOLUME",
                    value = formattedVolume,
                    footer = "Total kg lifted",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "EST. CALORIES",
                    value = "%,d".format(totalDuration * 6),
                    footer = "total burned",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- AREAS TO FOCUS ---
            AreasToFocusCard(muscleCounts)

            Spacer(modifier = Modifier.height(80.dp))
        }

        if (showBodyCompDialog != null) {
            AlertDialog(
                onDismissRequest = { showBodyCompDialog = null },
                title = { Text("Update $showBodyCompDialog") },
                text = {
                    OutlinedTextField(
                        value = bodyCompInputValue,
                        onValueChange = { bodyCompInputValue = it },
                        label = { Text("Value") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        when (showBodyCompDialog) {
                            "BODY FAT" -> profileViewModel.updateBodyComposition(bodyFat = bodyCompInputValue, muscleMass = null, waistSize = null)
                            "MUSCLE" -> profileViewModel.updateBodyComposition(bodyFat = null, muscleMass = bodyCompInputValue, waistSize = null)
                            "WAIST" -> profileViewModel.updateBodyComposition(bodyFat = null, muscleMass = null, waistSize = bodyCompInputValue)
                        }
                        showBodyCompDialog = null
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBodyCompDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun TimeFilterSelector() {
    val options = listOf("1D", "1W", "1M", "3M", "1Y", "All")
    var selectedOption by remember { mutableStateOf("1M") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        options.forEach { option ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (option == selectedOption) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { selectedOption = option }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = option,
                    color = if (option == selectedOption) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.secondary,
                    fontWeight = if (option == selectedOption) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    unit: String = "",
    footer: String,
    footerColor: Color? = null,
    hasEditIcon: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.aspectRatio(0.9f)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                if (hasEditIcon) {
                    Icon(
                        imageVector = Icons.Outlined.MonitorWeight,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = " $unit",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            Text(
                text = footer,
                color = footerColor ?: MaterialTheme.colorScheme.secondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun BodyCompCard(title: String, value: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .width(110.dp)
            .height(110.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = " $title",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                color = if (value == "Tap to add") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                fontSize = if (value == "Tap to add") 14.sp else 20.sp,
                fontWeight = if (value == "Tap to add") FontWeight.Normal else FontWeight.Bold
            )
        }
    }
}

@Composable
fun MuscleFocusCard(muscleCounts: List<MuscleSetCount>, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "MUSCLE FOCUS",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (muscleCounts.isEmpty()) {
                Text("Log workouts to see your focus", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
            } else {
                val totalSets = muscleCounts.sumOf { it.setCount }.toFloat().coerceAtLeast(1f)
                muscleCounts.take(4).forEach { count ->
                    MuscleBar(count.bodyPart, "${count.setCount} sets", count.setCount / totalSets)
                }
            }
        }
    }
}

@Composable
fun MuscleBar(name: String, sets: String, progress: Float) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = sets, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun AreasToFocusCard(muscleCounts: List<MuscleSetCount>) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "AREAS TO FOCUS",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            val allMuscles = listOf("chest", "back", "legs", "shoulders", "arms", "core")
            val trainedMuscles = muscleCounts.map { it.bodyPart.lowercase() }
            val untrained = allMuscles.filter { it !in trainedMuscles }
            
            if (untrained.isEmpty() && muscleCounts.size >= 2) {
                // Toate sunt antrenate, alege cele cu cele mai putine seturi
                val lowest = muscleCounts.takeLast(2)
                lowest.forEach { 
                    FocusRow(it.bodyPart.replaceFirstChar { c -> c.uppercase() }, "Needs more volume") 
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else if (untrained.isNotEmpty()) {
                untrained.take(2).forEach { 
                    FocusRow(it.replaceFirstChar { c -> c.uppercase() }, "Undertrained") 
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                 Text("Log workouts to identify areas to focus", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun FocusRow(area: String, status: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(com.corecoders.gymbuddy.ui.theme.WarningOrange))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = area, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = status, color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
    }
}
