package com.icontree.animate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.compose.LottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.flow.drop

/** Playback behavior, matching the npm package's `mode` prop. */
enum class IconMode {
    /** Displays the first frame. */
    Static,

    /** Plays continuously. */
    Loop,

    /** Plays once when the icon is clicked (also calls `onClick`). */
    OnClick,

    /** Plays once when a pointer enters the icon (mouse, stylus or ChromeOS/desktop hover). */
    Hover,
}

/**
 * Starts an [IconMode.OnClick] icon from some other element — the Compose equivalent of the npm
 * package's `bindId`. Create one with [rememberIconTrigger], pass it to [Icon], and call [play]
 * (for example from a Button's `onClick`).
 */
@Stable
class IconTrigger {
    internal var count by mutableIntStateOf(0)
        private set

    fun play() {
        count++
    }
}

@Composable
fun rememberIconTrigger(): IconTrigger = remember { IconTrigger() }

/**
 * An animated icon from the bundled set.
 *
 * @param id icon name, such as `"heart"` or `"arrow-right"` (see [iconIds] and [IconTree.search]).
 * @param size width and height of the icon.
 * @param color tint applied to all solid fills and strokes. [Color.Unspecified] means
 *   "current color" and follows `LocalContentColor`.
 * @param mode playback behavior.
 * @param onClick callback for [IconMode.OnClick] playback.
 * @param trigger external trigger for [IconMode.OnClick] playback (npm `bindId`).
 * @param resetAfterPlay return to the first frame after a one-shot playback completes.
 */
@Composable
fun Icon(
    id: IconId,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = Color.Unspecified,
    mode: IconMode = IconMode.Static,
    onClick: (() -> Unit)? = null,
    trigger: IconTrigger? = null,
    resetAfterPlay: Boolean = true,
) {
    val resolved = if (color == Color.Unspecified) LocalContentColor.current else color
    val argb = resolved.toArgb() or 0xFF000000.toInt()

    // Parsed once per icon and cached by Lottie; recoloring is a cheap dynamic property, so a
    // color change never re-reads or re-parses the animation JSON.
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("${IconTree.ASSET_DIR}/$id.json")
    )
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(LottieProperty.COLOR, argb, "**"),
        rememberLottieDynamicProperty(LottieProperty.STROKE_COLOR, argb, "**"),
    )
    IconBody(composition, dynamicProperties, modifier, size, mode, onClick, trigger, resetAfterPlay)
}

@Composable
private fun IconBody(
    composition: LottieComposition?,
    dynamicProperties: LottieDynamicProperties,
    modifier: Modifier,
    size: Dp,
    mode: IconMode,
    onClick: (() -> Unit)?,
    trigger: IconTrigger?,
    resetAfterPlay: Boolean,
) {
    val animatable = rememberLottieAnimatable()
    val latestOnClick by rememberUpdatedState(onClick)
    val latestReset by rememberUpdatedState(resetAfterPlay)

    // Number of one-shot plays requested since the mode last changed.
    var plays by remember(mode) { mutableIntStateOf(0) }

    LaunchedEffect(composition, mode, plays) {
        val comp = composition ?: return@LaunchedEffect
        when {
            mode == IconMode.Loop ->
                animatable.animate(comp, iterations = LottieConstants.IterateForever, initialProgress = 0f)
            plays > 0 -> {
                animatable.animate(comp, iterations = 1, initialProgress = 0f)
                if (latestReset) animatable.snapTo(comp, progress = 0f)
            }
            else -> animatable.snapTo(comp, progress = 0f)
        }
    }

    LaunchedEffect(trigger, mode) {
        if (trigger == null || mode != IconMode.OnClick) return@LaunchedEffect
        snapshotFlow { trigger.count }.drop(1).collect {
            plays++
            latestOnClick?.invoke()
        }
    }

    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    LaunchedEffect(hovered, mode) {
        if (hovered && mode == IconMode.Hover) plays++
    }

    var boxModifier = modifier.size(size)
    if (mode == IconMode.Hover) boxModifier = boxModifier.hoverable(interaction)
    if (mode == IconMode.OnClick) {
        boxModifier = boxModifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
        ) {
            plays++
            latestOnClick?.invoke()
        }
    }

    Box(boxModifier, contentAlignment = Alignment.Center) {
        if (composition != null) {
            LottieAnimation(
                composition = composition,
                progress = { animatable.progress },
                modifier = Modifier.size(size),
                renderMode = RenderMode.HARDWARE,
                dynamicProperties = dynamicProperties,
            )
        }
    }
}

