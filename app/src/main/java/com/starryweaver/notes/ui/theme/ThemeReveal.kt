package com.starryweaver.notes.ui.theme

import android.graphics.Bitmap
import android.graphics.Canvas
import android.provider.Settings
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.starryweaver.notes.data.ThemeColor
import com.starryweaver.notes.data.ThemeMode
import kotlin.math.hypot

/** 一次主题揭示请求：触点位置 + 切换前的界面截图。 */
data class RevealRequest(
    val origin: Offset,
    val snapshot: ImageBitmap,
    val id: Long,
)

/** 由设置页写入触点与截图，动画消费后清空。 */
class ThemeRevealState {
    var request: RevealRequest? by mutableStateOf(null)
        private set

    fun requestReveal(origin: Offset, snapshot: ImageBitmap?) {
        if (snapshot == null) return
        request = RevealRequest(origin, snapshot, System.nanoTime())
    }

    fun clear() {
        request = null
    }
}

val LocalThemeReveal = staticCompositionLocalOf { ThemeRevealState() }

private const val REVEAL_DURATION_MILLIS = 750

/** 把当前界面抓成位图，作为切换前那一层的内容。 */
fun captureToImageBitmap(view: View): ImageBitmap? = runCatching {
    if (view.width <= 0 || view.height <= 0) return null
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    view.draw(Canvas(bitmap))
    bitmap.asImageBitmap()
}.getOrNull()

/**
 * 日夜切换：底层是已生效的新主题（单次组合，不做双树渲染），
 * 上层叠一张切换前的界面截图，并用「整屏矩形减去扩散圆」的遮罩把它挡在圆外，
 * 圆从触点扩大即逐步露出新主题。遮罩层只做绘制、不参与触摸，事件照常穿透到界面。
 */
@Composable
fun RevealTheme(
    themeMode: ThemeMode,
    dynamicColor: Boolean,
    themeColor: ThemeColor,
    customColor: Color,
    content: @Composable () -> Unit,
) {
    val reveal = LocalThemeReveal.current
    val colorScheme = rememberColorScheme(
        dark = resolveDark(themeMode),
        dynamicColor = dynamicColor,
        themeColor = themeColor,
        customColor = customColor,
    )

    val context = LocalContext.current
    val reduceMotion = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }

    val progress = remember { Animatable(1f) }
    val request = reveal.request

    LaunchedEffect(request?.id) {
        if (request == null || reduceMotion) {
            reveal.clear()
            progress.snapTo(1f)
            return@LaunchedEffect
        }
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = REVEAL_DURATION_MILLIS,
                easing = FastOutSlowInEasing,
            ),
        )
        reveal.clear()
        progress.snapTo(1f)
    }

    MaterialTheme(colorScheme = colorScheme, typography = NotesTypography) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()

            if (request != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            val fraction = progress.value
                            if (fraction >= 1f) return@drawBehind
                            val radius = fraction * maxRadius(request.origin, size.width, size.height)
                            val mask = Path().apply {
                                op(
                                    Path().apply { addRect(Rect(0f, 0f, size.width, size.height)) },
                                    Path().apply { addOval(Rect(request.origin, radius)) },
                                    PathOperation.Difference,
                                )
                            }
                            clipPath(mask) {
                                drawImage(
                                    image = request.snapshot,
                                    srcOffset = IntOffset.Zero,
                                    srcSize = IntSize(request.snapshot.width, request.snapshot.height),
                                    dstOffset = IntOffset.Zero,
                                    dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                                    filterQuality = FilterQuality.None,
                                )
                            }
                        },
                )
            }
        }
    }
}

private fun maxRadius(origin: Offset, width: Float, height: Float): Float {
    val dx = maxOf(origin.x, width - origin.x)
    val dy = maxOf(origin.y, height - origin.y)
    return hypot(dx, dy)
}
