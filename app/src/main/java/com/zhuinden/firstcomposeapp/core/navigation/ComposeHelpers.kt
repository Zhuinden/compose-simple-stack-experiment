package com.zhuinden.firstcomposeapp.core.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import com.zhuinden.simplestack.*
import com.zhuinden.simplestackextensions.servicesktx.lookup
import kotlinx.coroutines.launch

val LocalKey =
    staticCompositionLocalOf<DefaultComposeKey> { throw Exception("Key should not be null") }

abstract class DefaultComposeKey {
    @Composable
    fun RenderComposable(modifier: Modifier = Modifier) {
        val key = this

        CompositionLocalProvider(LocalKey provides (key)) {
            ScreenComposable(modifier = modifier)
        }
    }

    @Composable
    protected abstract fun ScreenComposable(modifier: Modifier = Modifier)
}

class ComposeStack(
    private val renderer: @Composable (Modifier, StateChange, StateChanger.Callback) -> Unit = renderer@{ modifier, stateChange, completionCallback ->
        val callback by rememberUpdatedState(newValue = completionCallback)

        val topNewKey = stateChange.topNewKey<DefaultComposeKey>()
        val topPreviousKey = stateChange.topPreviousKey<DefaultComposeKey>()

        if (topPreviousKey == null) {
            topNewKey.RenderComposable(modifier)

            DisposableEffect(key1 = callback, effect = {
                callback.stateChangeComplete()

                onDispose {
                    // do nothing
                }
            })

            return@renderer
        }

        var isAnimating by remember { mutableStateOf(true) } // true renders previous immediately

        val scope = rememberCoroutineScope()

        val lerping = Animatable(0.0f, Float.VectorConverter, 1.0f) // DO NOT REMEMBER!

        var animationProgress by remember { mutableStateOf(0.0f) }

        var fullWidth by remember { mutableStateOf(0) }

        val measurePolicy = MeasurePolicy { measurables, constraints ->
            val placeables = measurables.fastMap { it.measure(constraints) }
            val maxWidth = placeables.fastMaxBy { it.width }?.width ?: 0
            val maxHeight = placeables.fastMaxBy { it.height }?.height ?: 0

            if (fullWidth == 0) {
                fullWidth = maxWidth
            }

            layout(maxWidth, maxHeight) {
                placeables.fastForEach { placeable ->
                    placeable.place(0, 0)
                }
            }
        }

        Layout(
            content = {
                if (fullWidth > 0) {
                    topNewKey.RenderComposable(modifier)
                }
            }, measurePolicy = measurePolicy, modifier = modifier.then(
                when(stateChange.direction) {
                    StateChange.FORWARD -> Modifier.graphicsLayer(translationX = fullWidth + (-1) * fullWidth * animationProgress)
                    StateChange.BACKWARD -> Modifier.graphicsLayer(translationX = -1 * fullWidth + fullWidth * animationProgress)
                    else /* REPLACE */ -> Modifier.graphicsLayer(alpha = 0 + animationProgress)
                }
            )
        )

        Layout(
            content = {
                if (isAnimating) {
                    topPreviousKey.RenderComposable(modifier)
                }
            }, measurePolicy = measurePolicy, modifier = modifier.then(
                when(stateChange.direction) {
                    StateChange.FORWARD -> Modifier.graphicsLayer(translationX = 0 + (-1) * fullWidth * animationProgress)
                    StateChange.BACKWARD -> Modifier.graphicsLayer(translationX = 0 + fullWidth * animationProgress)
                    else /* REPLACE */ -> Modifier.graphicsLayer(alpha = (1 - animationProgress))
                }
            )
        )

        DisposableEffect(key1 = callback, effect = {
            scope.launch {
                isAnimating = true
                animationProgress = 0.0f
                lerping.animateTo(1.0f, TweenSpec(325, 0, LinearEasing)) {
                    animationProgress = this.value
                }
                isAnimating = false
                callback.stateChangeComplete()
            }

            onDispose {
                // do nothing
            }
        })
    },
) : AsyncStateChanger.NavigationHandler {
    private var backstackState by mutableStateOf(BackstackState(renderer))

    override fun onNavigationEvent(stateChange: StateChange, callback: StateChanger.Callback) {
        this.backstackState =
            BackstackState(renderer = renderer, callback = callback, stateChange = stateChange)
    }

    private data class BackstackState(
        private val renderer: @Composable (Modifier, StateChange, StateChanger.Callback) -> Unit,
        private val stateChange: StateChange? = null,
        private val callback: StateChanger.Callback? = null
    ) {
        @Composable
        fun RenderScreen(modifier: Modifier = Modifier) {
            val stateChange = stateChange ?: return
            val callback = callback ?: return

            renderer(modifier, stateChange, callback)
        }
    }

    @Composable
    fun RenderScreen(modifier: Modifier = Modifier) {
        backstackState.RenderScreen(modifier)
    }
}

val LocalBackstack =
    staticCompositionLocalOf<Backstack> { throw Exception("Backstack should not be null") }

@Composable
fun BackstackProvider(backstack: Backstack, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalBackstack provides (backstack)) {
        content()
    }
}

@Composable
inline fun <reified T> rememberService(serviceTag: String = T::class.java.name): T {
    val backstack = LocalBackstack.current

    return remember { backstack.lookup(serviceTag) }
}