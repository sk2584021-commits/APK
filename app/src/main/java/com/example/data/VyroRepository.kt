package com.example.data

import kotlinx.coroutines.flow.Flow

class VyroRepository(private val dao: VyroDao) {
    // We can fetch the global feed or the personalized feed. For simplicity in MVP, we might just expose getFollowedPosts
    fun getFeedPosts(userId: Int): Flow<List<PostWithUser>> = dao.getFollowedPosts(userId)
    fun getAllFeedPosts(): Flow<List<PostWithUser>> = dao.getFeedPosts()

    // Users
    suspend fun insertUser(user: User): Long = dao.insertUser(user)
    suspend fun updateUser(user: User) = dao.updateUser(user)
    suspend fun getUserById(userId: Int): User? = dao.getUserById(userId)
    fun getUserByIdFlow(userId: Int): Flow<User?> = dao.getUserByIdFlow(userId)
    suspend fun getUserByUsername(username: String): User? = dao.getUserByUsername(username)
    fun searchUsers(query: String): Flow<List<User>> = dao.searchUsers(query)

    // Posts
    suspend fun insertPost(post: Post): Long = dao.insertPost(post)
    suspend fun deletePost(postId: Int) = dao.deletePost(postId)
    fun getPostsForUser(userId: Int): Flow<List<PostWithUser>> = dao.getPostsForUser(userId)
    fun searchPosts(query: String): Flow<List<PostWithUser>> = dao.searchPosts(query)
    fun getVideoPosts(): Flow<List<PostWithUser>> = dao.getVideoPosts()

    // Likes
    fun getUserLikes(userId: Int): Flow<List<Like>> = dao.getUserLikes(userId)
    suspend fun toggleLike(userId: Int, postId: Int, postOwnerId: Int) {
        val inserted = dao.insertLike(Like(userId, postId))
        if (inserted != -1L) {
            dao.incrementPostLikes(postId)
            if (userId != postOwnerId) {
                dao.insertNotification(Notification(userId = postOwnerId, senderId = userId, type = "LIKE", postId = postId, text = "liked your post"))
            }
        } else {
            dao.deleteLike(Like(userId, postId))
            dao.decrementPostLikes(postId)
        }
    }

    // Comments
    fun getCommentsForPost(postId: Int): Flow<List<CommentWithUser>> = dao.getCommentsWithUserForPost(postId)
    suspend fun addComment(userId: Int, postId: Int, postOwnerId: Int, text: String) {
        dao.insertComment(Comment(userId = userId, postId = postId, text = text))
        dao.incrementPostComments(postId)
        if (userId != postOwnerId) {
            dao.insertNotification(Notification(userId = postOwnerId, senderId = userId, type = "COMMENT", postId = postId, text = text))
        }
    }
    suspend fun deleteComment(commentId: Int, userId: Int, postId: Int) {
        dao.deleteComment(commentId, userId)
        dao.decrementPostComments(postId)
    }

    // Follows
    fun getUserFollowing(userId: Int): Flow<List<Int>> = dao.getUserFollowing(userId)
    suspend fun toggleFollow(followerId: Int, followingId: Int) {
        val inserted = dao.insertFollow(Follow(followerId, followingId))
        if (inserted != -1L) {
            dao.incrementFollowing(followerId)
            dao.incrementFollowers(followingId)
            dao.insertNotification(Notification(userId = followingId, senderId = followerId, type = "FOLLOW", text = "started following you"))
        } else {
            dao.deleteFollow(Follow(followerId, followingId))
            dao.decrementFollowing(followerId)
            dao.decrementFollowers(followingId)
        }
    }

    // Notifications
    fun getUserNotifications(userId: Int): Flow<List<NotificationWithUser>> = dao.getUserNotifications(userId)

    suspend fun markNotificationAsRead(notificationId: Int) = dao.markNotificationAsRead(notificationId)
    suspend fun markAllNotificationsAsRead(userId: Int) = dao.markAllNotificationsAsRead(userId)

    // Chat
    fun getUserConversations(userId: Int): Flow<List<ConversationWithOtherUser>> = dao.getUserConversations(userId)
    
    suspend fun getOrCreateConversation(currentUserId: Int, otherUserId: Int): Int {
        val existing = dao.getConversationBetweenUsers(currentUserId, otherUserId)
        if (existing != null) return existing.id
        
        val newId = dao.insertConversation(Conversation(user1Id = currentUserId, user2Id = otherUserId))
        return newId.toInt()
    }
    
    fun getMessagesForConversation(conversationId: Int): Flow<List<Message>> = dao.getMessagesForConversation(conversationId)
    
    suspend fun sendMessage(conversationId: Int, senderId: Int, receiverId: Int, text: String) {
        val message = Message(conversationId = conversationId, senderId = senderId, text = text)
        dao.insertMessage(message)
        dao.updateConversationLastMessage(conversationId, text, System.currentTimeMillis())
        
        // Local notification
        dao.insertNotification(Notification(
            userId = receiverId,
            senderId = senderId,
            type = "MESSAGE",
            text = "Sent you a message: $text"
        ))
    }
    
    suspend fun markMessagesAsRead(conversationId: Int, currentUserId: Int) = dao.markMessagesAsRead(conversationId, currentUserId)
    
    suspend fun deleteMessage(messageId: Int, userId: Int) = dao.deleteMessage(messageId, userId)

    // Blocks
    fun getBlockedUsers(userId: Int): Flow<List<Int>> = dao.getBlockedUsers(userId)
    fun getUsersWhoBlocked(userId: Int): Flow<List<Int>> = dao.getUsersWhoBlocked(userId)
    
    suspend fun toggleBlock(blockerId: Int, blockedId: Int) {
        // Simple toggle logic using try-catch for uniqueness exception or just check existence
        // Actually, we'll just check if they are in the blocked list in the ViewModel or here.
        // For simplicity, we can do a try insert, and if it fails, delete it.
        try {
            dao.insertBlock(Block(blockerId, blockedId))
            // When blocked, unfollow each other to be safe
            dao.deleteFollow(Follow(blockerId, blockedId))
            dao.deleteFollow(Follow(blockedId, blockerId))
            dao.decrementFollowing(blockerId)
            dao.decrementFollowers(blockedId)
            dao.decrementFollowing(blockedId)
            dao.decrementFollowers(blockerId)
        } catch (e: Exception) {
            dao.deleteBlock(Block(blockerId, blockedId))
        }
    }

    // Saved Posts
    fun getSavedPostIds(userId: Int): Flow<List<Int>> = dao.getSavedPostIds(userId)
    fun getSavedPosts(userId: Int): Flow<List<PostWithUser>> = dao.getSavedPosts(userId)
    
    suspend fun toggleSavePost(userId: Int, postId: Int) {
        try {
            dao.insertSavedPost(SavedPost(userId, postId))
        } catch (e: Exception) {
            dao.deleteSavedPost(SavedPost(userId, postId))
        }
    }

    // Reports
    suspend fun submitReport(reporterId: Int, reportedId: Int, type: String, reason: String) {
        dao.insertReport(Report(reporterId = reporterId, reportedId = reportedId, type = type, reason = reason))
    }
}
