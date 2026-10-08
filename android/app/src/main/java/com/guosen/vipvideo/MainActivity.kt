package com.guosen.vipvideo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.guosen.vipvideo.ui.VipVideoRoot
import com.guosen.vipvideo.ui.theme.VipVideoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val isTv = BuildConfig.FLAVOR == "tv"
        setContent {
            VipVideoTheme {
                VipVideoRoot(isTv = isTv)
            }
        }
    }
}
