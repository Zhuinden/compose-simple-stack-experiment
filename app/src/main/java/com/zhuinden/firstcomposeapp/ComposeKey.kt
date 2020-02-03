package com.zhuinden.firstcomposeapp

import android.os.Parcelable
import androidx.compose.Composable

abstract class ComposeKey {
    @Composable
    fun composable() {
        KeyAmbient.Provider(value = this) {
            realComposable()
        }
    }

    @Composable
    abstract fun realComposable()
}