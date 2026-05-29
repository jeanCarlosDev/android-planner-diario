package com.jsjstudios.dailyplanner.data
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskList::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("listId")]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val completedDate: String? = null,
    val attachmentUri: String? = null,    // content URI ou URL
    val attachmentType: String? = null,   // "PDF", "IMAGE", "LINK"
    val attachmentName: String? = null,   // nome exibido
    val isScheduled: Boolean = false,     // tarefa programada (vinculada a uma data)
    val scheduledDate: String? = null,    // data no formato "yyyy-MM-dd"
    val isRecurring: Boolean = false,     // tarefa recorrente (aparece todo dia)
    val recurrenceInterval: String? = null, // "ALWAYS", "WEEKLY", "BIWEEKLY", "MONTHLY"
    val createdAt: Long = System.currentTimeMillis()
)
