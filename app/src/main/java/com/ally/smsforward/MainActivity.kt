package com.ally.smsforward

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ally.smsforward.ui.theme.SmsForwardWeChatTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.HourglassEmpty

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { (permission, isGranted) ->
            // Handle permission grant or denial
            // For now, we'll just log it or show a toast
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your app.
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sharedPreferences = getSharedPreferences("SmsForwardPrefs", Context.MODE_PRIVATE)
        setContent {
            SmsForwardWeChatTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        sharedPreferences = sharedPreferences
                    )
                }
            }
        }
        requestSmsPermissions()
    }

    private fun requestSmsPermissions() {
        val permissionsToRequest = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        val permissionsNotGranted = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNotGranted.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsNotGranted.toTypedArray())
        }
    }
}

data class ForwardConfig(
    val id: String,
    val smsKeyword: String,
    val wechatNumber: String,
    var isActive: Boolean
)

@Composable
fun MainScreen(modifier: Modifier = Modifier, sharedPreferences: SharedPreferences) {
    val context = LocalContext.current
    var showConfigScreen by remember { mutableStateOf(false) }
    var configs by remember { mutableStateOf<List<ForwardConfig>>(emptyList()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // 顶部按钮区
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { showConfigScreen = true }) {
                Icon(Icons.Default.Add, contentDescription = "添加配置", tint = Color.Black)
            }
            IconButton(onClick = {
                val intent = Intent(context, LogActivity::class.java)
                context.startActivity(intent)
            }) {
                Icon(Icons.Filled.List, contentDescription = "查看日志", tint = Color.Black)
            }
        }

        // 列表区
        if (configs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    "暂无短信监听\r\n请先添加一个吧~",
                    style = TextStyle(fontSize = 20.sp, color = Color.Gray)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(configs) { config ->
                    ConfigItem(
                        config = config,
                        onStart = {
                            configs = configs.map { 
                                if (it.id == config.id) it.copy(isActive = true) 
                                else it 
                            }
                            with(sharedPreferences.edit()) {
                                putString("smsKeyword", config.smsKeyword)
                                putString("wechatNumber", config.wechatNumber)
                                putBoolean("isListening", true)
                                apply()
                            }
                            android.widget.Toast.makeText(
                                context,
                                "开始监听",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        onStop = {
                            configs = configs.map { 
                                if (it.id == config.id) it.copy(isActive = false) 
                                else it 
                            }
                            with(sharedPreferences.edit()) {
                                putBoolean("isListening", false)
                                apply()
                            }
                            android.widget.Toast.makeText(
                                context,
                                "停止监听",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }

    // 弹窗方式显示配置界面
    if (showConfigScreen) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showConfigScreen = false }) {
            ConfigScreen(
                onSave = { smsKeyword, wechatNumber ->
                    configs = configs + ForwardConfig(
                        id = System.currentTimeMillis().toString(),
                        smsKeyword = smsKeyword,
                        wechatNumber = wechatNumber,
                        isActive = false
                    )
                    showConfigScreen = false
                },
                onCancel = { showConfigScreen = false }
            )
        }
    }
}

@Composable
fun ConfigScreen(
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit
) {
    var smsKeyword by remember { mutableStateOf("") }
    var wechatNumber by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .padding(32.dp), // 四周留白
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 350.dp)
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "添加监听配置",
                style = TextStyle(fontSize = 24.sp, color = Color.Black)
            )

            OutlinedTextField(
                value = smsKeyword,
                onValueChange = { smsKeyword = it },
                label = { Text("短信关键字") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = wechatNumber,
                onValueChange = { wechatNumber = it },
                label = { Text("转发微信号") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    16.dp,
                    Alignment.CenterHorizontally
                )
            ) {
                Button(
                    onClick = { onCancel() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text("取消")
                }
                
                Button(
                    onClick = { 
                        if (smsKeyword.isNotBlank() && wechatNumber.isNotBlank()) {
                            onSave(smsKeyword, wechatNumber) 
                        }
                    },
                    enabled = smsKeyword.isNotBlank() && wechatNumber.isNotBlank()
                ) {
                    Text("保存")
                }
            }
        }
    }
}

@Composable
fun ConfigItem(
    config: ForwardConfig,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val isActive = config.isActive

    // 旋转动画
    val infiniteTransition = rememberInfiniteTransition()
    val rotation = if (isActive) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing)
            )
        ).value
    } else {
        0f
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${config.smsKeyword} → ${config.wechatNumber}",
            style = TextStyle(fontSize = 16.sp, color = Color.Black)
        )

        Row {
            // 开始监听按钮
            IconButton(
                onClick = { if (!isActive) onStart() },
                enabled = !isActive
            ) {
                if (isActive) {
                    Icon(
                        imageVector = Icons.Filled.Autorenew,
                        contentDescription = "正在监听",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF4CAF50)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "开始监听",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 停止监听按钮
            IconButton(
                onClick = { if (isActive) onStop() },
                enabled = isActive
            ) {
                Icon(
                    imageVector = Icons.Filled.Stop,
                    contentDescription = "停止监听",
                    modifier = Modifier.size(24.dp),
                    tint = if (isActive) Color.Red else Color.LightGray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    SmsForwardWeChatTheme {
        val context = LocalContext.current
        MainScreen(
            sharedPreferences = context.getSharedPreferences(
                "PreviewSmsForwardPrefs",
                Context.MODE_PRIVATE
            )
        )
    }
}

@Preview
@Composable
fun ConfigScreenPreview() {
    SmsForwardWeChatTheme {
        ConfigScreen(
            onSave = { _, _ -> },
            onCancel = {}
        )
    }
}