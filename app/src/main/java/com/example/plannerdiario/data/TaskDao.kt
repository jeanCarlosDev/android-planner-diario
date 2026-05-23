package com.example.plannerdiario.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("""
        SELECT * FROM tasks
        WHERE listId = :listId
          AND (completedDate IS NULL OR completedDate = :date)
          AND (scheduledDate IS NULL OR scheduledDate = :date)
        ORDER BY isCompleted ASC, isScheduled ASC, createdAt ASC
    """)
    fun getTasksForListAndDate(listId: Long, date: String): Flow<List<Task>>

    @Query("SELECT DISTINCT completedDate FROM tasks WHERE listId = :listId AND completedDate IS NOT NULL")
    fun getCompletedDates(listId: Long): Flow<List<String>>

    @Query("SELECT DISTINCT scheduledDate FROM tasks WHERE listId = :listId AND isScheduled = 1 AND isCompleted = 0 AND scheduledDate IS NOT NULL")
    fun getScheduledDates(listId: Long): Flow<List<String>>

    @Query("SELECT * FROM tasks")
    suspend fun getAllDirect(): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}
