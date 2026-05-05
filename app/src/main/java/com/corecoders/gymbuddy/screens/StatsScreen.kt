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

@Composable
fun StatsScreen(navController: NavController, workoutViewModel: WorkoutViewModel) {
    val scrollState = rememberScrollState()
    val workoutsCount by workoutViewModel.workoutsCount.collectAsState()
    val totalVolume by workoutViewModel.totalVolume.collectAsState()

    // Transformăm volumul în format "K" (ex: 47900 devine 47.9K)
    val formattedVolume = if (totalVolume >= 1000) {
        "%.1fK".format(totalVolume / 1000)
    } else {
        totalVolume.toInt().toString()
    }

    Scaffold(
        containerColor = Color(0xFFF2F2F7) // Fundal gri deschis iOS
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
                        color = Color.Black
                    )
                    Text(
                        text = "Lift Card Analytics",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { /* TODO: Settings */ },
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- TIME FILTER ---
            TimeFilterSelector()

            Spacer(modifier = Modifier.height(24.dp))

            // --- MAIN STATS GRID ---
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    title = "WORKOUTS LOGGED",
                    value = workoutsCount.toString(), // Valoare REALĂ
                    footer = "Total sessions",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "WEEKLY GOAL",
                    value = "1/4",
                    footer = "3 more to go → 25%",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    title = "WEIGHT",
                    value = "74.7",
                    unit = "kg",
                    footer = "↗ +0.6 kg\nLast updated Mar 23",
                    hasEditIcon = true,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "RELATIVE STRENGTH",
                    value = "1.57",
                    footer = "times your bodyweight",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- BODY COMPOSITION ---
            Text(
                text = "BODY COMPOSITION",
                color = Color.Gray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BodyCompCard("BODY FAT")
                BodyCompCard("MUSCLE")
                BodyCompCard("WAIST")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- MUSCLE FOCUS & VOLUME ---
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MuscleFocusCard(modifier = Modifier.weight(1.2f))
                StatCard(
                    title = "VOLUME",
                    value = formattedVolume, // Valoare REALĂ (ex: 12.4K)
                    footer = "Total kg lifted",
                    modifier = Modifier.weight(0.8f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- AREAS TO FOCUS ---
            AreasToFocusCard()

            Spacer(modifier = Modifier.height(24.dp))

            // --- HEALTH STATS ---
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    title = "STEPS",
                    value = "178.0K",
                    footer = "this month   ↑ 100%",
                    footerColor = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "ACTIVE CAL",
                    value = "2,963",
                    footer = "this month",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    title = "HEART RATE",
                    value = "109",
                    footer = "bpm",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "RESTING HR",
                    value = "53",
                    footer = "bpm",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(80.dp)) // Spațiu pentru Bottom Nav
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
                    .background(if (option == selectedOption) Color.White else Color.Transparent)
                    .clickable { selectedOption = option }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = option,
                    color = if (option == selectedOption) Color.Black else Color.Gray,
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
    footerColor: Color = Color.Gray,
    hasEditIcon: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.aspectRatio(0.9f) // Menține un aspect pătrățos
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
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                if (hasEditIcon) {
                    Icon(
                        imageVector = Icons.Outlined.MonitorWeight, // Placeholder pt icon edit
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = " $unit",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            Text(
                text = footer,
                color = footerColor,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun BodyCompCard(title: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .width(110.dp)
            .height(110.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = " $title",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Tap to add",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun MuscleFocusCard(modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "MUSCLE FOCUS",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            MuscleBar("Chest", "45 sets", 0.8f)
            MuscleBar("Back", "23 sets", 0.4f)
            MuscleBar("Arms", "19 sets", 0.3f)
            MuscleBar("Shoulders", "16 sets", 0.2f)
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
            Text(text = name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = sets, color = Color.Gray, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFFFF2D55), // Roz iOS
            trackColor = Color(0xFFF2F2F7),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun AreasToFocusCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "AREAS TO FOCUS",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            FocusRow("Legs", "Undertrained")
            Spacer(modifier = Modifier.height(12.dp))
            FocusRow("Core", "Undertrained")
        }
    }
}

@Composable
fun FocusRow(area: String, status: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFFF9500))) // Portocaliu
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = area, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = status, color = Color.Gray, fontSize = 14.sp)
    }
}