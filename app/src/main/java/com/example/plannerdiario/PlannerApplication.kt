package com.jsjstudios.dailyplanner
import android.app.Application
import com.jsjstudios.dailyplanner.data.AppDatabase
import com.jsjstudios.dailyplanner.data.PlannerRepository
class PlannerApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { PlannerRepository(database.taskListDao(), database.taskDao()) }
}
