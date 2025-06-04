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
                    MainScreen(modifier = Modifier.padding(innerPadding), sharedPreferences = sharedPreferences)
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

@Composable
fun MainScreen(modifier: Modifier = Modifier, sharedPreferences: SharedPreferences) {
    var smsKeyword by remember { mutableStateOf(sharedPreferences.getString("smsKeyword", "") ?: "") }
    var wechatNumber by remember { mutableStateOf(sharedPreferences.getString("wechatNumber", "") ?: "") }
    var isListening by remember { mutableStateOf(sharedPreferences.getBoolean("isListening", false)) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("短信转发配置", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = smsKeyword,
            onValueChange = { smsKeyword = it },
            label = { Text("短信关键字") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = wechatNumber,
            onValueChange = { wechatNumber = it },
            label = { Text("转发微信号") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = {
                if (smsKeyword.isNotBlank() && wechatNumber.isNotBlank()) {
                    isListening = true
                    with(sharedPreferences.edit()) {
                        putString("smsKeyword", smsKeyword)
                        putString("wechatNumber", wechatNumber)
                        putBoolean("isListening", true)
                        apply()
                    }
                    android.widget.Toast.makeText(context, "开始监听", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, "请输入关键字和微信号", android.widget.Toast.LENGTH_SHORT).show()
                }
            }, enabled = !isListening) {
                Text("确定")
            }

            Button(onClick = {
                isListening = false
                with(sharedPreferences.edit()) {
                    putBoolean("isListening", false)
                    apply()
                }
                android.widget.Toast.makeText(context, "取消监听", android.widget.Toast.LENGTH_SHORT).show()
            }, enabled = isListening) {
                Text("取消")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            // Navigate to Log screen
            val intent = Intent(context, LogActivity::class.java)
            context.startActivity(intent)
        }) {
            Text("查看日志")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    SmsForwardWeChatTheme {
        // For preview, we can pass a dummy SharedPreferences or null
        // This might require adjusting MainScreen to handle null SharedPreferences for preview purposes
        // Or use a more sophisticated preview setup with a fake SharedPreferences implementation.
        val context = LocalContext.current
        MainScreen(sharedPreferences = context.getSharedPreferences("PreviewSmsForwardPrefs", Context.MODE_PRIVATE))
    }
}