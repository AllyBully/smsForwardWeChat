package com.ally.smsforward.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.ally.smsforward.data.AppDatabase
import com.ally.smsforward.data.LogEntry
import com.ally.smsforward.data.LogDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LogViewModel(application: Application) : AndroidViewModel(application) {

    private val logDao: LogDao
    val allLogs: LiveData<List<LogEntry>>

    init {
        logDao = AppDatabase.getDatabase(application).logDao()
        allLogs = logDao.getAllLogs()
    }

    fun insert(logEntry: LogEntry) = viewModelScope.launch(Dispatchers.IO) {
        logDao.insert(logEntry)
    }

    fun clearLogs() = viewModelScope.launch(Dispatchers.IO) {
        logDao.clearAllLogs()
    }
}