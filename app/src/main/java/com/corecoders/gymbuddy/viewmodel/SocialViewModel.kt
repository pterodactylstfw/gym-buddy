package com.corecoders.gymbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.SocialRepository
import com.corecoders.gymbuddy.data.dto.SocialPostDto
import com.corecoders.gymbuddy.data.dto.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SocialViewModel : ViewModel() {
    private val repository = SocialRepository()

    private val _feedPosts = MutableStateFlow<List<SocialPostDto>>(emptyList())
    val feedPosts = _feedPosts.asStateFlow()

    private val _searchResults = MutableStateFlow<List<UserProfile>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile = _currentUserProfile.asStateFlow()

    init {
        loadFeed()
        loadCurrentUserProfile()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _isLoading.value = true
            _feedPosts.value = repository.getFeedPosts()
            _isLoading.value = false
        }
    }
    
    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            val userId = repository.getCurrentUserId()
            if (userId != null) {
                _currentUserProfile.value = repository.getUserProfile(userId)
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _searchResults.value = repository.searchUsers(query)
            _isLoading.value = false
        }
    }

    fun addFriend(friendId: String) {
        viewModelScope.launch {
            val success = repository.addFriend(friendId)
            if (success) {
                loadCurrentUserProfile()
                loadFeed() // feed might change if we added a friend
            }
        }
    }
    
    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            val success = repository.removeFriend(friendId)
            if (success) {
                loadCurrentUserProfile()
                loadFeed()
            }
        }
    }

    fun toggleClap(postId: String) {
        viewModelScope.launch {
            // Optimistic update
            val currentUserId = repository.getCurrentUserId() ?: return@launch
            val updatedPosts = _feedPosts.value.map { post ->
                if (post.postId == postId) {
                    val clappedBy = post.clappedBy.toMutableList()
                    var claps = post.claps
                    if (clappedBy.contains(currentUserId)) {
                        clappedBy.remove(currentUserId)
                        claps--
                    } else {
                        clappedBy.add(currentUserId)
                        claps++
                    }
                    post.copy(clappedBy = clappedBy, claps = claps)
                } else {
                    post
                }
            }
            _feedPosts.value = updatedPosts

            repository.toggleClap(postId)
        }
    }
}
