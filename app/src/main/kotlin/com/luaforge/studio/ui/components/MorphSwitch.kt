package com.luaforge.studio.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Material 3 Switch with animated check / close thumb icons.
 * ON  → 对号，OFF → 叉号，带缩放与交叉淡入淡出。
 */
@Composable
fun MorphSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val scale by animateFloatAsState(
        targetValue = if (checked) 1f else 0.96f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "morphSwitchScale",
    )

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.scale(scale),
        enabled = enabled,
        interactionSource = interactionSource,
        thumbContent = {
            AnimatedContent(
                targetState = checked,
                transitionSpec = {
                    (fadeIn(tween(160)) + scaleIn(initialScale = 0.6f, animationSpec = tween(180)))
                        .togetherWith(fadeOut(tween(120)) + scaleOut(targetScale = 0.6f, animationSpec = tween(120)))
                },
                label = "morphSwitchThumb",
            ) { on ->
                val icon: ImageVector = if (on) Icons.Filled.Check else Icons.Filled.Close
                Icon(
                    imageVector = icon,
                    contentDescription = if (on) "开启" else "关闭",
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        },
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            checkedBorderColor = MaterialTheme.colorScheme.primary,
            checkedIconColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            uncheckedBorderColor = MaterialTheme.colorScheme.outline,
            uncheckedIconColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
    )
}
