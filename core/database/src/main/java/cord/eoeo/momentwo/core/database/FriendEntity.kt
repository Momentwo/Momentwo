package cord.eoeo.momentwo.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cord.eoeo.momentwo.core.model.FriendItem

@Entity(tableName = "friend")
data class FriendEntity(
    @PrimaryKey val nickname: String,
    @ColumnInfo(name = "user_profile_image") val userProfileImage: String,
) {
    fun mapToFriendItem(): FriendItem =
        FriendItem(
            nickname = nickname,
            userProfileImage = userProfileImage,
        )
}
