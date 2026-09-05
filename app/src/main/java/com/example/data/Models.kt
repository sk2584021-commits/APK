package com.example.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val fullName: String,
    val bio: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isPrivate: Boolean = false,
    val profilePicUrl: String? = null
)

@Entity(
    tableName = "posts",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index(value = ["userId"])]
)
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val caption: String,
    val hashtags: String = "",
    val location: String? = null,
    val imageUrl: String? = null,
    val isVideo: Boolean = false,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class PostWithUser(
    @Embedded val post: Post,
    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val user: User
)

@Entity(tableName = "likes", primaryKeys = ["userId", "postId"])
data class Like(
    val userId: Int,
    val postId: Int
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val postId: Int,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CommentWithUser(
    @Embedded val comment: Comment,
    val username: String,
    val profilePicUrl: String?
)

@Entity(tableName = "follows", primaryKeys = ["followerId", "followingId"])
data class Follow(
    val followerId: Int,
    val followingId: Int
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val senderId: Int,
    val type: String,
    val postId: Int? = null,
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class NotificationWithUser(
    @Embedded val notification: Notification,
    val username: String,
    val profilePicUrl: String?
)

@Entity(
    tableName = "conversations",
    indices = [androidx.room.Index(value = ["user1Id", "user2Id"], unique = true)]
)
data class Conversation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val user1Id: Int,
    val user2Id: Int,
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis()
)

data class ConversationWithOtherUser(
    @Embedded val conversation: Conversation,
    val otherUserId: Int,
    val otherUsername: String,
    val otherUserProfilePic: String?,
    val unreadCount: Int = 0
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(entity = Conversation::class, parentColumns = ["id"], childColumns = ["conversationId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [androidx.room.Index(value = ["conversationId"])]
)
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val conversationId: Int,
    val senderId: Int,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "blocks", primaryKeys = ["blockerId", "blockedId"])
data class Block(
    val blockerId: Int,
    val blockedId: Int
)

@Entity(tableName = "saved_posts", primaryKeys = ["userId", "postId"])
data class SavedPost(
    val userId: Int,
    val postId: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reporterId: Int,
    val reportedId: Int,
    val type: String, // "USER", "POST", "COMMENT"
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
