package com.luaforge.studio.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * Material 3 SwitchBar — Google Settings style row.
 */
@Composable
fun SwitchBar(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    colors: SwitchBarColors = SwitchBarDefaults.colors(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.containerColor),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = { if (enabled) onCheckedChange(!checked) },
        enabled = enabled,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = text,
                style = textStyle,
                color = colors.textColor,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
                thumbContent = if (checked) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.checkedThumbColor,
                    checkedTrackColor = colors.checkedTrackColor,
                    checkedIconColor = colors.checkedTrackColor,
                    uncheckedThumbColor = colors.uncheckedThumbColor,
                    uncheckedTrackColor = colors.untrackedTrackColor,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                    uncheckedIconColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                ),
            )
        }
    }
}

data class SwitchBarColors(
    val containerColor: Color,
    val textColor: Color,
    val checkedThumbColor: Color,
    val checkedTrackColor: Color,
    val uncheckedThumbColor: Color,
    val untrackedTrackColor: Color,
)

object SwitchBarDefaults {
    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
        textColor: Color = MaterialTheme.colorScheme.onSurface,
        checkedThumbColor: Color = MaterialTheme.colorScheme.onPrimary,
        checkedTrackColor: Color = MaterialTheme.colorScheme.primary,
        uncheckedThumbColor: Color = MaterialTheme.colorScheme.outline,
        uncheckedTrackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ): SwitchBarColors = SwitchBarColors(
        containerColor = containerColor,
        textColor = textColor,
        checkedThumbColor = checkedThumbColor,
        checkedTrackColor = checkedTrackColor,
        uncheckedThumbColor = uncheckedThumbColor,
        untrackedTrackColor = uncheckedTrackColor,
    )
}
