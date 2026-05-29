package com.jsjstudios.dailyplanner.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("""
        SELECT * FROM tasks
        WHERE listId = :listId
          AND (
            -- Tarefas normais e programadas
            (isRecurring = 0
             AND (completedDate IS NULL OR completedDate = :date)
             AND (scheduledDate IS NULL OR scheduledDate = :date))
            OR
            -- Recorrente SEMPRE: aparece todos os dias
            (isRecurring = 1 AND (recurrenceInterval = 'ALWAYS' OR recurrenceInterval IS NULL))
            OR
            -- Recorrente SEMANAL: a cada 7 dias a partir da criação (data local, sem hora)
            (isRecurring = 1 AND recurrenceInterval = 'WEEKLY'
             AND CAST(julianday(:date) - julianday(date(datetime(createdAt/1000, 'unixepoch', 'localtime'))) AS INTEGER) >= 0
             AND CAST(julianday(:date) - julianday(date(datetime(createdAt/1000, 'unixepoch', 'localtime'))) AS INTEGER) % 7 = 0)
            OR
            -- Recorrente QUINZENAL: a cada 15 dias a partir da criação
            (isRecurring = 1 AND recurrenceInterval = 'BIWEEKLY'
             AND CAST(julianday(:date) - julianday(date(datetime(createdAt/1000, 'unixepoch', 'localtime'))) AS INTEGER) >= 0
             AND CAST(julianday(:date) - julianday(date(datetime(createdAt/1000, 'unixepoch', 'localtime'))) AS INTEGER) % 15 = 0)
            OR
            -- Recorrente MENSAL: a cada 30 dias a partir da criação
            (isRecurring = 1 AND recurrenceInterval = 'MONTHLY'
             AND CAST(julianday(:date) - julianday(date(datetime(createdAt/1000, 'unixepoch', 'localtime'))) AS INTEGER) >= 0
             AND CAST(julianday(:date) - julianday(date(datetime(createdAt/1000, 'unixepoch', 'localtime'))) AS INTEGER) % 30 = 0)
          )
        ORDER BY isCompleted ASC, isScheduled ASC, isRecurring ASC, createdAt ASC
    """)
    fun getTasksForListAndDate(listId: Long, date: String): Flow<List<Task>>

    @Query("SELECT DISTINCT completedDate FROM tasks WHERE listId = :listId AND completedDate IS NOT NULL")
    fun getCompletedDates(listId: Long): Flow<List<String>>

    @Query("SELECT DISTINCT scheduledDate FROM tasks WHERE listId = :listId AND isScheduled = 1 AND isCompleted = 0 AND scheduledDate IS NOT NULL")
    fun getScheduledDates(listId: Long): Flow<List<String>>

    @Query("SELECT * FROM tasks WHERE listId = :listId AND isRecurring = 1")
    fun getRecurringTasks(listId: Long): Flow<List<Task>>

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
