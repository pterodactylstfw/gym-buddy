package com.corecoders.gymbuddy.data

import android.util.Log
import com.corecoders.gymbuddy.data.dto.SocialPostDto
import com.corecoders.gymbuddy.data.dto.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch

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
                
                // Trigger follower notification
                createNotification(
                    recipientUserId = friendId,
                    senderUserId = currentUserId,
                    type = "FOLLOW"
                )
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
            var isClapping = false
            
            if (clappedBy.contains(currentUserId)) {
                clappedBy.remove(currentUserId)
                claps -= 1
            } else {
                clappedBy.add(currentUserId)
                claps += 1
                isClapping = true
            }
            
            docRef.update(
                mapOf(
                    "clappedBy" to clappedBy,
                    "claps" to claps
                )
            ).await()
            
            if (isClapping && post.userId != currentUserId) {
                createNotification(
                    recipientUserId = post.userId,
                    senderUserId = currentUserId,
                    type = "CLAP",
                    postId = postId,
                    postWorkoutName = post.workoutName
                )
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun createNotification(
        recipientUserId: String,
        senderUserId: String,
        type: String,
        postId: String = "",
        postWorkoutName: String = ""
    ): Boolean {
        if (recipientUserId == senderUserId) return true
        return try {
            val senderProfile = getUserProfile(senderUserId) ?: return false
            val notifRef = firestore.collection("users")
                .document(recipientUserId)
                .collection("notifications")
                .document()
            
            val notification = com.corecoders.gymbuddy.data.dto.NotificationDto(
                notificationId = notifRef.id,
                recipientUserId = recipientUserId,
                senderUserId = senderUserId,
                senderUsername = senderProfile.username,
                senderAvatar = senderProfile.avatarUri,
                type = type,
                postId = postId,
                postWorkoutName = postWorkoutName,
                timestamp = System.currentTimeMillis(),
                read = false
            )
            notifRef.set(notification).await()
            true
        } catch (e: Exception) {
            Log.e("SocialRepository", "Error creating notification", e)
            false
        }
    }

    suspend fun getNotifications(): List<com.corecoders.gymbuddy.data.dto.NotificationDto> {
        val currentUserId = getCurrentUserId() ?: return emptyList()
        return try {
            val result = firestore.collection("users")
                .document(currentUserId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            result.toObjects(com.corecoders.gymbuddy.data.dto.NotificationDto::class.java)
        } catch (e: Exception) {
            Log.e("SocialRepository", "Error getting notifications", e)
            emptyList()
        }
    }

    suspend fun markNotificationsAsRead(): Boolean {
        val currentUserId = getCurrentUserId() ?: return false
        return try {
            val unreadNotifs = firestore.collection("users")
                .document(currentUserId)
                .collection("notifications")
                .whereEqualTo("read", false)
                .get()
                .await()
                
            if (!unreadNotifs.isEmpty) {
                firestore.runBatch { batch ->
                    for (doc in unreadNotifs.documents) {
                        batch.update(doc.reference, "read", true)
                    }
                }.await()
            }
            true
        } catch (e: Exception) {
            Log.e("SocialRepository", "Error marking notifications as read", e)
            false
        }
    }

    suspend fun addComment(postId: String, text: String): Boolean {
        val currentUserId = getCurrentUserId() ?: return false
        val profile = getUserProfile(currentUserId) ?: return false
        
        return try {
            val postRef = firestore.collection("posts").document(postId)
            val postDoc = postRef.get().await()
            val post = postDoc.toObject(SocialPostDto::class.java) ?: return false
            
            val commentsRef = postRef.collection("comments").document()
            val comment = com.corecoders.gymbuddy.data.dto.Comment(
                commentId = commentsRef.id,
                postId = postId,
                userId = currentUserId,
                username = profile.username,
                userAvatar = profile.avatarUri,
                text = text,
                timestamp = System.currentTimeMillis()
            )
            
            commentsRef.set(comment).await()
            postRef.update("comments", post.comments + 1).await()
            
            if (post.userId != currentUserId) {
                createNotification(
                    recipientUserId = post.userId,
                    senderUserId = currentUserId,
                    type = "COMMENT",
                    postId = postId,
                    postWorkoutName = post.workoutName
                )
            }
            true
        } catch (e: Exception) {
            Log.e("SocialRepository", "Error adding comment", e)
            false
        }
    }

    suspend fun getComments(postId: String): List<com.corecoders.gymbuddy.data.dto.Comment> {
        return try {
            val result = firestore.collection("posts")
                .document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()
            result.toObjects(com.corecoders.gymbuddy.data.dto.Comment::class.java)
        } catch (e: Exception) {
            Log.e("SocialRepository", "Error getting comments", e)
            emptyList()
        }
    }

    fun getFollowersFlow(targetUserId: String? = null): Flow<List<UserProfile>> = callbackFlow {
        val uid = targetUserId ?: getCurrentUserId()
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val listener = firestore.collection("users")
            .whereArrayContains("friends", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SocialRepository", "Error listening to followers", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val followersList = snapshot.toObjects(UserProfile::class.java)
                    trySend(followersList)
                }
            }
            
        awaitClose {
            listener.remove()
        }
    }

    fun getFollowingFlow(targetUserId: String? = null): Flow<List<UserProfile>> = callbackFlow {
        val uid = targetUserId ?: getCurrentUserId()
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val listener = firestore.collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SocialRepository", "Error listening to following", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val profile = snapshot.toObject(UserProfile::class.java)
                    val friendIds = profile?.friends ?: emptyList()
                    
                    if (friendIds.isEmpty()) {
                        trySend(emptyList())
                    } else {
                        launch {
                            try {
                                val followingList = mutableListOf<UserProfile>()
                                for (id in friendIds) {
                                    val friendDoc = firestore.collection("users").document(id).get().await()
                                    friendDoc.toObject(UserProfile::class.java)?.let {
                                        followingList.add(it)
                                    }
                                }
                                trySend(followingList)
                            } catch (e: Exception) {
                                Log.e("SocialRepository", "Error fetching following profiles", e)
                            }
                        }
                    }
                }
            }
            
        awaitClose {
            listener.remove()
        }
    }

    fun getNotificationsFlow(): Flow<List<com.corecoders.gymbuddy.data.dto.NotificationDto>> = callbackFlow {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val listener = firestore.collection("users")
            .document(currentUserId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SocialRepository", "Error listening to notifications", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val notificationsList = snapshot.toObjects(com.corecoders.gymbuddy.data.dto.NotificationDto::class.java)
                    trySend(notificationsList)
                }
            }
            
        awaitClose {
            listener.remove()
        }
    }

    suspend fun deleteUserData(userId: String): Boolean {
        return try {
            val batch = firestore.batch()

            // 1. Delete user profile document
            val userRef = firestore.collection("users").document(userId)
            batch.delete(userRef)

            // 2. Delete posts published by the user
            val postsResult = firestore.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            for (doc in postsResult.documents) {
                batch.delete(doc.reference)
            }

            // 3. Remove user ID from any other user's "friends" array
            val friendsResult = firestore.collection("users")
                .whereArrayContains("friends", userId)
                .get()
                .await()
            for (doc in friendsResult.documents) {
                val friendsList = doc.get("friends") as? List<*> ?: emptyList<Any>()
                val updatedFriends = friendsList.filter { it != userId }
                batch.update(doc.reference, "friends", updatedFriends)
            }

            // 4. Delete notifications under users/{userId}/notifications
            val notificationsResult = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .get()
                .await()
            for (doc in notificationsResult.documents) {
                batch.delete(doc.reference)
            }

            batch.commit().await()
            true
        } catch (e: Exception) {
            Log.e("SocialRepository", "Error deleting user data from Firestore", e)
            false
        }
    }
}
