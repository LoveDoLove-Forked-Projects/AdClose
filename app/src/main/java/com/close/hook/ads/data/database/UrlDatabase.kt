package com.close.hook.ads.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.close.hook.ads.data.dao.UrlDao
import com.close.hook.ads.data.model.Url
import kotlin.math.abs

@Database(entities = [Url::class], version = 2, exportSchema = false)
abstract class UrlDatabase : RoomDatabase() {
    abstract val urlDao: UrlDao

    companion object {
        private var instance: UrlDatabase? = null

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE url_info_new (id integer not null, url TEXT not null, PRIMARY KEY(id))")
                db.execSQL("INSERT INTO url_info_new (id, url) SELECT id, url FROM url_info")
                db.execSQL("DROP TABLE url_info")
                db.execSQL("ALTER TABLE url_info_new RENAME TO url_info")
            }
        }

        @Synchronized
        fun getDatabase(context: Context): UrlDatabase {
            instance?.let {
                return it
            }
            return databaseBuilder(
                context.applicationContext,
                UrlDatabase::class.java, "url_database"
            )
                .addMigrations(MIGRATION_1_2)
                .allowMainThreadQueries()
                .build().apply {
                    instance = this
                }
        }
    }
}
