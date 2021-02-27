package com.zhuinden.firstcomposeapp

import android.os.Parcelable
import androidx.compose.runtime.*

val LocalKey = staticCompositionLocalOf<ComposeKey> { throw Exception("Key should not be null") }

abstract class ComposeKey: Parcelable {
    @Composable
    fun ScreenComposable() {
        val key = this

        CompositionLocalProvider(LocalKey provides(key)) {
            AssociatedComposable()
        }
    }

    @Composable
    protected abstract fun AssociatedComposable()
}