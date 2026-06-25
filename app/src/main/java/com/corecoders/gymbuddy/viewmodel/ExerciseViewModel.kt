package com.corecoders.gymbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.Exercise
import com.corecoders.gymbuddy.data.dao.ExerciseDao
import com.corecoders.gymbuddy.services.ApiClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExerciseViewModel(private val exerciseDao: ExerciseDao) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(listOf("All"))
    val categories = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    // Citim mereu baza de date locala
    private val localExercises: Flow<List<Exercise>> = exerciseDao.getAllExercises()

    // Combinam datele locale cu filtrele UI
    val exercises: StateFlow<List<Exercise>> = combine(
        _searchQuery,
        localExercises,
        _selectedCategory
    ) { text, all, category ->
        var filtered = all

        // Filtrare dupa categorie
        if (!category.isNullOrBlank() && category != "All") {
            filtered = filtered.filter { exercise ->
                val searchStr = category.lowercase()
                val bodyPart = exercise.bodyPart.lowercase()
                val target = exercise.targetMuscle.lowercase()
                val name = exercise.name.lowercase()

                when (searchStr) {
                    "core" -> bodyPart.contains("waist") || bodyPart.contains("core") || name.contains("abs") || target.contains("abs") || name.contains("crunch") || name.contains("plank")
                    "arms" -> bodyPart.contains("arm") || name.contains("curl") || name.contains("tricep") || name.contains("bicep")
                    "legs" -> bodyPart.contains("leg") || bodyPart.contains("calv") || bodyPart.contains("thigh") || bodyPart.contains("glute") || name.contains("squat") || name.contains("lunge") || target.contains("quad") || target.contains("hamstring")
                    "chest" -> bodyPart.contains("chest") || target.contains("pectoral") || name.contains("press") || name.contains("fly") || name.contains("push-up")
                    "back" -> bodyPart.contains("back") || target.contains("lat") || target.contains("spine") || name.contains("row") || name.contains("pull")
                    "shoulders" -> bodyPart.contains("shoulder") || target.contains("deltoid") || name.contains("raise")
                    "cardio" -> bodyPart.contains("cardio") || name.contains("run") || name.contains("jump") || name.contains("step")
                    else -> bodyPart.contains(searchStr) || target.contains(searchStr) || name.contains(searchStr)
                }
            }
        }

        // Filtrare dupa text
        if (text.isNotBlank()) {
            filtered = filtered.filter { it.name.contains(text, ignoreCase = true) }
        }

        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // La crearea viewmodelului, declansam descarcarea
        fetchCategories()
        syncExercisesFromApi()
    }


    private fun syncExercisesFromApi() {
        viewModelScope.launch {
            try {
                // Verificam daca avem deja baza de date completa
                val benchPress = exerciseDao.getExerciseNameById("exr_41n2hxnFMotsXTj3")
                val existing = exerciseDao.getAllExercises().first()
                if (existing.size > 50 && benchPress != null) {
                    println("====== Avem deja baza de date completă. Sărim descărcarea! ======")
                    return@launch
                }

                println("====== Începem descărcarea pe bucăți (Hack pentru a ocoli limitarea API-ului)... ======")

                // Stergem ce s-a descarcat
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    exerciseDao.clearExercises()
                }

                val searchQueries = listOf("CHEST", "BACK", "UPPER ARMS", "LOWER ARMS", "SHOULDERS", "LEGS", "CALVES", "WAIST", "CARDIO")
                val allDownloadedExercises = mutableListOf<Exercise>()

                for (query in searchQueries) {
                    var retries = 0
                    var success = false

                    while (!success && retries < 2) {
                        try {
                            println("--> Căutăm: $query...")
                            val response = ApiClient.exerciseApi.searchExercises(query = query, limit = 50)

                            val mapped = response.data.map { dto ->
                                val mappedBodyPart = when (query) {
                                    "UPPER ARMS", "LOWER ARMS" -> "Arms"
                                    "CALVES" -> "Legs"
                                    "WAIST" -> "Core"
                                    else -> query.lowercase().replaceFirstChar { it.uppercase() }
                                }

                                Exercise(
                                    id = dto.exerciseId,
                                    name = dto.name.trim().replaceFirstChar { it.uppercase() },
                                    targetMuscle = "Unknown",
                                    bodyPart = mappedBodyPart,
                                    gifUrl = dto.imageUrl
                                )
                            }

                            allDownloadedExercises.addAll(mapped)
                            success = true

                            delay(1500)

                        } catch (e: Exception) {
                            retries++
                            println("Eroare la $query. Reîncercăm...")
                            delay(2000)
                        }
                    }
                }

                if (allDownloadedExercises.isNotEmpty()) {
                    val distinctExercises = allDownloadedExercises.distinctBy { it.id }
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        exerciseDao.insertExercises(distinctExercises)
                    }
                    println("====== HACK REUȘIT! Am adunat și salvat ${distinctExercises.size} exerciții în Room! ======")
                }

            } catch (e: Exception) {
                println("Eroare fatală la sincronizare: ${e.message}")
            }
        }
    }

    private fun fetchCategories() {
        _categories.value = listOf("All", "Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Cardio")
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = if (category == "All") null else category
    }
}

class ExerciseViewModelFactory(private val exerciseDao: ExerciseDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseViewModel(exerciseDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}