package com.zhuinden.firstcomposeapp.core.navigation

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import com.zhuinden.simplestack.*
import com.zhuinden.simplestackextensions.servicesktx.lookup
import kotlinx.coroutines.launch

val LocalComposeKey =
    staticCompositionLocalOf<DefaultComposeKey> { throw IllegalStateException("Key does not exist in this composable scope") }

abstract class DefaultComposeKey {
    @Composable
    fun RenderComposable(modifier: Modifier = Modifier) {
        val key = this

        CompositionLocalProvider(LocalComposeKey provides (key)) {
            ScreenComposable(modifier = modifier)
        }
    }

    @Composable
    protected abstract fun ScreenComposable(modifier: Modifier = Modifier)
}

class ComposeStateChanger(
    private val configuration: Configuration = Configuration()
) : StateChanger {
    private var backstackState by mutableStateOf(BackstackState(configuration = configuration))

    override fun handleStateChange(
        stateChange: StateChange,
        completionCallback: StateChanger.Callback
    ) {
        if (stateChange.isTopNewKeyEqualToPrevious) { // simpler consumer API if I don't use AsyncStateChanger here
            completionCallback.stateChangeComplete()
            return
        }

        this.backstackState =
            BackstackState(
                configuration = configuration,
                stateChange = stateChange,
                callback = completionCallback,
            )
    }

    class Configuration(
        val animationSpec: FiniteAnimationSpec<Float> = TweenSpec(325, 0, LinearEasing),
        val customComposableTransitions: CustomComposableTransitions = CustomComposableTransitions()
    ) {
        class CustomComposableTransitions(
            val previousComposableTransition: PreviousComposableTransition =
                @Suppress("RedundantSamConstructor")
                PreviousComposableTransition { modifier, stateChange, fullWidth, animationProgress ->
                    modifier.then(
                        when (stateChange.direction) {
                            StateChange.FORWARD -> Modifier.graphicsLayer(translationX = 0 + (-1) * fullWidth * animationProgress)
                            StateChange.BACKWARD -> Modifier.graphicsLayer(translationX = 0 + fullWidth * animationProgress)
                            else /* REPLACE */ -> Modifier.graphicsLayer(alpha = (1 - animationProgress))
                        }
                    )
                },
            val newComposableTransition: NewComposableTransition =
                @Suppress("RedundantSamConstructor")
                NewComposableTransition { modifier, stateChange, fullWidth, animationProgress ->
                    modifier.then(
                        when (stateChange.direction) {
                            StateChange.FORWARD -> Modifier.graphicsLayer(translationX = fullWidth + (-1) * fullWidth * animationProgress)
                            StateChange.BACKWARD -> Modifier.graphicsLayer(translationX = -1 * fullWidth + fullWidth * animationProgress)
                            else /* REPLACE */ -> Modifier.graphicsLayer(alpha = 0 + animationProgress)
                        }
                    )
                },
        ) {
            fun interface PreviousComposableTransition {
                @SuppressLint("ModifierFactoryExtensionFunction")
                fun animatePreviousComposable(
                    modifier: Modifier,
                    stateChange: StateChange,
                    fullWidth: Int,
                    animationProgress: Float
                ): Modifier
            }

            fun interface NewComposableTransition {
                @SuppressLint("ModifierFactoryExtensionFunction")
                fun animateNewComposable(
                    modifier: Modifier,
                    stateChange: StateChange,
                    fullWidth: Int,
                    animationProgress: Float
                ): Modifier
            }
        }
    }

    private data class BackstackState(
        private val configuration: Configuration,
        private val stateChange: StateChange? = null,
        private val callback: StateChanger.Callback? = null,
    ) {
        @Composable
        fun RenderScreen(modifier: Modifier = Modifier) {
            val stateChange = stateChange ?: return
            val callback = callback ?: return

            val completionCallback by rememberUpdatedState(newValue = callback)

            val topNewKey = stateChange.topNewKey<DefaultComposeKey>()
            val topPreviousKey = stateChange.topPreviousKey<DefaultComposeKey>()

            if (topPreviousKey == null) {
                key(topNewKey) {
                    topNewKey.RenderComposable(modifier)
                }

                DisposableEffect(key1 = completionCallback, effect = {
                    completionCallback.stateChangeComplete()

                    onDispose {
                        // do nothing
                    }
                })

                return
            }

            var isAnimating by remember { mutableStateOf(true) } // true renders previous initially for fullWidth

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
                        key(topNewKey) {
                            topNewKey.RenderComposable(modifier)
                        }
                    }
                },
                measurePolicy = measurePolicy,
                modifier = when {
                    !isAnimating -> modifier
                    else -> configuration.customComposableTransitions.newComposableTransition.animateNewComposable(
                        modifier,
                        stateChange,
                        fullWidth,
                        animationProgress,
                    )
                }
            )

            Layout(
                content = {
                    if (isAnimating) {
                        key(topPreviousKey) {
                            topPreviousKey.RenderComposable(modifier)
                        }
                    }
                },
                measurePolicy = measurePolicy,
                modifier = when {
                    !isAnimating -> modifier
                    else -> configuration.customComposableTransitions.previousComposableTransition.animatePreviousComposable(
                        modifier,
                        stateChange,
                        fullWidth,
                        animationProgress,
                    )
                }
            )

            DisposableEffect(key1 = completionCallback, effect = {
                scope.launch {
                    isAnimating = true
                    animationProgress = 0.0f
                    lerping.animateTo(1.0f, configuration.animationSpec) {
                        animationProgress = this.value
                    }
                    isAnimating = false
                    completionCallback.stateChangeComplete()
                }

                onDispose {
                    // do nothing
                }
            })
        }
    }

    @Composable
    fun RenderScreen(modifier: Modifier = Modifier) {
        LocalBackstack.current // force `BackstackProvider` to be set

        backstackState.RenderScreen(modifier)
    }
}

val LocalBackstack =
    staticCompositionLocalOf<Backstack> { throw IllegalStateException("You must ensure that the BackstackProvider provides the backstack, but it currently doesn't exist.") }

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