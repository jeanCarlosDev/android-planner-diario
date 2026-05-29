package com.jsjstudios.dailyplanner.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskListDao {
    @Query("SELECT * FROM task_lists ORDER BY sortOrder ASC, id ASC")
    fun getAllTaskLists(): Flow<List<TaskList>>

    @Query("SELECT * FROM task_lists ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllDirect(): List<TaskList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(taskList: TaskList): Long

    @Update
    suspend fun update(taskList: TaskList)

    @Update
    suspend fun updateAll(taskLists: List<TaskList>)

    @Delete
    suspend fun delete(taskList: TaskList)

    @Query("DELETE FROM task_lists")
    suspend fun deleteAll()
}
