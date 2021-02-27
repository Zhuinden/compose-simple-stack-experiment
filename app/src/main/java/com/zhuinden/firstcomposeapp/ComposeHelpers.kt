package com.zhuinden.firstcomposeapp

import android.os.Parcelable
import androidx.compose.runtime.*
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.SimpleStateChanger
import com.zhuinden.simplestack.StateChange

val LocalKey = staticCompositionLocalOf<DefaultComposeKey> { throw Exception("Key should not be null") }

abstract class DefaultComposeKey: Parcelable {
    @Composable
    fun RenderComposable() {
        val key = this

        CompositionLocalProvider(LocalKey provides(key)) {
            ScreenComposable()
        }
    }

    @Composable
    protected abstract fun ScreenComposable()
}

class ComposeStack(
    private val renderer: @Composable (StateChange) -> Unit = { stateChange ->
        stateChange.topNewKey<DefaultComposeKey>().RenderComposable()
    },
): SimpleStateChanger.NavigationHandler {
    private var backstackState by mutableStateOf(BackstackState(renderer))

    override fun onNavigationEvent(stateChange: StateChange) {
        this.backstackState = BackstackState(renderer = renderer, stateChange = stateChange)
    }

    private data class BackstackState(
        private val renderer: @Composable() (StateChange) -> Unit,
        private val stateChange: StateChange? = null
    ) {
        @Composable
        fun RenderScreen() {
            val stateChange = stateChange ?: return

            renderer(stateChange)
        }
    }

    @Composable
    fun RenderScreen() {
        backstackState.RenderScreen()
    }
}

val LocalBackstack = staticCompositionLocalOf<Backstack> { throw Exception("Backstack should not be null") }

@Composable
fun BackstackProvider(backstack: Backstack, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalBackstack provides (backstack)) {
        content()
    }
}
