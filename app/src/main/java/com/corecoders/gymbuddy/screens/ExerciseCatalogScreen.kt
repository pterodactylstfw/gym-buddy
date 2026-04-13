package com.corecoders.gymbuddy.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape // NOU: Shape rotunjit
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // NOU: Pentru a tăia imaginea rotunjit
import androidx.compose.ui.graphics.Color // NOU: Culoare custom
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.corecoders.gymbuddy.data.Exercise
import com.corecoders.gymbuddy.viewmodel.ExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCatalogScreen(viewModel: ExerciseViewModel, onExerciseSelected: (Exercise) -> Unit) {
    val context = LocalContext.current

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val exercises by viewModel.exercises.collectAsState()

    Scaffold(
        // Modificăm TopAppBar să fie mai minimalist
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Exercises", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF7F7F7) // Fundal gri foarte deschis
                )
            )
        },
        containerColor = Color(0xFFF7F7F7) // Fundal general al ecranului
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Bara de Căutare redesenată
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search exercises...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                // Shape rotunjit pentru bară
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            // Lista de Exerciții
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp) // Spațiere mai mare
            ) {
                items(exercises) { exercise ->
                    // Card redesenat
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable{onExerciseSelected(exercise)},
                        // Colțuri FOARTE rotunjite
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White // Card alb curat
                        ),
                        // Elevație mai mică, pentru un look flat
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp), // Spațiere interioară mai mare
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Imagine rotunjită și curată
                            AsyncImage(
                                model = exercise.gifUrl,
                                contentDescription = exercise.name,
                                imageLoader = imageLoader,
                                modifier = Modifier
                                    .size(64.dp) // Puțin mai mică imaginea
                                    .clip(RoundedCornerShape(8.dp)) // Rotunjim și imaginea
                                    .background(Color(0xFFEEEEEE)), // Fundal gri pentru imagine
                                contentScale = ContentScale.Crop
                            )

                            // Detaliile text ierarhizate
                            Column(
                                modifier = Modifier.padding(start = 16.dp)
                            ) {
                                Text(
                                    text = exercise.name,
                                    // Font gros, stil titlu
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold, // Foarte gros
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = exercise.targetMuscle,
                                    // Font mai subțire, culoare secundară (albastru)
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF007AFF) // Albastru specific iOS
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}

