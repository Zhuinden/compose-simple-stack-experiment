package com.zhuinden.firstcomposeapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.zhuinden.firstcomposeapp.core.navigation.BackstackProvider
import com.zhuinden.firstcomposeapp.core.navigation.ComposeStack
import com.zhuinden.simplestack.AsyncStateChanger
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.SimpleStateChanger
import com.zhuinden.simplestack.navigator.Navigator
import com.zhuinden.simplestackextensions.navigatorktx.androidContentFrame
import com.zhuinden.simplestackextensions.services.DefaultServiceProvider

class MainActivity : AppCompatActivity() {
    private val composeStack = ComposeStack()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backstack = Navigator.configure()
            .setScopedServices(DefaultServiceProvider())
            .setStateChanger(AsyncStateChanger(composeStack))
            .install(this, androidContentFrame, History.of(FirstKey()))

        setContent {
            BackstackProvider(backstack) {
                MaterialTheme {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        composeStack.RenderScreen()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (!Navigator.onBackPressed(this)) {
            super.onBackPressed()
        }
    }
}
