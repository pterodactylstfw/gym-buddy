package com.corecoders.gymbuddy.data.dto

data class UserProfile(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val name: String = "",
    val avatarUri: String = "",
    val friends: List<String> = emptyList(),
    val onboardingCompleted: Boolean = false,
    val age: Int = 0,
    val weight: Float = 0f,
    val height: Int = 0,
    val targetWeight: Float = 0f,
    val trainingFrequency: Int = 0,
    val fitnessGoal: String = "",
    val experienceLevel: String = "",
    val gender: String = "",
    val bodyFat: String = "",
    val muscleMass: String = "",
    val waistSize: String = ""
)

data class SocialPostDto(
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val userAvatar: String = "",
    val workoutName: String = "",
    val timestamp: Long = 0L,
    val stats: String = "",
    val exercises: List<String> = emptyList(),
    val claps: Int = 0,
    val comments: Int = 0,
    val clappedBy: List<String> = emptyList()
)
