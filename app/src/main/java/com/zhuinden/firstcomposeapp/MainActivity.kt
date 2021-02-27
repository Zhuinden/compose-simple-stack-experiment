package com.zhuinden.firstcomposeapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.SimpleStateChanger
import com.zhuinden.simplestack.StateChange
import com.zhuinden.simplestack.navigator.Navigator
import com.zhuinden.simplestackextensions.navigatorktx.androidContentFrame
import com.zhuinden.simplestackextensions.services.DefaultServiceProvider


private data class BackstackState(private val stateChange: StateChange? = null) {
    @Composable
    fun RenderScreen() {
        stateChange?.topNewKey<DefaultComposeKey>()?.RenderComposable()
    }
}

val LocalBackstack = staticCompositionLocalOf<Backstack> { throw Exception("Backstack should not be null") }

@Composable
fun BackstackProvider(backstack: Backstack, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalBackstack provides (backstack)) {
        content()
    }
}

class MainActivity : AppCompatActivity(), SimpleStateChanger.NavigationHandler {
    private var backstackState by mutableStateOf(BackstackState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backstack = Navigator.configure()
            .setScopedServices(DefaultServiceProvider())
            .setStateChanger(SimpleStateChanger(this))
            .install(this, androidContentFrame, History.of(FirstKey()))

        setContent {
            BackstackProvider(backstack) {
                MaterialTheme {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        backstackState.RenderScreen()
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
