package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class VyroViewModel(private val repository: VyroRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val feedPosts: Flow<List<PostWithUser>> = _currentUser.flatMapLatest { user ->
        if (user == null) emptyFlow() else repository.getFeedPosts(user.id)
    }
    
    val userLikes: Flow<Set<Int>> = _currentUser.flatMapLatest { user ->
        if (user == null) emptyFlow() else repository.getUserLikes(user.id).map { likes -> likes.map { it.postId }.toSet() }
    }

    val userFollowing: Flow<Set<Int>> = _currentUser.flatMapLatest { user ->
        if (user == null) emptyFlow() else repository.getUserFollowing(user.id).map { it.toSet() }
    }

    val myPosts: Flow<List<PostWithUser>> = _currentUser.flatMapLatest { user ->
        if (user == null) emptyFlow() else repository.getPostsForUser(user.id)
    }

    val notifications: Flow<List<NotificationWithUser>> = _currentUser.flatMapLatest { user ->
        if (user == null) emptyFlow() else repository.getUserNotifications(user.id)
    }

    fun markNotificationAsRead(notificationId: Int) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notificationId)
        }
    }

    fun markAllNotificationsAsRead() {
        val currentUserId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(currentUserId)
        }
    }

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResultsUsers: Flow<List<User>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) emptyFlow() else repository.searchUsers(query)
    }

    val searchResultsPosts: Flow<List<PostWithUser>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) emptyFlow() else repository.searchPosts(query)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val videoPosts: Flow<List<PostWithUser>> = repository.getVideoPosts()

    fun getProfileUser(userId: Int): Flow<User?> = repository.getUserByIdFlow(userId)

    fun getPostsForUser(userId: Int): Flow<List<PostWithUser>> = repository.getPostsForUser(userId)

    fun login(username: String) {
        viewModelScope.launch {
            val user = repository.getUserByUsername(username)
            if (user != null) {
                _currentUser.value = user
            } else {
                val newUser = User(username = username, fullName = username)
                val id = repository.insertUser(newUser)
                _currentUser.value = newUser.copy(id = id.toInt())
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }
    
    fun updateProfile(username: String, bio: String, profilePicUrl: String?) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updatedUser = user.copy(username = username, bio = bio, profilePicUrl = profilePicUrl)
            repository.updateUser(updatedUser)
            _currentUser.value = repository.getUserById(user.id)
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            repository.updateUser(user)
            if (_currentUser.value?.id == user.id) {
                _currentUser.value = repository.getUserById(user.id)
            }
        }
    }

    fun createPost(caption: String, hashtags: String, imageUrl: String? = null, isVideo: Boolean = false) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.insertPost(
                Post(userId = user.id, caption = caption, hashtags = hashtags, imageUrl = imageUrl, isVideo = isVideo)
            )
        }
    }
    
    fun deletePost(postId: Int) {
        viewModelScope.launch {
            repository.deletePost(postId)
        }
    }

    fun toggleLike(postId: Int, postOwnerId: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.toggleLike(userId = user.id, postId = postId, postOwnerId = postOwnerId)
        }
    }

    fun toggleFollow(followingId: Int) {
        val user = _currentUser.value ?: return
        if (user.id == followingId) return
        viewModelScope.launch {
            repository.toggleFollow(followerId = user.id, followingId = followingId)
            _currentUser.value = repository.getUserById(user.id)
        }
    }

    fun getCommentsForPost(postId: Int): Flow<List<CommentWithUser>> {
        return repository.getCommentsForPost(postId)
    }

    fun addComment(postId: Int, postOwnerId: Int, text: String) {
        val user = _currentUser.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addComment(userId = user.id, postId = postId, postOwnerId = postOwnerId, text = text)
        }
    }
    
    fun deleteComment(commentId: Int, postId: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteComment(commentId = commentId, userId = user.id, postId = postId)
        }
    }

    // --- CHAT & BLOCKS ---
    val conversations: Flow<List<com.example.data.ConversationWithOtherUser>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getUserConversations(user.id) else flowOf(emptyList())
    }
    
    val blockedUsers: Flow<List<Int>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getBlockedUsers(user.id) else flowOf(emptyList())
    }
    
    val usersWhoBlocked: Flow<List<Int>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getUsersWhoBlocked(user.id) else flowOf(emptyList())
    }

    fun getMessagesForConversation(conversationId: Int): Flow<List<com.example.data.Message>> = repository.getMessagesForConversation(conversationId)

    suspend fun getOrCreateConversation(otherUserId: Int): Int {
        val currentUserId = _currentUser.value?.id ?: return -1
        return repository.getOrCreateConversation(currentUserId, otherUserId)
    }

    fun sendMessage(conversationId: Int, receiverId: Int, text: String) {
        val currentUserId = _currentUser.value?.id ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(conversationId, currentUserId, receiverId, text)
        }
    }

    fun markMessagesAsRead(conversationId: Int) {
        val currentUserId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.markMessagesAsRead(conversationId, currentUserId)
        }
    }

    fun deleteMessage(messageId: Int) {
        val currentUserId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.deleteMessage(messageId, currentUserId)
        }
    }
    
    fun toggleBlock(blockedId: Int) {
        val currentUserId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.toggleBlock(currentUserId, blockedId)
        }
    }

    // --- SAVED POSTS & REPORTS ---
    val savedPostIds: Flow<List<Int>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getSavedPostIds(user.id) else flowOf(emptyList())
    }
    
    val savedPosts: Flow<List<com.example.data.PostWithUser>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getSavedPosts(user.id) else flowOf(emptyList())
    }

    fun toggleSavePost(postId: Int) {
        val currentUserId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.toggleSavePost(currentUserId, postId)
        }
    }

    fun submitReport(reportedId: Int, type: String, reason: String) {
        val currentUserId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.submitReport(reporterId = currentUserId, reportedId = reportedId, type = type, reason = reason)
        }
    }
}

class VyroViewModelFactory(private val repository: VyroRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VyroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VyroViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
