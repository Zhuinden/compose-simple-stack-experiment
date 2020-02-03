package com.zhuinden.firstcomposeapp

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.*
import androidx.ui.core.ContextAmbient
import androidx.ui.core.setContent
import androidx.ui.layout.Container
import androidx.ui.material.Button
import androidx.ui.material.MaterialTheme
import androidx.ui.tooling.preview.Preview
import com.zhuinden.simplestack.*
import com.zhuinden.simplestack.navigator.Navigator
import java.util.*

@Model
class BackstackState(var screens: List<ComposeKey>)

data class FirstKey(private val placeholder: String = "") : ComposeKey() {
    @Composable
    override fun realComposable() {
        FirstScreen()
    }
}

data class SecondKey(private val placeholder: String = "") : ComposeKey() {
    @Composable
    override fun realComposable() {
        SecondScreen()
    }
}

class MainActivity : AppCompatActivity(), SimpleStateChanger.NavigationHandler {
    private lateinit var backstack: Backstack

    val backstackState = BackstackState(Collections.emptyList())

    @Suppress("NAME_SHADOWING", "ControlFlowWithEmptyBody")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contentView =
            window.decorView.findViewById<ViewGroup>(android.R.id.content) // might lax this later

        backstack = Navigator.configure()
            .setStateChanger(SimpleStateChanger(this))
            .setDeferredInitialization(true)
            .setKeyParceler(GsonParceler())
            .install(this, contentView, History.of(FirstKey("FIRST VIEW")))

        setContent {
            MaterialTheme {
                BackstackAmbient.Provider(value = backstack) {
                    backstackState.screens.takeIf { it.isNotEmpty() }?.also { history ->
                        val top = history.last()
                        top.composable() // using `screens.lastOrNull()?.composable() breaks the compiler
                    }
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
        backstackState.screens = stateChange.getNewKeys<ComposeKey>()
    }
}

val BackstackAmbient = Ambient.of<Backstack>()

val KeyAmbient = Ambient.of<ComposeKey>()

@Composable
fun FirstScreen() {
    val key = ambient(KeyAmbient) // params? or function args instead

    val backstack = ambient(BackstackAmbient)

    Container {
        Button("Hello First Screen!", onClick = {
            // onClick is not a composition context, must get ambients above
            backstack.goTo(SecondKey())
        })
    }
}

@Composable
fun SecondScreen() {
    val context = ambient(ContextAmbient)

    Container {
        Button("Hello Second Screen!", onClick = {
            // onClick is not a composition context, must get ambients above
            Toast.makeText(context, "Blah", Toast.LENGTH_LONG).show()
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