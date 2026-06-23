package com.corecoders.gymbuddy.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

object AuthManager {
    fun currentUserIdFlow(): Flow<String> = callbackFlow {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid ?: "")
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser?.uid ?: "")
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()
}
