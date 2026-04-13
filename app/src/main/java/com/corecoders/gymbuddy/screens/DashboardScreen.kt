package com.corecoders.gymbuddy.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.corecoders.gymbuddy.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: WorkoutViewModel) {
    val workouts by viewModel.allWorkouts.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            LargeTopAppBar(title = { Text("My Workouts") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.startNewWorkout("Workout #${workouts.size + 1}")
            }) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (workouts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No workouts yet. Tap + to start!")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(workouts) { workout ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = workout.name, style = MaterialTheme.typography.titleMedium)
                                val date = SimpleDateFormat(
                                    "dd MMM yyyy, HH:mm",
                                    Locale.getDefault()
                                )
                                    .format(Date(workout.date))
                                Text(text = date, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}