package com.corecoders.gymbuddy.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.Routine
import com.corecoders.gymbuddy.data.Workout
import com.corecoders.gymbuddy.data.dao.RoutineDao
import com.corecoders.gymbuddy.data.dao.WorkoutDao
import com.corecoders.gymbuddy.data.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.corecoders.gymbuddy.data.AuthManager
import com.corecoders.gymbuddy.data.dto.UserProfile

class ProfileViewModel(
    private val database: com.corecoders.gymbuddy.data.AppDatabase,
    private val workoutDao: WorkoutDao,
    private val routineDao: RoutineDao,
    private val userPreferences: UserPreferences
): ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""

    init {
        viewModelScope.launch {
            AuthManager.currentUserIdFlow().collect { uid ->
                if (uid.isNotEmpty()) {
                    kotlinx.coroutines.withContext(Dispatchers.IO) {
                        workoutDao.assignOrphanWorkouts(uid)
                        routineDao.assignOrphanRoutines(uid)
                    }
                }
            }
        }
    }

    val userEmail: String = auth.currentUser?.email ?: "Unknown User"

    val allWorkouts: StateFlow<List<Workout>> = AuthManager.currentUserIdFlow().flatMapLatest { userId ->
        workoutDao.getAllWorkouts(userId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalWorkouts: StateFlow<Int> = allWorkouts
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val allRoutines: StateFlow<List<Routine>> = AuthManager.currentUserIdFlow().flatMapLatest { userId ->
        routineDao.getAllRoutines(userId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalVolume: StateFlow<Double> = AuthManager.currentUserIdFlow().flatMapLatest { userId ->
        workoutDao.getTotalVolume(userId).map { it ?: 0.0 }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val followers: StateFlow<List<UserProfile>> = AuthManager.currentUserIdFlow().flatMapLatest { userId ->
        if (userId.isNotEmpty()) {
            val repository = com.corecoders.gymbuddy.data.SocialRepository()
            repository.getFollowersFlow(userId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val following: StateFlow<List<UserProfile>> = AuthManager.currentUserIdFlow().flatMapLatest { userId ->
        if (userId.isNotEmpty()) {
            val repository = com.corecoders.gymbuddy.data.SocialRepository()
            repository.getFollowingFlow(userId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun loadFollowers() {
        // Handled automatically via Firestore snapshot flow
    }

    val age = userPreferences.ageFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    val weight = userPreferences.weightFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0f)
    val height = userPreferences.heightFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    val trainingFrequency = userPreferences.trainingFrequencyFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 4)
    val experienceLevel = userPreferences.experienceLevelFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")
    val fitnessGoal = userPreferences.fitnessGoalFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")
    val gender = userPreferences.genderFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")
    val targetWeight = userPreferences.targetWeightFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0f)
    val profilePictureUri = userPreferences.profilePictureUriFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")
    val bodyFat = userPreferences.bodyFatFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")
    val muscleMass = userPreferences.muscleMassFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")
    val waistSize = userPreferences.waistSizeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    fun updateBodyComposition(bodyFat: String?, muscleMass: String?, waistSize: String?) {
        viewModelScope.launch {
            userPreferences.updateBodyComposition(bodyFat, muscleMass, waistSize)
            val repository = com.corecoders.gymbuddy.data.SocialRepository()
            repository.updateBodyComposition(
                bodyFat = bodyFat,
                muscleMass = muscleMass,
                waistSize = waistSize
            )
        }
    }

    fun saveProfileData(
        age: Int,
        gender: String,
        weight: Float,
        targetWeight: Float,
        height: Int,
        fitnessGoal: String,
        experienceLevel: String,
        trainingFrequency: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            userPreferences.updateProfileData(
                age = age,
                weight = weight,
                height = height,
                targetWeight = targetWeight,
                trainingFrequency = trainingFrequency,
                fitnessGoal = fitnessGoal,
                experienceLevel = experienceLevel,
                gender = gender
            )
            userPreferences.updateOnboardingCompleted(true)
            
            val repository = com.corecoders.gymbuddy.data.SocialRepository()
            repository.saveOnboardingDetails(
                age = age,
                gender = gender,
                weight = weight,
                targetWeight = targetWeight,
                height = height,
                fitnessGoal = fitnessGoal,
                experienceLevel = experienceLevel,
                trainingFrequency = trainingFrequency
            )
            onSuccess()
        }
    }

    fun saveProfilePicture(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                if (originalBitmap != null) {
                    val maxDimension = 300
                    val width = originalBitmap.width
                    val height = originalBitmap.height
                    val newWidth: Int
                    val newHeight: Int
                    if (width > height) {
                        newWidth = maxDimension
                        newHeight = (height * maxDimension) / width
                    } else {
                        newHeight = maxDimension
                        newWidth = (width * maxDimension) / height
                    }
                    val resizedBitmap = android.graphics.Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
                    
                    val outputStream = java.io.ByteArrayOutputStream()
                    resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
                    val byteArray = outputStream.toByteArray()
                    val base64String = "data:image/jpeg;base64," + android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
                    
                    userPreferences.updateProfilePictureUri(base64String)
                    val repository = com.corecoders.gymbuddy.data.SocialRepository()
                    repository.updateAvatarUri(base64String)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeProfilePicture() {
        viewModelScope.launch {
            userPreferences.updateProfilePictureUri("")
            val repository = com.corecoders.gymbuddy.data.SocialRepository()
            repository.updateAvatarUri("")
        }
    }

    fun signOut() {
        viewModelScope.launch {
            auth.signOut()
            userPreferences.clearProfileData()
        }
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            user.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModelScope.launch {
                        userPreferences.clearProfileData()
                        kotlinx.coroutines.withContext(Dispatchers.IO) {
                            workoutDao.clearWorkouts()
                            workoutDao.clearWorkoutSets()
                            routineDao.clearRoutines()
                            routineDao.clearRoutineExercises()
                        }
                        onSuccess()
                    }
                } else {
                    onError(task.exception?.localizedMessage ?: "Failed to delete account.")
                }
            }
        } else {
            onError("No authenticated user found.")
        }
    }
}

class ProfileViewModelFactory(
    private val database: com.corecoders.gymbuddy.data.AppDatabase,
    private val workoutDao: WorkoutDao,
    private val routineDao: RoutineDao,
    private val userPreferences: UserPreferences
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(database, workoutDao, routineDao, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
