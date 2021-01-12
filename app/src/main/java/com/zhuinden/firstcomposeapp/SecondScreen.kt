package com.zhuinden.firstcomposeapp

import android.widget.Toast
import androidx.compose.material.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.parcelize.Parcelize


@Parcelize
data class SecondKey(private val noArgsPlaceholder: String = "") : ComposeKey() {
    @Composable
    override fun defineComposable() {
        SecondScreen()
    }
}

@Composable
fun SecondScreen() {
    val context = AmbientContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            // onClick is not a composition context, must get ambients above
            Toast.makeText(context, "Blah", Toast.LENGTH_LONG).show()
        }, content = {
            Text("Hello Second Screen!")
        })
    }
}

@Preview
@Composable
fun SecondScreenPreview() {
    MaterialTheme {
        SecondScreen()
    }
}