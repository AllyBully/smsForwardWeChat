package com.ally.smsforward.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_logs")
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val sender: String,
    val message: String,
    val wechatNumber: String,
    val status: String // e.g., "SUCCESS", "FAILURE"
)