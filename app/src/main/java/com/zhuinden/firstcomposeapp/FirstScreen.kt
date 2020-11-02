package com.zhuinden.firstcomposeapp

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.ui.tooling.preview.Preview
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FirstKey(val title: String) : ComposeKey() {
    constructor() : this("Hello First Screen!")

    @Composable
    override fun defineComposable() {
        FirstScreen(title)
    }
}

@Composable
fun FirstScreen(title: String) {
    val backstack = BackstackAmbient.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            // onClick is not a composition context, must get ambients above
            backstack.goTo(SecondKey())
        }, content = {
            Text(title)
        })
    }
}

@Preview
@Composable
fun FirstScreenPreview() {
    MaterialTheme {
        FirstScreen("This is a preview")
    }
}