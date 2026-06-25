package com.corecoders.gymbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.corecoders.gymbuddy.data.Exercise
import com.corecoders.gymbuddy.data.dto.ExerciseDto
import com.corecoders.gymbuddy.viewmodel.ExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCatalogScreen(
    viewModel: ExerciseViewModel,
    onExerciseSelected: ((Exercise) -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var selectedExerciseToShow by remember { mutableStateOf<Exercise?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercises", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
                navigationIcon = {
                    if (onBack != null) {
                        TextButton(onClick = onBack) {
                            Text("Cancel", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search exercises") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            // Filter
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = (category == "All" && selectedCategory == null) || (category == selectedCategory)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = { Text(category) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            // Exercises list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(exercises) { exercise ->
                    ExerciseItem(
                        exercise = exercise,
                        onClick = {
                            if (onExerciseSelected != null) {
                                onExerciseSelected(exercise)
                            } else {
                                selectedExerciseToShow = it
                            }
                        }
                    )
                }
            }
        }

        // Floating exercise detail catalog
        selectedExerciseToShow?.let { exercise ->
            var exerciseDetail by remember(exercise.id) { mutableStateOf<ExerciseDto?>(null) }
            var isLoadingDetail by remember(exercise.id) { mutableStateOf(false) }
            var loadingError by remember(exercise.id) { mutableStateOf<String?>(null) }

            LaunchedEffect(exercise.id) {
                isLoadingDetail = true
                loadingError = null
                try {
                    val response = com.corecoders.gymbuddy.services.ApiClient.exerciseApi.getExerciseById(exercise.id)
                    if (response.success) {
                        exerciseDetail = response.data
                    } else {
                        loadingError = "Could not load details"
                    }
                } catch (e: java.net.UnknownHostException) {
                    loadingError = "No internet connection. Please check your network and try again."
                } catch (e: java.io.IOException) {
                    loadingError = "Connection error. Please try again later."
                } catch (e: Exception) {
                    loadingError = e.localizedMessage ?: "Error loading details."
                } finally {
                    isLoadingDetail = false
                }
            }

            Dialog(onDismissRequest = { selectedExerciseToShow = null }) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .padding(16.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        // Scrollable content area
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = exercise.name.uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val displayCategory = if (exercise.targetMuscle != "Unknown") {
                                if (exercise.bodyPart != "Unknown" && exercise.bodyPart != exercise.targetMuscle) {
                                    "${exercise.bodyPart} • ${exercise.targetMuscle}"
                                } else {
                                    exercise.targetMuscle
                                }
                            } else if (exercise.bodyPart != "Unknown") {
                                exercise.bodyPart
                            } else {
                                "Full Body"
                            }

                            Text(
                                text = displayCategory,
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Imaginea mare a exercitiului (GIF)
                            AsyncImage(
                                model = exercise.gifUrl,
                                contentDescription = exercise.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.background),
                                contentScale = ContentScale.Fit
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (isLoadingDetail) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else if (loadingError != null) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudOff,
                                        contentDescription = "Offline",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = loadingError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else if (exerciseDetail != null) {
                                val detail = exerciseDetail!!
                                
                                // Badges for Equipment and Exercise Type
                                val equipmentText = detail.equipments?.joinToString(", ") ?: "None"
                                val typeText = detail.exerciseType ?: "Unknown"

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                                ) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("EQ: ${equipmentText.uppercase()}") },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            labelColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("TYPE: ${typeText.uppercase()}") },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            labelColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }

                                if (!detail.overview.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = detail.overview,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Justify,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }

                                detail.instructions?.let { instructions ->
                                    if (instructions.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "INSTRUCTIONS",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.align(Alignment.Start)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        instructions.forEachIndexed { index, step ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.Start
                                            ) {
                                                Text(
                                                    text = "${index + 1}. ",
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = step,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Bottom Actions area
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = { selectedExerciseToShow = null },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(0.8f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("CLOSE", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseItem(exercise: Exercise, onClick: (Exercise) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(exercise) }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = exercise.gifUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    exercise.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val displayCategory = if (exercise.targetMuscle != "Unknown") {
                    if (exercise.bodyPart != "Unknown" && exercise.bodyPart != exercise.targetMuscle) {
                        "${exercise.bodyPart} • ${exercise.targetMuscle}"
                    } else {
                        exercise.targetMuscle
                    }
                } else if (exercise.bodyPart != "Unknown") {
                    exercise.bodyPart
                } else {
                    "Full Body"
                }

                Text(
                    displayCategory,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
