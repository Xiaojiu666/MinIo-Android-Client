package io.minio.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import io.minio.MinioClient
import io.minio.android.ui.theme.MinIoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val minioClient = MinioClient.builder().endpoint(ENDPOINT)
            .credentials(ACCESS_KEY, SECRET_KEY).build()
        val buckets = minioClient.listBuckets()
        println("buckets $buckets")
        setContent {
            MinIoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Text("MinIo-Android-Client")
                }
            }
        }
    }
}

