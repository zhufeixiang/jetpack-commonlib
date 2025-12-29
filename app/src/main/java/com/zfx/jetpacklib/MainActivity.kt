package com.zfx.jetpacklib

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.zfx.jetpacklib.ui.MainContent

class MainActivity : ComponentActivity() {
    
    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }
    
    private val KEY_ACTIVITY_CHOOSEN = "activity_choosen"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isChoosen = remember { 
                mutableStateOf(prefs.getBoolean(KEY_ACTIVITY_CHOOSEN, false))
            }
            
//            if (isChoosen.value) {
//                // 已经选择过，直接显示主界面
//                MainContent()
//            } else {
                // 第一次进入，显示选择界面
                ActivityChooserScreen(
                    onChooseMvvm = {
                        // 选择 MVVM 版本（当前 Activity）
                        prefs.edit { putBoolean(KEY_ACTIVITY_CHOOSEN, true) }
                        isChoosen.value = true
                    },
                    onChooseMvi = {
                        // 选择 MVI 版本，跳转到 MainMviActivity
                        prefs.edit { putBoolean(KEY_ACTIVITY_CHOOSEN, true) }
                        startActivity(Intent(this@MainActivity, MainMviActivity::class.java))
                        finish()
                    }
                )
//            }
        }
    }
}

/**
 * Activity 选择界面
 */
@Composable
fun ActivityChooserScreen(
    onChooseMvvm: () -> Unit,
    onChooseMvi: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(android.R.color.white)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "请选择架构模式",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(android.R.color.black),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "两个 Activity 的内容完全相同，只是架构模式不同",
                fontSize = 14.sp,
                color = colorResource(android.R.color.darker_gray),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // MVVM 选项
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChooseMvvm() },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(android.R.color.white)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MVVM 架构",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(android.R.color.black)
                    )
                    Text(
                        text = "使用 BaseComposeVmActivity",
                        fontSize = 14.sp,
                        color = colorResource(android.R.color.darker_gray),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "当前 MainActivity",
                        fontSize = 12.sp,
                        color = colorResource(android.R.color.darker_gray),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // MVI 选项
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChooseMvi() },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(android.R.color.white)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MVI 架构",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(android.R.color.black)
                    )
                    Text(
                        text = "使用 BaseComposeMviActivity",
                        fontSize = 14.sp,
                        color = colorResource(android.R.color.darker_gray),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "跳转到 MainMviActivity",
                        fontSize = 12.sp,
                        color = colorResource(android.R.color.darker_gray),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}