package com.minimal.launcher.model

data class AppItem(
    val label: String,
    val packageName: String,
    val componentName: String,
    val isHidden: Boolean = false,
    val isFavorite: Boolean = false
)
