package com.corecoders.gymbuddy.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {

    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val UNIT_SYSTEM_METRIC = booleanPreferencesKey("unit_system_metric")
        val AUTO_COMPLETE_SET = booleanPreferencesKey("auto_complete_set")
        
        // Onboarding / Profile fields
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val AGE = intPreferencesKey("age")
        val WEIGHT = floatPreferencesKey("weight")
        val HEIGHT = intPreferencesKey("height")
        val TARGET_WEIGHT = floatPreferencesKey("target_weight")
        val TRAINING_FREQUENCY = intPreferencesKey("training_frequency")
        val FITNESS_GOAL = stringPreferencesKey("fitness_goal")
        val EXPERIENCE_LEVEL = stringPreferencesKey("experience_level")
        val GENDER = stringPreferencesKey("gender")
    }

    private fun <T> getFlow(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                preferences[key] ?: defaultValue
            }
    }

    val darkModeFlow: Flow<Boolean> = getFlow(PreferencesKeys.DARK_MODE, true)
    val unitSystemMetricFlow: Flow<Boolean> = getFlow(PreferencesKeys.UNIT_SYSTEM_METRIC, true)
    val autoCompleteSetFlow: Flow<Boolean> = getFlow(PreferencesKeys.AUTO_COMPLETE_SET, true)
    val onboardingCompletedFlow: Flow<Boolean> = getFlow(PreferencesKeys.ONBOARDING_COMPLETED, false)

    val ageFlow: Flow<Int> = getFlow(PreferencesKeys.AGE, 0)
    val weightFlow: Flow<Float> = getFlow(PreferencesKeys.WEIGHT, 0f)
    val heightFlow: Flow<Int> = getFlow(PreferencesKeys.HEIGHT, 0)
    val targetWeightFlow: Flow<Float> = getFlow(PreferencesKeys.TARGET_WEIGHT, 0f)
    val trainingFrequencyFlow: Flow<Int> = getFlow(PreferencesKeys.TRAINING_FREQUENCY, 0)
    val fitnessGoalFlow: Flow<String> = getFlow(PreferencesKeys.FITNESS_GOAL, "")
    val experienceLevelFlow: Flow<String> = getFlow(PreferencesKeys.EXPERIENCE_LEVEL, "")
    val genderFlow: Flow<String> = getFlow(PreferencesKeys.GENDER, "")

    suspend fun updateDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DARK_MODE] = enabled }
    }

    suspend fun updateUnitSystem(metric: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.UNIT_SYSTEM_METRIC] = metric }
    }

    suspend fun updateAutoCompleteSet(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_COMPLETE_SET] = enabled }
    }
    
    suspend fun updateOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun updateProfileData(
        age: Int? = null,
        weight: Float? = null,
        height: Int? = null,
        targetWeight: Float? = null,
        trainingFrequency: Int? = null,
        fitnessGoal: String? = null,
        experienceLevel: String? = null,
        gender: String? = null
    ) {
        context.dataStore.edit { prefs ->
            age?.let { prefs[PreferencesKeys.AGE] = it }
            weight?.let { prefs[PreferencesKeys.WEIGHT] = it }
            height?.let { prefs[PreferencesKeys.HEIGHT] = it }
            targetWeight?.let { prefs[PreferencesKeys.TARGET_WEIGHT] = it }
            trainingFrequency?.let { prefs[PreferencesKeys.TRAINING_FREQUENCY] = it }
            fitnessGoal?.let { prefs[PreferencesKeys.FITNESS_GOAL] = it }
            experienceLevel?.let { prefs[PreferencesKeys.EXPERIENCE_LEVEL] = it }
            gender?.let { prefs[PreferencesKeys.GENDER] = it }
        }
    }

    suspend fun clearProfileData() {
        context.dataStore.edit { prefs ->
            prefs.remove(PreferencesKeys.ONBOARDING_COMPLETED)
            prefs.remove(PreferencesKeys.AGE)
            prefs.remove(PreferencesKeys.WEIGHT)
            prefs.remove(PreferencesKeys.HEIGHT)
            prefs.remove(PreferencesKeys.TARGET_WEIGHT)
            prefs.remove(PreferencesKeys.TRAINING_FREQUENCY)
            prefs.remove(PreferencesKeys.FITNESS_GOAL)
            prefs.remove(PreferencesKeys.EXPERIENCE_LEVEL)
            prefs.remove(PreferencesKeys.GENDER)
        }
    }
}
