package com.example.iptvhost.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class TvActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TvRoot()
        }
    }
}

@Composable
fun TvRoot() {
    MaterialTheme {
        Surface {
            Text("Hello IPTV Host (TV)")
        }
    }
}

@Preview
@Composable
fun PreviewTv() {
    TvRoot()
}