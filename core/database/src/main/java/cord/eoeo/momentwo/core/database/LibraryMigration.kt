package cord.eoeo.momentwo.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Legacy network caches have no provable owner and must never become device-owned originals. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS library (id TEXT NOT NULL PRIMARY KEY, accountId TEXT, name TEXT NOT NULL)")
        db.execSQL("INSERT INTO library (id, accountId, name) VALUES ('device', NULL, '이 기기')")
        db.execSQL("CREATE TABLE IF NOT EXISTS local_album (localId TEXT NOT NULL PRIMARY KEY, libraryId TEXT NOT NULL, title TEXT NOT NULL, subtitle TEXT NOT NULL, coverMediaName TEXT, remoteId INTEGER, revision INTEGER NOT NULL, createdAt INTEGER NOT NULL, FOREIGN KEY(libraryId) REFERENCES library(id) ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_local_album_libraryId ON local_album(libraryId)")
        db.execSQL("CREATE TABLE IF NOT EXISTS local_subalbum (localId TEXT NOT NULL PRIMARY KEY, albumId TEXT NOT NULL, title TEXT NOT NULL, remoteId INTEGER, revision INTEGER NOT NULL, createdAt INTEGER NOT NULL, FOREIGN KEY(albumId) REFERENCES local_album(localId) ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_local_subalbum_albumId ON local_subalbum(albumId)")
        db.execSQL("CREATE TABLE IF NOT EXISTS local_photo (localId TEXT NOT NULL PRIMARY KEY, subAlbumId TEXT NOT NULL, mediaName TEXT, remoteUrl TEXT NOT NULL, remoteId INTEGER, description TEXT NOT NULL, descriptionKnown INTEGER NOT NULL, favorite INTEGER NOT NULL, isLiked INTEGER NOT NULL, likeCount INTEGER, revision INTEGER NOT NULL, createdAt INTEGER NOT NULL, FOREIGN KEY(subAlbumId) REFERENCES local_subalbum(localId) ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_local_photo_subAlbumId ON local_photo(subAlbumId)")
        db.execSQL("CREATE TABLE IF NOT EXISTS local_memo (localId TEXT NOT NULL PRIMARY KEY, photoId TEXT NOT NULL, text TEXT NOT NULL, createdAt INTEGER NOT NULL, FOREIGN KEY(photoId) REFERENCES local_photo(localId) ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_local_memo_photoId ON local_memo(photoId)")
        listOf("photo", "photo_key", "friend", "friend_key").forEach { db.execSQL("DELETE FROM $it") }
    }
}

/** Unowned legacy server caches cannot be attributed to the currently logged-in user. */
val MIGRATION_2_3 = object : Migration(2,3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE local_album ADD COLUMN remoteCoverUrl TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE local_album ADD COLUMN role TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE local_album ADD COLUMN subsLoaded INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE local_subalbum ADD COLUMN photosLoaded INTEGER NOT NULL DEFAULT 1")
        db.execSQL("DROP TABLE IF EXISTS photo")
        db.execSQL("CREATE TABLE IF NOT EXISTS `photo` (`id` INTEGER NOT NULL, `album_id` INTEGER NOT NULL, `sub_album_id` INTEGER NOT NULL, `photo_url` TEXT NOT NULL, `is_liked` INTEGER NOT NULL, `ownerId` TEXT NOT NULL DEFAULT '', PRIMARY KEY(`ownerId`, `id`))")
        db.execSQL("DROP TABLE IF EXISTS photo_key")
        db.execSQL("CREATE TABLE IF NOT EXISTS `photo_key` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `album_id` INTEGER NOT NULL, `sub_album_id` INTEGER NOT NULL, `last_updated` INTEGER NOT NULL, `next_cursor` INTEGER, `ownerId` TEXT NOT NULL DEFAULT '')")
        db.execSQL("DROP TABLE IF EXISTS friend")
        db.execSQL("CREATE TABLE IF NOT EXISTS `friend` (`nickname` TEXT NOT NULL, `user_profile_image` TEXT NOT NULL, `ownerId` TEXT NOT NULL DEFAULT '', PRIMARY KEY(`ownerId`, `nickname`))")
        db.execSQL("DROP TABLE IF EXISTS friend_key")
        db.execSQL("CREATE TABLE IF NOT EXISTS `friend_key` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `last_updated` INTEGER NOT NULL, `next_cursor` INTEGER, `ownerId` TEXT NOT NULL DEFAULT '')")
    }
}
