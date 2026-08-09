package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [StreetEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun streetDao(): StreetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "auspost_streets_db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate database with Australian sample streets
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                database.streetDao().insertAll(
                                    listOf(
                                        StreetEntity("HIGH STREET", "ROUND 01"),
                                        StreetEntity("HIGH ST", "ROUND 01"),
                                        StreetEntity("STATION ROAD", "ROUND 05"),
                                        StreetEntity("STATION RD", "ROUND 05"),
                                        StreetEntity("VICTORIA PARADE", "ROUND 12"),
                                        StreetEntity("VICTORIA PDE", "ROUND 12"),
                                        StreetEntity("BOURKE STREET", "ROUND 07"),
                                        StreetEntity("BOURKE ST", "ROUND 07"),
                                        StreetEntity("COLLINS STREET", "ROUND 03"),
                                        StreetEntity("COLLINS ST", "ROUND 03"),
                                        StreetEntity("FLINDERS STREET", "ROUND 04"),
                                        StreetEntity("FLINDERS ST", "ROUND 04"),
                                        StreetEntity("ELIZABETH STREET", "ROUND 06"),
                                        StreetEntity("ELIZABETH ST", "ROUND 06"),
                                        StreetEntity("SWANSTON STREET", "ROUND 02"),
                                        StreetEntity("SWANSTON ST", "ROUND 02"),
                                        StreetEntity("GEORGE STREET", "ROUND 08"),
                                        StreetEntity("GEORGE ST", "ROUND 08"),
                                        StreetEntity("PITT STREET", "ROUND 09"),
                                        StreetEntity("PITT ST", "ROUND 09"),
                                        StreetEntity("KING STREET", "ROUND 10"),
                                        StreetEntity("KING ST", "ROUND 10"),
                                        StreetEntity("QUEEN STREET", "ROUND 11"),
                                        StreetEntity("QUEEN ST", "ROUND 11"),
                                        StreetEntity("MACQUARIE STREET", "ROUND 14"),
                                        StreetEntity("CHAPEL STREET", "ROUND 15")
                                    )
                                )
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
