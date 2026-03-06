package com.resukisu.resukisu.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.backdrop.shadow.Shadow
import com.kyant.capsule.ContinuousRoundedRectangle
import com.resukisu.resukisu.ui.theme.ThemeConfig
import com.resukisu.resukisu.ui.theme.getCardColors
import com.resukisu.resukisu.ui.theme.getCardElevation
import com.resukisu.resukisu.ui.util.LocalWallpaperBackdrop
import com.resukisu.resukisu.ui.util.ui.InteractiveHighlight
import com.resukisu.resukisu.ui.util.ui.rememberUISensor

/**
 * A card that renders as liquid glass when a custom background is loaded,
 * and falls back to ElevatedCard otherwise.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    if (ThemeConfig.backgroundImageLoaded) {
        val backdrop = LocalWallpaperBackdrop.current
        val animationScope = rememberCoroutineScope()
        val interactiveHighlight = remember(animationScope) {
            InteractiveHighlight(animationScope = animationScope)
        }

        val uiSensor = rememberUISensor()
        val highlightAngle by animateFloatAsState(
            targetValue = uiSensor?.gravityAngle ?: 45f,
            animationSpec = tween(400)
        )

        Box(
            modifier = modifier
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { ContinuousRoundedRectangle(16.dp) },
                    effects = {
                        vibrancy()
                        blur(8.dp.toPx())
                        lens(12f.dp.toPx(), 24f.dp.toPx())
                    },
                    highlight = {
                        Highlight(
                            style = HighlightStyle.Default(angle = highlightAngle),
                            alpha = 0.6f
                        )
                    },
                    shadow = { Shadow() },
                    onDrawSurface = {
                        if (tint.isSpecified) {
                            drawRect(tint, blendMode = BlendMode.Hue)
                            drawRect(tint.copy(alpha = 0.87f))
                        }
                    }
                )
                .then(
                    if (onClick != null) {
                        Modifier
                            .then(interactiveHighlight.modifier)
                            .then(interactiveHighlight.gestureModifier)
                            .clickable(onClick = onClick)
                    } else Modifier
                ),
            content = content
        )
    } else {
        ElevatedCard(
            modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
            colors = if (tint.isSpecified) getCardColors(tint) else CardDefaults.elevatedCardColors(),
            elevation = getCardElevation(),
        ) {
            Box(content = content)
        }
    }
}
