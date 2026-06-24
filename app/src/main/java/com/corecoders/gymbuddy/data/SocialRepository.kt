package com.corecoders.gymbuddy.data

import android.util.Log
import com.corecoders.gymbuddy.data.dto.SocialPostDto
import com.corecoders.gymbuddy.data.dto.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class SocialRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    suspend fun createOrUpdateProfile(username: String, name: String, avatarUri: String): Boolean {
        val userId = getCurrentUserId() ?: return false
        val email = auth.currentUser?.email ?: ""
        
        // Check if username is already taken by someone else
        val existing = firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .await()
            
        if (!existing.isEmpty) {
            val doc = existing.documents.first()
            if (doc.id != userId) {
                // Username taken by another user
                return false
            }
        }
        
        val userProfile = UserProfile(
            userId = userId,
            username = username.lowercase(),
            email = email,
            name = name,
            avatarUri = avatarUri
        )
        
        return try {
            firestore.collection("users").document(userId).set(userProfile).await()
            true
        } catch (e: Exception) {
            Log.e("SocialRepository", "Error updating profile", e)
            false
        }
    }

    suspend fun saveOnboardingDetails(
        age: Int,
        gender: String,
        weight: Float,
        targetWeight: Float,
        height: Int,
        fitnessGoal: String,
        experienceLevel: String,
        trainingFrequency: Int
    ): Boolean {
        val userId = getCurrentUserId() ?: return false
        return try {
            val docRef = firestore.collection("users").document(userId)
            val updates = mapOf(
                "age" to age,
                "gender" to gender,
                "weight" to weight,
                "targetWeight" to targetWeight,
                "height" to height,
                "fitnessGoal" to fitnessGoal,
                "experienceLevel" to experienceLevel,
                "trainingFrequency" to trainingFrequency,
                "onboardingCompleted" to true
            )
            docRef.update(updates).await()
            true
        } catch (e: Exception) {
            Log.e("SocialRepository", "Error updating onboarding details in firestore", e)
            false
        }
    }

    suspend fun updateBodyComposition(
        bodyFat: String?,
        muscleMass: String?,
        waistSize: String?
    ): Boolean {
        val userId = getCurrentUserId() ?: return false
        return try {
            val docRef = firestore.collection("users").document(userId)
            val updates = mutableMapOf<String, Any>()
            bodyFat?.let { updates["bodyFat"] = it }
            muscleMass?.let { updates["muscleMass"] = it }
            waistSize?.let { updates["waistSize"] = it }
            
            if (updates.isNotEmpty()) {
                docRef.update(updates).await()
            }
            true
        } catch (e: Exception) {
            Log.e("SocialRepository", "Error updating body composition in firestore", e)
            false
        }
    }

    suspend fun updateAvatarUri(avatarUri: String): Boolean {
        val userId = getCurrentUserId() ?: return false
        return try {
            val docRef = firestore.collection("users").document(userId)
            docRef.update("avatarUri", avatarUri).await()
            true
        } catch (e: Exception) {
            Log.e("SocialRepository", "Error updating avatarUri in firestore", e)
            false
        }
    }
    
    suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            doc.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun searchUsers(query: String): List<UserProfile> {
        return try {
            val currentUserId = getCurrentUserId() ?: return emptyList()
            val lowerQuery = query.lowercase()
            // We search by username prefix
            val result = firestore.collection("users")
                .whereGreaterThanOrEqualTo("username", lowerQuery)
                .whereLessThanOrEqualTo("username", lowerQuery + "\uf8ff")
                .get()
                .await()
                
            result.toObjects(UserProfile::class.java).filter { it.userId != currentUserId }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun addFriend(friendId: String): Boolean {
        val currentUserId = getCurrentUserId() ?: return false
        if (currentUserId == friendId) return false
        
        return try {
            val docRef = firestore.collection("users").document(currentUserId)
            val doc = docRef.get().await()
            val profile = doc.toObject(UserProfile::class.java) ?: return false
            
            val updatedFriends = profile.friends.toMutableList()
            if (!updatedFriends.contains(friendId)) {
                updatedFriends.add(friendId)
                docRef.update("friends", updatedFriends).await()
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun removeFriend(friendId: String): Boolean {
         val currentUserId = getCurrentUserId() ?: return false
         return try {
             val docRef = firestore.collection("users").document(currentUserId)
             val doc = docRef.get().await()
             val profile = doc.toObject(UserProfile::class.java) ?: return false
             
             val updatedFriends = profile.friends.toMutableList()
             if (updatedFriends.contains(friendId)) {
                 updatedFriends.remove(friendId)
                 docRef.update("friends", updatedFriends).await()
             }
             
             true
         } catch (e: Exception) {
             false
         }
    }
    
    suspend fun getFollowers(targetUserId: String? = null): List<UserProfile> {
        val uid = targetUserId ?: getCurrentUserId() ?: return emptyList()
        return try {
            val result = firestore.collection("users")
                .whereArrayContains("friends", uid)
                .get()
                .await()
            result.toObjects(UserProfile::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getFollowing(targetUserId: String? = null): List<UserProfile> {
        val uid = targetUserId ?: getCurrentUserId() ?: return emptyList()
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            val profile = doc.toObject(UserProfile::class.java) ?: return emptyList()
            val friendIds = profile.friends
            
            if (friendIds.isEmpty()) return emptyList()
            
            val followingList = mutableListOf<UserProfile>()
            for (id in friendIds) {
                val friendDoc = firestore.collection("users").document(id).get().await()
                friendDoc.toObject(UserProfile::class.java)?.let {
                    followingList.add(it)
                }
            }
            followingList
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getUserPosts(userId: String): List<SocialPostDto> {
        return try {
            val result = firestore.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            result.toObjects(SocialPostDto::class.java).sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            Log.e("SocialRepository", "Error getting user posts", e)
            emptyList()
        }
    }
    
    suspend fun publishPost(workoutName: String, stats: String, exercises: List<String>): Boolean {
        val currentUserId = getCurrentUserId() ?: return false
        val profile = getUserProfile(currentUserId) ?: return false
        
        val newDocRef = firestore.collection("posts").document()
        val post = SocialPostDto(
            postId = newDocRef.id,
            userId = currentUserId,
            username = profile.username,
            userAvatar = profile.avatarUri,
            workoutName = workoutName,
            timestamp = System.currentTimeMillis(),
            stats = stats,
            exercises = exercises
        )
        
        return try {
            newDocRef.set(post).await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getFeedPosts(): List<SocialPostDto> {
        val currentUserId = getCurrentUserId() ?: return emptyList()
        val profile = getUserProfile(currentUserId) ?: return emptyList()
        
        val friendIds = profile.friends.toMutableList()
        friendIds.add(currentUserId) // include own posts
        
        if (friendIds.isEmpty()) return emptyList()
        
        return try {
            // Firestore 'in' queries are limited to 10 items.
            // If the user has more than 9 friends, we'd need to batch this or change schema.
            // For now, take up to 10 to prevent crash.
            val limitedFriendIds = friendIds.take(10)
            
            val result = firestore.collection("posts")
                .whereIn("userId", limitedFriendIds)
                .get()
                .await()
                
            result.toObjects(SocialPostDto::class.java).sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            Log.e("SocialRepository", "Error getting feed", e)
            emptyList()
        }
    }
    
    suspend fun toggleClap(postId: String): Boolean {
        val currentUserId = getCurrentUserId() ?: return false
        return try {
            val docRef = firestore.collection("posts").document(postId)
            val doc = docRef.get().await()
            val post = doc.toObject(SocialPostDto::class.java) ?: return false
            
            val clappedBy = post.clappedBy.toMutableList()
            var claps = post.claps
            
            if (clappedBy.contains(currentUserId)) {
                clappedBy.remove(currentUserId)
                claps -= 1
            } else {
                clappedBy.add(currentUserId)
                claps += 1
            }
            
            docRef.update(
                mapOf(
                    "clappedBy" to clappedBy,
                    "claps" to claps
                )
            ).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
