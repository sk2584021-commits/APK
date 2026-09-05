package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VyroDao {
    // --- USERS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?
    
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdFlow(userId: Int): Flow<User?>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%'")
    fun searchUsers(query: String): Flow<List<User>>

    // --- POSTS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post): Long

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: Int)

    @Transaction
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getFeedPosts(): Flow<List<PostWithUser>>

    @Transaction
    @Query("SELECT * FROM posts WHERE (userId IN (SELECT followingId FROM follows WHERE followerId = :userId) OR userId = :userId) AND isVideo = 0 ORDER BY timestamp DESC")
    fun getFollowedPosts(userId: Int): Flow<List<PostWithUser>>

    @Transaction
    @Query("SELECT * FROM posts WHERE isVideo = 1 ORDER BY timestamp DESC")
    fun getVideoPosts(): Flow<List<PostWithUser>>

    @Transaction
    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY timestamp DESC")
    fun getPostsForUser(userId: Int): Flow<List<PostWithUser>>

    @Transaction
    @Query("SELECT * FROM posts WHERE caption LIKE '%' || :query || '%' OR hashtags LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchPosts(query: String): Flow<List<PostWithUser>>

    // --- LIKES ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLike(like: Like): Long

    @Delete
    suspend fun deleteLike(like: Like)
    
    @Query("SELECT * FROM likes WHERE userId = :userId")
    fun getUserLikes(userId: Int): Flow<List<Like>>

    @Query("UPDATE posts SET likesCount = likesCount + 1 WHERE id = :postId")
    suspend fun incrementPostLikes(postId: Int)

    @Query("UPDATE posts SET likesCount = likesCount - 1 WHERE id = :postId")
    suspend fun decrementPostLikes(postId: Int)

    // --- COMMENTS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    @Query("DELETE FROM comments WHERE id = :commentId AND userId = :userId")
    suspend fun deleteComment(commentId: Int, userId: Int)

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: Int): Flow<List<Comment>>

    @Query("UPDATE posts SET commentsCount = commentsCount + 1 WHERE id = :postId")
    suspend fun incrementPostComments(postId: Int)

    @Query("UPDATE posts SET commentsCount = commentsCount - 1 WHERE id = :postId")
    suspend fun decrementPostComments(postId: Int)
    
    @Query("SELECT comments.*, users.username, users.profilePicUrl FROM comments INNER JOIN users ON comments.userId = users.id WHERE comments.postId = :postId ORDER BY comments.timestamp ASC")
    fun getCommentsWithUserForPost(postId: Int): Flow<List<CommentWithUser>>

    // --- FOLLOWS ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFollow(follow: Follow): Long

    @Delete
    suspend fun deleteFollow(follow: Follow)
    
    @Query("SELECT followingId FROM follows WHERE followerId = :userId")
    fun getUserFollowing(userId: Int): Flow<List<Int>>

    @Query("UPDATE users SET followersCount = followersCount + 1 WHERE id = :userId")
    suspend fun incrementFollowers(userId: Int)
    
    @Query("UPDATE users SET followersCount = followersCount - 1 WHERE id = :userId")
    suspend fun decrementFollowers(userId: Int)
    
    @Query("UPDATE users SET followingCount = followingCount + 1 WHERE id = :userId")
    suspend fun incrementFollowing(userId: Int)
    
    @Query("UPDATE users SET followingCount = followingCount - 1 WHERE id = :userId")
    suspend fun decrementFollowing(userId: Int)

    // --- NOTIFICATIONS ---
    @Insert
    suspend fun insertNotification(notification: Notification)

    @Query("SELECT notifications.*, users.username, users.profilePicUrl FROM notifications INNER JOIN users ON notifications.senderId = users.id WHERE notifications.userId = :userId ORDER BY notifications.timestamp DESC")
    fun getUserNotifications(userId: Int): Flow<List<NotificationWithUser>>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markNotificationAsRead(notificationId: Int)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsAsRead(userId: Int)

    // --- CHAT ---
    @Query("""
        SELECT 
            c.*, 
            u.id AS otherUserId, 
            u.username AS otherUsername, 
            u.profilePicUrl AS otherUserProfilePic,
            (SELECT COUNT(*) FROM messages WHERE conversationId = c.id AND senderId != :userId AND isRead = 0) AS unreadCount
        FROM conversations c
        INNER JOIN users u ON u.id = CASE WHEN c.user1Id = :userId THEN c.user2Id ELSE c.user1Id END
        WHERE c.user1Id = :userId OR c.user2Id = :userId
        ORDER BY c.lastMessageTimestamp DESC
    """)
    fun getUserConversations(userId: Int): Flow<List<ConversationWithOtherUser>>

    @Query("SELECT * FROM conversations WHERE (user1Id = :user1Id AND user2Id = :user2Id) OR (user1Id = :user2Id AND user2Id = :user1Id) LIMIT 1")
    suspend fun getConversationBetweenUsers(user1Id: Int, user2Id: Int): Conversation?

    @Insert
    suspend fun insertConversation(conversation: Conversation): Long

    @Query("UPDATE conversations SET lastMessageText = :text, lastMessageTimestamp = :timestamp WHERE id = :conversationId")
    suspend fun updateConversationLastMessage(conversationId: Int, text: String, timestamp: Long)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: Int): Flow<List<Message>>

    @Insert
    suspend fun insertMessage(message: Message): Long

    @Query("UPDATE messages SET isRead = 1 WHERE conversationId = :conversationId AND senderId != :currentUserId")
    suspend fun markMessagesAsRead(conversationId: Int, currentUserId: Int)

    @Query("DELETE FROM messages WHERE id = :messageId AND senderId = :userId")
    suspend fun deleteMessage(messageId: Int, userId: Int)

    // --- BLOCKS ---
    @Insert
    suspend fun insertBlock(block: Block)

    @Delete
    suspend fun deleteBlock(block: Block)

    @Query("SELECT blockedId FROM blocks WHERE blockerId = :userId")
    fun getBlockedUsers(userId: Int): Flow<List<Int>>

    @Query("SELECT blockerId FROM blocks WHERE blockedId = :userId")
    fun getUsersWhoBlocked(userId: Int): Flow<List<Int>>

    // --- SAVED POSTS ---
    @Insert
    suspend fun insertSavedPost(savedPost: SavedPost)

    @Delete
    suspend fun deleteSavedPost(savedPost: SavedPost)

    @Query("SELECT postId FROM saved_posts WHERE userId = :userId")
    fun getSavedPostIds(userId: Int): Flow<List<Int>>

    @Transaction
    @Query("SELECT * FROM posts WHERE id IN (SELECT postId FROM saved_posts WHERE userId = :userId) ORDER BY timestamp DESC")
    fun getSavedPosts(userId: Int): Flow<List<PostWithUser>>

    // --- REPORTS ---
    @Insert
    suspend fun insertReport(report: Report)
}
