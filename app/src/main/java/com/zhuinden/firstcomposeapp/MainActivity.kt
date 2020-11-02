package com.zhuinden.firstcomposeapp

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.setContent
import androidx.ui.tooling.preview.Preview
import com.zhuinden.simplestack.*
import com.zhuinden.simplestack.navigator.Navigator
import com.zhuinden.simplestackextensions.navigatorktx.androidContentFrame
import kotlinx.android.parcel.Parcelize
import java.util.*


private data class BackstackState(val stateChange: StateChange?)

val BackstackAmbient = ambientOf<Backstack>()

class MainActivity : AppCompatActivity(), SimpleStateChanger.NavigationHandler {
    private lateinit var backstack: Backstack

    private var backstackState by mutableStateOf(BackstackState(null))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backstack = Navigator.configure()
            .setStateChanger(SimpleStateChanger(this))
            .install(this, androidContentFrame, History.of(FirstKey()))

        setContent {
            Providers(BackstackAmbient provides (backstack)) {
                MaterialTheme {
                    Box(Modifier.fillMaxSize(), alignment = Alignment.Center) {
                        backstackState.stateChange?.topNewKey<ComposeKey>()?.composable()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (!Navigator.onBackPressed(this)) {
            super.onBackPressed()
        }
    }

    override fun onNavigationEvent(stateChange: StateChange) {
        backstackState = backstackState.copy(stateChange = stateChange)
    }
}