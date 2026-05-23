package com.example.plannerdiario.data
import kotlinx.coroutines.flow.Flow

class PlannerRepository(
    private val taskListDao: TaskListDao,
    private val taskDao: TaskDao
) {
    fun getAllTaskLists(): Flow<List<TaskList>> = taskListDao.getAllTaskLists()
    suspend fun getAllTaskListsOnce(): List<TaskList> = taskListDao.getAllDirect()

    suspend fun insertTaskList(taskList: TaskList): Long = taskListDao.insert(taskList)
    suspend fun updateTaskList(taskList: TaskList) = taskListDao.update(taskList)
    suspend fun updateListOrder(lists: List<TaskList>) = taskListDao.updateAll(lists)
    suspend fun deleteTaskList(taskList: TaskList) = taskListDao.delete(taskList)
    suspend fun deleteAllTaskLists() = taskListDao.deleteAll()

    fun getTasksForListAndDate(listId: Long, date: String): Flow<List<Task>> =
        taskDao.getTasksForListAndDate(listId, date)

    fun getCompletedDates(listId: Long): Flow<List<String>> =
        taskDao.getCompletedDates(listId)

    fun getScheduledDates(listId: Long): Flow<List<String>> =
        taskDao.getScheduledDates(listId)

    suspend fun getAllTasksOnce(): List<Task> = taskDao.getAllDirect()

    suspend fun insertTask(task: Task): Long = taskDao.insert(task)
    suspend fun updateTask(task: Task) = taskDao.update(task)
    suspend fun deleteTask(task: Task) = taskDao.delete(task)
    suspend fun deleteAllTasks() = taskDao.deleteAll()
}
