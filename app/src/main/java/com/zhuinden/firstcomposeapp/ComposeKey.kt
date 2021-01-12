package com.zhuinden.firstcomposeapp

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.ambientOf

val AmbientKey = ambientOf<ComposeKey>()

abstract class ComposeKey: Parcelable {
    @Composable
    fun composable() {
        val key = this

        Providers(AmbientKey provides(key)) {
            defineComposable()
        }
    }

    @Composable
    protected abstract fun defineComposable()
}