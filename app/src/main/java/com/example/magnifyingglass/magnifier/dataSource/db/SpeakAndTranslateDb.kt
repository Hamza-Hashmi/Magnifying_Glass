package com.example.magnifyingglass.magnifier.dataSource.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.magnifyingglass.magnifier.billing.PremiumDao
import com.example.magnifyingglass.magnifier.billing.SkuDetailsModel

@Database(entities = [SkuDetailsModel::class],version = 3,
    exportSchema = true)
abstract class SpeakAndTranslateDb:RoomDatabase() {

    abstract fun skuDetailsDao(): PremiumDao


    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Perform migration logic here
            //database.execSQL("CREATE TABLE IF NOT EXISTS `billing` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` REAL NOT NULL)")
        }
    }
}
