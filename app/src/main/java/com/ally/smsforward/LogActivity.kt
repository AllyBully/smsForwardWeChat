package com.ally.smsforward

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.ally.smsforward.data.LogEntry
import com.ally.smsforward.ui.LogViewModel
import com.ally.smsforward.ui.theme.SmsForwardWeChatTheme
import java.text.SimpleDateFormat
import java.util.*

class LogActivity : ComponentActivity() {
    private lateinit var logViewModel: LogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        logViewModel = ViewModelProvider(this).get(LogViewModel::class.java)
        setContent {
            SmsForwardWeChatTheme {
                LogScreen(logViewModel = logViewModel, onNavigateUp = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(logViewModel: LogViewModel, onNavigateUp: () -> Unit) {
    val logs by logViewModel.allLogs.observeAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("转发日志") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { logViewModel.clearLogs() }) {
                        Icon(Icons.Filled.Delete, contentDescription = "清空日志")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无日志记录")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(logs) {
                    log -> LogItem(log)
                    Divider()
                }
            }
        }
    }
}

@Composable
fun LogItem(logEntry: LogEntry) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {
        Text("时间: ${dateFormat.format(Date(logEntry.timestamp))}", style = MaterialTheme.typography.bodySmall)
        Text("发送方: ${logEntry.sender}", style = MaterialTheme.typography.bodyMedium)
        Text("消息: ${logEntry.message}", style = MaterialTheme.typography.bodyMedium)
        Text("微信号: ${logEntry.wechatNumber}", style = MaterialTheme.typography.bodyMedium)
        Text("状态: ${logEntry.status}", style = MaterialTheme.typography.bodyMedium, color = if (logEntry.status == "SUCCESS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun LogScreenPreview() {
    SmsForwardWeChatTheme {
        // Dummy LogViewModel for preview
        val dummyLogs = listOf(
            LogEntry(1, System.currentTimeMillis(), "10086", "这是一条测试短信", "12345", "SUCCESS"),
            LogEntry(2, System.currentTimeMillis() - 100000, "+8613800138000", "验证码：123456", "54321", "FAILURE")
        )
        val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
        val previewLogViewModel = LogViewModel(app) // This might not work perfectly in preview if DB access is strict
        // A better way for preview would be to mock the LiveData directly or use a fake ViewModel.
        // For simplicity, we'll try this, but be aware it might have issues.

        // Simulate LiveData for preview
        val logsLiveData = androidx.lifecycle.MutableLiveData<List<LogEntry>>(dummyLogs)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("转发日志") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Delete, contentDescription = "清空日志")
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(dummyLogs) { log ->
                    LogItem(log)
                    Divider()
                }
            }
        }
    }
}