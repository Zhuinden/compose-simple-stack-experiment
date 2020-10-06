package com.zhuinden.firstcomposeapp

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Arrangement
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

data class BackstackState(val screens: List<ComposeKey>)

@Parcelize
data class FirstKey(private val placeholder: String = "") : ComposeKey() {
    @Composable
    override fun defineComposable() {
        FirstScreen()
    }
}

@Parcelize
data class SecondKey(private val placeholder: String = "") : ComposeKey() {
    @Composable
    override fun defineComposable() {
        SecondScreen()
    }
}

class MainActivity : AppCompatActivity(), SimpleStateChanger.NavigationHandler {
    private lateinit var backstack: Backstack

    private var backstackState by mutableStateOf(BackstackState(Collections.emptyList()))

    @Suppress("NAME_SHADOWING", "ControlFlowWithEmptyBody")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backstack = Navigator.configure()
            .setStateChanger(SimpleStateChanger(this))
            .setDeferredInitialization(true)
            .install(this, androidContentFrame, History.of(FirstKey("FIRST VIEW")))

        setContent {
            MaterialTheme {
                Providers(BackstackAmbient provides(backstack)) {
                    backstackState.screens.lastOrNull()?.composable()
                }
            }
        }

        Navigator.executeDeferredInitialization(this)
    }

    override fun onBackPressed() {
        if (!Navigator.onBackPressed(this)) {
            super.onBackPressed()
        }
    }

    override fun onNavigationEvent(stateChange: StateChange) {
        backstackState = backstackState.copy(screens = stateChange.getNewKeys())
    }
}

val BackstackAmbient = ambientOf<Backstack>()

val KeyAmbient = ambientOf<ComposeKey>()

@Composable
fun FirstScreen() {
    val key = KeyAmbient.current // params? or function args instead

    val backstack = BackstackAmbient.current

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalGravity = Alignment.CenterHorizontally) {
        Button(onClick = {
            // onClick is not a composition context, must get ambients above
            backstack.goTo(SecondKey())
        }, content = {
            Text("Hello First Screen!")
        })
    }
}

@Composable
fun SecondScreen() {
    val context = ContextAmbient.current

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalGravity = Alignment.CenterHorizontally) {
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
fun DefaultPreview() {
    MaterialTheme {
        FirstScreen()
    }
}