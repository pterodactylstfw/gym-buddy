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

    // Citim mereu baza de date LOCALĂ (Room)[cite: 9]
    private val localExercises: Flow<List<Exercise>> = exerciseDao.getAllExercises()

    // Combinăm datele locale cu filtrele UI[cite: 9]
    val exercises: StateFlow<List<Exercise>> = combine(
        _searchQuery,
        localExercises,
        _selectedCategory
    ) { text, all, category ->
        var filtered = all

        // Filtrare după categorie (dacă e selectat ceva diferit de "All")
        if (!category.isNullOrBlank() && category != "All") {
            filtered = filtered.filter { it.bodyPart.contains(category, ignoreCase = true) }
        }

        // Filtrare după text
        if (text.isNotBlank()) {
            filtered = filtered.filter { it.name.contains(text, ignoreCase = true) }
        }

        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // La crearea ViewModel-ului, declanșăm descărcarea[cite: 9]
        fetchCategories()
        syncExercisesFromApi()
    }


    private fun syncExercisesFromApi() {
        viewModelScope.launch {
            try {
                // Verificăm dacă avem deja date ca să nu mai așteptăm degeaba
                val existing = exerciseDao.getAllExercises().first()
                if (existing.size > 50) {
                    println("====== Avem deja ${existing.size} exerciții în baza de date. Sărim descărcarea! ======")
                    return@launch
                }

                println("====== Începem descărcarea pe bucăți (Hack pentru limitarea API-ului)... ======")

                // Tipurile principale de mușchi/părți pentru a face căutări țintite
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
                                Exercise(
                                    id = dto.exerciseId,
                                    name = dto.name.trim().replaceFirstChar { it.uppercase() },
                                    targetMuscle = dto.targetMuscles?.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Unknown",
                                    bodyPart = dto.bodyParts?.joinToString(", ") { p ->
                                        p.lowercase().replaceFirstChar { it.uppercase() }
                                    } ?: "Unknown",
                                    gifUrl = dto.imageUrl
                                )
                            }

                            allDownloadedExercises.addAll(mapped)
                            success = true

                            // PAUZĂ VITALĂ DE 1.5 SECUNDE! Fără ea, API-ul blochează emulatorul și dă UnknownHostException.
                            delay(1500)

                        } catch (e: Exception) {
                            retries++
                            println("Eroare la $query. Reîncercăm...")
                            delay(2000) // Pauză mai lungă dacă a crăpat
                        }
                    }
                }

                if (allDownloadedExercises.isNotEmpty()) {
                    val distinctExercises = allDownloadedExercises.distinctBy { it.id }
                    exerciseDao.insertExercises(distinctExercises)
                    println("====== HACK REUȘIT! Am adunat și salvat ${distinctExercises.size} exerciții în Room! ======")
                }

            } catch (e: Exception) {
                println("Eroare fatală la sincronizare: ${e.message}")
            }
        }
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = ApiClient.exerciseApi.getBodyParts()
                val partList = response.data.map { it.name.lowercase().replaceFirstChar { char -> char.uppercase() } }
                _categories.value = listOf("All") + partList
            } catch (e: Exception) {
                println("Failed to fetch categories: ${e.message}")
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = if (category == "All") null else category
    }
}

// Factory-ul rămâne la fel[cite: 9]
class ExerciseViewModelFactory(private val exerciseDao: ExerciseDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseViewModel(exerciseDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}