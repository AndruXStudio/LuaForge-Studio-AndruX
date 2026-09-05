package com.luaforge.studio.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp

/**
 * Pure Material 3 shape scale (Google design tokens).
 * Reference: https://m3.material.io/styles/shape/shape-scale-tokens
 */
val Material3Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/**
 * Animated shape scale driven by user setting (still maps to M3 ratios).
 */
@Composable
fun createDynamicShapes(shapeSizeIndex: Int): Shapes {
    val targetBaseSize = when (shapeSizeIndex) {
        0 -> 4f
        1 -> 8f
        2 -> 12f
        3 -> 16f
        else -> 12f
    }

    val animatedBaseSize = remember { Animatable(targetBaseSize) }

    LaunchedEffect(targetBaseSize) {
        animatedBaseSize.animateTo(
            targetValue = targetBaseSize,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    val base = animatedBaseSize.value.dp
    return Shapes(
        extraSmall = RoundedCornerShape(base * 0.33f),
        small = RoundedCornerShape(base * 0.67f),
        medium = RoundedCornerShape(base),
        large = RoundedCornerShape(base * 1.33f),
        extraLarge = RoundedCornerShape(base * 2.33f),
    )
}
