package com.corecoders.gymbuddy.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.Exercise
import com.corecoders.gymbuddy.services.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExerciseViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _allExercises = MutableStateFlow<List<Exercise>>(emptyList())

    // State-uri pentru UI (Loading și Final de listă)
    var isNextPageLoading by mutableStateOf(false)
    var isEndReached by mutableStateOf(false)
    private var nextCursor: String? = null

    // Combinăm search-ul cu lista totală
    val exercises: StateFlow<List<Exercise>> = combine(_searchQuery, _allExercises) { text, all ->
        if (text.isBlank()) all
        else all.filter { it.name.contains(text, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // La inițializare, încărcăm prima pagină
        fetchNextPage()
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun fetchNextPage() {
        // Dacă deja încărcăm ceva sau am ajuns la capăt, nu mai facem nimic
        if (isNextPageLoading || isEndReached) return

        viewModelScope.launch {
            isNextPageLoading = true
            try {
                // Facem apelul la API folosind cursorul curent (null prima dată)
                val response = ApiClient.exerciseApi.getAllExercises(cursor = nextCursor)

                // Mapăm rezultatele de la DTO la clasa noastră Exercise
                val newExercises = response.data.map { dto ->
                    Exercise(
                        id = dto.exerciseId,
                        name = dto.name.trim().replaceFirstChar { it.uppercase() },
                        targetMuscle = dto.targetMuscles.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Unknown",
                        gifUrl = dto.imageUrl
                    )
                }

                // Actualizăm cursorul pentru pagina următoare și verificăm dacă mai există date
                nextCursor = response.meta.nextCursor
                isEndReached = !response.meta.hasNextPage

                // Adăugăm noile exerciții la cele existente
                _allExercises.value = _allExercises.value + newExercises

            } catch (e: Exception) {
                println("Eroare API: ${e.message}")
            } finally {
                isNextPageLoading = false
            }
        }
    }
}