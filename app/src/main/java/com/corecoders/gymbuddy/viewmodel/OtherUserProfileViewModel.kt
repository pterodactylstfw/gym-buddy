package com.corecoders.gymbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.SocialRepository
import com.corecoders.gymbuddy.data.dto.SocialPostDto
import com.corecoders.gymbuddy.data.dto.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OtherUserProfileViewModel : ViewModel() {
    private val repository = SocialRepository()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _posts = MutableStateFlow<List<SocialPostDto>>(emptyList())
    val posts = _posts.asStateFlow()

    private val _followers = MutableStateFlow<List<UserProfile>>(emptyList())
    val followers = _followers.asStateFlow()

    private val _following = MutableStateFlow<List<UserProfile>>(emptyList())
    val following = _following.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()
    
    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile = _currentUserProfile.asStateFlow()

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            val currentId = repository.getCurrentUserId()
            if (currentId != null) {
                _currentUserProfile.value = repository.getUserProfile(currentId)
            }
            
            _userProfile.value = repository.getUserProfile(userId)
            _posts.value = repository.getUserPosts(userId)
            _followers.value = repository.getFollowers(userId)
            _following.value = repository.getFollowing(userId)
            
            _isLoading.value = false
        }
    }

    fun toggleFollowStatus(targetUserId: String, isCurrentlyFollowing: Boolean) {
        viewModelScope.launch {
            if (isCurrentlyFollowing) {
                val success = repository.removeFriend(targetUserId)
                if (success) {
                    _followers.value = repository.getFollowers(targetUserId)
                    // Reload current user to update amIFollowingThem
                    val currentId = repository.getCurrentUserId()
                    if (currentId != null) {
                        _currentUserProfile.value = repository.getUserProfile(currentId)
                    }
                }
            } else {
                val success = repository.addFriend(targetUserId)
                if (success) {
                    _followers.value = repository.getFollowers(targetUserId)
                    val currentId = repository.getCurrentUserId()
                    if (currentId != null) {
                        _currentUserProfile.value = repository.getUserProfile(currentId)
                    }
                }
            }
        }
    }
    
    fun toggleClap(postId: String) {
        viewModelScope.launch {
            val currentUserId = repository.getCurrentUserId() ?: return@launch
            val updatedPosts = _posts.value.map { post ->
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
            _posts.value = updatedPosts

            repository.toggleClap(postId)
        }
    }
}
