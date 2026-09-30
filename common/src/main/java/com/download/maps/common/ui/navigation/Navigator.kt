package com.download.maps.common.ui.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

class Navigator(private val backStack: NavBackStack<NavKey>) {
    fun navigate(destination: NavKey) {
        backStack.add(destination)
    }

    fun pop() {
        pop(to = backStack.last())
    }

    fun pop(
        to: NavKey,
        inclusive: Boolean = true
    ) {
        backStack.removeAll(
            backStack.subList(
                if (inclusive) {
                    backStack.lastIndexOf(to)
                } else {
                    backStack.lastIndexOf(to) + 1
                }.coerceIn(backStack.indices),
                backStack.lastIndex + 1
            )
        )
    }
}

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("Navigator not provided!") }
