package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StreetDao {
    @Query("SELECT * FROM streets ORDER BY streetName ASC")
    fun getAllStreets(): Flow<List<StreetEntity>>

    @Query("SELECT * FROM streets WHERE streetName LIKE '%' || :query || '%' OR roundNumber LIKE '%' || :query || '%' ORDER BY streetName ASC")
    fun searchStreets(query: String): Flow<List<StreetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(street: StreetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(streets: List<StreetEntity>)

    @Delete
    suspend fun delete(street: StreetEntity)

    @Query("DELETE FROM streets WHERE streetName = :streetName")
    suspend fun deleteByName(streetName: String)

    @Query("SELECT COUNT(*) FROM streets")
    suspend fun getCount(): Int

    @Query("SELECT * FROM streets")
    suspend fun getAllList(): List<StreetEntity>
}
