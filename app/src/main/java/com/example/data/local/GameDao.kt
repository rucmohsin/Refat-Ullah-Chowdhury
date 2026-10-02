package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgress(): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    suspend fun getUserProgressOnce(): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProgress(progress: UserProgressEntity)

    @Update
    suspend fun updateUserProgress(progress: UserProgressEntity)

    @Query("SELECT * FROM level_records WHERE levelNumber = :levelNumber LIMIT 1")
    suspend fun getLevelRecord(levelNumber: Int): LevelRecordEntity?

    @Query("SELECT * FROM level_records ORDER BY levelNumber ASC")
    fun getAllLevelRecords(): Flow<List<LevelRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLevelRecord(record: LevelRecordEntity)

    @Query("SELECT COUNT(*) FROM level_records")
    fun getCompletedLevelCount(): Flow<Int>

    @Query("SELECT SUM(score) FROM level_records")
    fun getTotalScore(): Flow<Int?>
}
