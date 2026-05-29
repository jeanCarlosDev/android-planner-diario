package com.jsjstudios.dailyplanner.ui
import androidx.lifecycle.*
import com.jsjstudios.dailyplanner.data.BackupManager
import com.jsjstudios.dailyplanner.data.PlannerRepository
import com.jsjstudios.dailyplanner.data.Task
import com.jsjstudios.dailyplanner.data.TaskList
import com.jsjstudios.dailyplanner.data.listPresetOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
@OptIn(ExperimentalCoroutinesApi::class)
class PlannerViewModel(private val repository: PlannerRepository) : ViewModel() {
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()
    private val _selectedListId = MutableStateFlow<Long?>(null)
    val selectedListId: StateFlow<Long?> = _selectedListId.asStateFlow()
    val taskLists: StateFlow<List<TaskList>> = repository.getAllTaskLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val selectedList: StateFlow<TaskList?> = combine(taskLists, _selectedListId) { lists, id ->
        lists.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val tasks: StateFlow<List<Task>> = combine(_selectedListId, _selectedDate) { id, date ->
        Pair(id, date)
    }.flatMapLatest { (listId, date) ->
        if (listId != null) repository.getTasksForListAndDate(listId, date.toString())
            .map { list ->
                list.map { task ->
                    // Tarefas recorrentes: "concluída" é apenas para o dia em questão
                    if (task.isRecurring && task.completedDate != date.toString()) {
                        task.copy(isCompleted = false, completedDate = null)
                    } else task
                }
            }
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedDates: StateFlow<Set<LocalDate>> = _selectedListId.flatMapLatest { id ->
        if (id != null) repository.getCompletedDates(id).map { list ->
            list.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }.toSet()
        } else flowOf(emptySet())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val scheduledDates: StateFlow<Set<LocalDate>> = _selectedListId.flatMapLatest { id ->
        if (id != null) repository.getScheduledDates(id).map { list ->
            list.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }.toSet()
        } else flowOf(emptySet())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val recurringTasks: StateFlow<List<Task>> = _selectedListId.flatMapLatest { id ->
        if (id != null) repository.getRecurringTasks(id)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    init {
        viewModelScope.launch {
            taskLists.collect { lists ->
                if (_selectedListId.value == null && lists.isNotEmpty()) {
                    _selectedListId.value = lists.first().id
                }
            }
        }
    }
    fun selectDate(date: LocalDate) { _selectedDate.value = date }
    fun goToPreviousDay() { _selectedDate.value = _selectedDate.value.minusDays(1) }
    fun goToNextDay()     { _selectedDate.value = _selectedDate.value.plusDays(1) }
    fun selectList(listId: Long) { _selectedListId.value = listId }
    fun addList(name: String, colorHex: String? = null, shape: String? = null) {
        viewModelScope.launch {
            val autoOption = listPresetOptions[taskLists.value.size % listPresetOptions.size]
            val finalColorHex = colorHex ?: autoOption.colorHex
            val finalShape    = shape    ?: autoOption.shape
            val nextOrder     = taskLists.value.size
            val id = repository.insertTaskList(
                TaskList(name = name, colorHex = finalColorHex, shape = finalShape, sortOrder = nextOrder)
            )
            _selectedListId.value = id
        }
    }
    fun deleteList(taskList: TaskList) {
        viewModelScope.launch { repository.deleteTaskList(taskList) }
    }

    /** Apaga todas as listas e tarefas. */
    fun clearAll() {
        viewModelScope.launch {
            repository.deleteAllTasks()
            repository.deleteAllTaskLists()
            _selectedListId.value = null
        }
    }

    /** Serializa todos os dados para JSON e entrega via callback no Main thread. */
    fun exportBackup(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val lists = repository.getAllTaskListsOnce()
            val tasks = repository.getAllTasksOnce()
            onReady(BackupManager.exportToJson(lists, tasks))
        }
    }

    /** Importa listas e tarefas a partir de JSON (adiciona ao banco existente). */
    fun importBackup(json: String, onDone: (success: Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val (lists, tasks) = BackupManager.importFromJson(json)
                val idMap = mutableMapOf<Long, Long>()
                lists.forEach { list ->
                    val newId = repository.insertTaskList(list.copy(id = 0))
                    idMap[list.id] = newId
                }
                tasks.forEach { task ->
                    val newListId = idMap[task.listId] ?: return@forEach
                    repository.insertTask(task.copy(id = 0, listId = newListId))
                }
                // seleciona a primeira lista importada
                idMap.values.firstOrNull()?.let { _selectedListId.value = it }
                onDone(true)
            } catch (_: Exception) {
                onDone(false)
            }
        }
    }
    fun addTask(
        title: String,
        description: String = "",
        attachmentUri: String? = null,
        attachmentType: String? = null,
        attachmentName: String? = null,
        isScheduled: Boolean = false,
        repeatDays: Int = 1,
        isRecurring: Boolean = false,
        recurrenceInterval: String? = null
    ) {
        val listId = _selectedListId.value ?: return
        val currentDate = _selectedDate.value
        viewModelScope.launch {
            when {
                isRecurring -> {
                    repository.insertTask(
                        Task(
                            listId             = listId,
                            title              = title,
                            description        = description,
                            attachmentUri      = attachmentUri,
                            attachmentType     = attachmentType,
                            attachmentName     = attachmentName,
                            isRecurring        = true,
                            recurrenceInterval = recurrenceInterval
                        )
                    )
                }
                isScheduled -> {
                    val days = repeatDays.coerceAtLeast(1)
                    repeat(days) { dayOffset ->
                        val taskDate = currentDate.plusDays(dayOffset.toLong())
                        repository.insertTask(
                            Task(
                                listId         = listId,
                                title          = title,
                                description    = description,
                                attachmentUri  = attachmentUri,
                                attachmentType = attachmentType,
                                attachmentName = attachmentName,
                                isScheduled    = true,
                                scheduledDate  = taskDate.toString()
                            )
                        )
                    }
                }
                else -> {
                    repository.insertTask(
                        Task(
                            listId         = listId,
                            title          = title,
                            description    = description,
                            attachmentUri  = attachmentUri,
                            attachmentType = attachmentType,
                            attachmentName = attachmentName
                        )
                    )
                }
            }
        }
    }
    fun toggleTask(task: Task) {
        viewModelScope.launch {
            val nowCompleted = !task.isCompleted
            repository.updateTask(
                task.copy(
                    isCompleted = nowCompleted,
                    completedDate = if (nowCompleted) _selectedDate.value.toString() else null
                )
            )
        }
    }
    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }
    /** Salva a nova ordem das listas após drag-and-drop. */
    fun reorderLists(orderedIds: List<Long>) {
        viewModelScope.launch {
            val current = taskLists.value
            val updated = orderedIds.mapIndexed { index, id ->
                current.first { it.id == id }.copy(sortOrder = index)
            }
            repository.updateListOrder(updated)
        }
    }
    fun updateTask(
        task: Task,
        title: String,
        description: String,
        attachmentUri: String?,
        attachmentType: String?,
        attachmentName: String?
    ) {
        viewModelScope.launch {
            repository.updateTask(
                task.copy(
                    title = title,
                    description = description,
                    attachmentUri = attachmentUri,
                    attachmentType = attachmentType,
                    attachmentName = attachmentName
                )
            )
        }
    }
}
class PlannerViewModelFactory(private val repository: PlannerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlannerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
