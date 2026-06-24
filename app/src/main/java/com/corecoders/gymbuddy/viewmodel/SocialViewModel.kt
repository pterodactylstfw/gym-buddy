package com.corecoders.gymbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.SocialRepository
import com.corecoders.gymbuddy.data.dto.SocialPostDto
import com.corecoders.gymbuddy.data.dto.UserProfile
import kotlinx.coroutines.flow.*
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

    private val _comments = MutableStateFlow<List<com.corecoders.gymbuddy.data.dto.Comment>>(emptyList())
    val comments = _comments.asStateFlow()

    private val _notifications = MutableStateFlow<List<com.corecoders.gymbuddy.data.dto.NotificationDto>>(emptyList())
    val notifications = _notifications.asStateFlow()

    val unreadNotificationsCount = _notifications
        .map { list -> list.count { !it.read } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        viewModelScope.launch {
            com.corecoders.gymbuddy.data.AuthManager.currentUserIdFlow().collectLatest { uid ->
                if (uid.isNotEmpty()) {
                    loadCurrentUserProfile()
                    loadFeed()
                    
                    // Listen to notifications in real-time
                    repository.getNotificationsFlow().collect { list ->
                        _notifications.value = list
                    }
                } else {
                    _currentUserProfile.value = null
                    _feedPosts.value = emptyList()
                    _searchResults.value = emptyList()
                    _notifications.value = emptyList()
                }
            }
        }
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

    private var currentSearchQuery: String = ""

    fun searchUsers(query: String) {
        currentSearchQuery = query
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
                if (currentSearchQuery.isNotBlank()) {
                    searchUsers(currentSearchQuery)
                }
            }
        }
    }
    
    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            val success = repository.removeFriend(friendId)
            if (success) {
                loadCurrentUserProfile()
                loadFeed()
                if (currentSearchQuery.isNotBlank()) {
                    searchUsers(currentSearchQuery)
                }
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

    fun loadNotifications() {
        viewModelScope.launch {
            _notifications.value = repository.getNotifications()
        }
    }

    fun markNotificationsAsRead() {
        viewModelScope.launch {
            repository.markNotificationsAsRead()
            loadNotifications() // refresh
        }
    }

    fun loadComments(postId: String) {
        viewModelScope.launch {
            _comments.value = repository.getComments(postId)
        }
    }

    fun addComment(postId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val success = repository.addComment(postId, text)
            if (success) {
                loadComments(postId)
                // Also optimistically update local comments count in feed
                _feedPosts.value = _feedPosts.value.map { post ->
                    if (post.postId == postId) {
                        post.copy(comments = post.comments + 1)
                    } else {
                        post
                    }
                }
            }
        }
    }
}
