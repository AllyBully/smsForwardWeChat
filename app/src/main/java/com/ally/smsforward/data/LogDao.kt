package com.ally.smsforward.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LogDao {
    @Insert
    suspend fun insert(logEntry: LogEntry)

    @Query("SELECT * FROM sms_logs ORDER BY timestamp DESC")
    fun getAllLogs(): LiveData<List<LogEntry>>

    @Query("DELETE FROM sms_logs")
    suspend fun clearAllLogs()
}