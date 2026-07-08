package cord.eoeo.momentwo.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val username: String,
    val name: String,
    val nickname: String,
    val phone: String,
    @ColumnInfo(name = "user_profile_image") val userProfileImage: String,
)
