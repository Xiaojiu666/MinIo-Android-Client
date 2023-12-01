package io.minio.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import io.minio.android.base.ui.theme.MinIoTheme
import io.minio.android.workflow.HomeNav

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MinIoTheme {
                HomeNav()
            }
        }
    }
}

