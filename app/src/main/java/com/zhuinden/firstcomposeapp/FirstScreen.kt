package com.zhuinden.firstcomposeapp

import android.annotation.SuppressLint
import androidx.compose.material.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.ServiceBinder
import com.zhuinden.simplestackextensions.services.DefaultServiceProvider
import com.zhuinden.simplestackextensions.servicesktx.add
import com.zhuinden.simplestackextensions.servicesktx.lookup
import com.zhuinden.simplestackextensions.servicesktx.rebind
import kotlinx.parcelize.Parcelize
import androidx.compose.ui.tooling.preview.Preview

class FirstModel(
    private val backstack: Backstack
): FirstScreen.ActionHandler {
    override fun doSomething() {
        backstack.goTo(SecondKey())
    }
}

@Parcelize
data class FirstKey(val title: String) : ComposeKey(), DefaultServiceProvider.HasServices {
    constructor() : this("Hello First Screen!")

    @Composable
    override fun AssociatedComposable() {
        FirstScreen(title)
    }

    override fun getScopeTag(): String = javaClass.name

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            val firstModel = FirstModel(backstack)

            add(firstModel)
            rebind<FirstScreen.ActionHandler>(firstModel)
        }
    }
}

class FirstScreen private constructor() {
    fun interface ActionHandler {
        fun doSomething()
    }

    companion object {
        @Composable
        @SuppressLint("ComposableNaming")
        operator fun invoke(title: String) {
            val backstack = LocalBackstack.current

            val eventHandler = remember { backstack.lookup<ActionHandler>() }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = {
                    // onClick is not a composition context, must get ambients above
                    eventHandler.doSomething()
                }, content = {
                    Text(title)
                })
            }
        }
    }
}

@Preview
@Composable
fun FirstScreenPreview() {
    MaterialTheme {
        FirstScreen("This is a preview")
    }
}