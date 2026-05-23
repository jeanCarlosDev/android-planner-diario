package com.example.plannerdiario
import android.app.Application
import com.example.plannerdiario.data.AppDatabase
import com.example.plannerdiario.data.PlannerRepository
class PlannerApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { PlannerRepository(database.taskListDao(), database.taskDao()) }
}
