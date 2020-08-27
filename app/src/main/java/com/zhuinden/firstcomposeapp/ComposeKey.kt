package com.zhuinden.firstcomposeapp

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers

abstract class ComposeKey: Parcelable {
    @Composable
    fun composable() {
        val key = this

        Providers(KeyAmbient provides(key)) {
            defineComposable()
        }
    }

    @Composable
    protected abstract fun defineComposable()
}