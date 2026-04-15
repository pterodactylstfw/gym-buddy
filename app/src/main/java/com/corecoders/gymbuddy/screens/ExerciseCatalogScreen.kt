package com.corecoders.gymbuddy.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
                if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
                else add(GifDecoder.Factory())
            }.build()
    }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val exercises by viewModel.exercises.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Exercises", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF7F7F7))
            )
        },
        containerColor = Color(0xFFF7F7F7)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search exercises...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // REPARARE: Folosim itemsIndexed pentru a detecta finalul listei
                itemsIndexed(exercises) { index, exercise ->

                    // Detectăm scroll-ul la final
                    if (index >= exercises.size - 1 && !viewModel.isEndReached && !viewModel.isNextPageLoading) {
                        LaunchedEffect(Unit) {
                            viewModel.fetchNextPage()
                        }
                    }

                    // Card-ul tău (înlocuiește ExerciseCard care lipsea)
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onExerciseSelected(exercise) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = exercise.gifUrl,
                                contentDescription = exercise.name,
                                imageLoader = imageLoader,
                                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFEEEEEE)),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.padding(start = 16.dp)) {
                                Text(text = exercise.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = exercise.targetMuscle, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF007AFF))
                            }
                        }
                    }
                }

                // Arătăm rotița de încărcare doar când se încarcă pagina următoare
                if (viewModel.isNextPageLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

