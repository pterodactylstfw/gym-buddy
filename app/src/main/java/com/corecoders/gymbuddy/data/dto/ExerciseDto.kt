data class ExerciseDto(
    val exerciseId: String,
    val name: String,
    val imageUrl: String,
    val targetMuscles: List<String>?, // Adăugat ?
    val bodyParts: List<String>?      // Adăugat ?
)