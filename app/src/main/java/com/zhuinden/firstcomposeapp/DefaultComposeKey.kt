package com.zhuinden.firstcomposeapp

import android.os.Parcelable
import androidx.compose.runtime.*

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