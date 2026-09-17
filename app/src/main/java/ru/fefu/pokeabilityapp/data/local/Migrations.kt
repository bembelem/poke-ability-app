package ru.fefu.pokeabilityapp.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `profiles` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`createdAt` INTEGER NOT NULL)"
        )
        db.execSQL(
            "INSERT INTO profiles (name, createdAt) VALUES ('Тренер', ${System.currentTimeMillis()})"
        )

        // favourites получает profileId, в SQLite первичный ключ меняется только пересозданием
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `favourites_new` (" +
                "`profileId` INTEGER NOT NULL, " +
                "`id` INTEGER NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`addedAt` INTEGER NOT NULL, " +
                "PRIMARY KEY(`profileId`, `id`), " +
                "FOREIGN KEY(`profileId`) REFERENCES `profiles`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL(
            "INSERT INTO favourites_new (profileId, id, name, addedAt) " +
                "SELECT (SELECT id FROM profiles LIMIT 1), id, name, addedAt FROM favourites"
        )
        db.execSQL("DROP TABLE favourites")
        db.execSQL("ALTER TABLE favourites_new RENAME TO favourites")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_favourites_profileId` ON `favourites` (`profileId`)")
    }
}
