package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.data.model.ArrowMazeItem
import com.example.data.model.ArrowSkin
import com.example.data.model.ArrowStatus
import com.example.data.model.Direction
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

@Composable
fun ArrowMazeBoard(
    arrows: List<ArrowMazeItem>,
    rows: Int,
    cols: Int,
    skin: ArrowSkin,
    hintedArrowId: Int?,
    onArrowTapped: (ArrowMazeItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // Selected skin color applies to ENTIRE arrow (body and head)
    val skinPrimaryColor = Color(skin.primaryColorHex)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("arrow_maze_canvas")
            .pointerInput(arrows, rows, cols) {
                detectTapGestures { tapOffset ->
                    val cellW = size.width / cols.toFloat()
                    val cellH = size.height / rows.toFloat()

                    val tappedArrow = findArrowAtTap(
                        tapOffset = tapOffset,
                        arrows = arrows,
                        cellW = cellW,
                        cellH = cellH
                    )
                    if (tappedArrow != null) {
                        onArrowTapped(tappedArrow)
                    }
                }
            }
    ) {
        val cellW = size.width / cols.toFloat()
        val cellH = size.height / rows.toFloat()

        // Doubled width as requested: from 0.0425f to 0.085f
        val strokeW = max(3.0f, min(cellW, cellH) * 0.085f)
        val headSize = strokeW * 2.8f

        for (arrow in arrows) {
            if (arrow.isCleared) continue

            val isHinted = arrow.id == hintedArrowId

            // Entire arrow color:
            // 1. Wrong click -> Red
            // 2. Correct click -> Green (then slithers like a snake)
            // 3. Normal -> Selected skin color for the ENTIRE arrow!
            val arrowColor = when (arrow.status) {
                ArrowStatus.WRONG_RED -> Color(0xFFE53935)
                ArrowStatus.CORRECT_GREEN, ArrowStatus.FLYING -> Color(0xFF00E676)
                ArrowStatus.NORMAL -> if (isHinted) Color(0xFFFFD54F) else skinPrimaryColor
            }

            drawSnakeArrow(
                arrow = arrow,
                cellW = cellW,
                cellH = cellH,
                strokeW = strokeW,
                headSize = headSize,
                color = arrowColor
            )
        }
    }
}

private fun DrawScope.drawSnakeArrow(
    arrow: ArrowMazeItem,
    cellW: Float,
    cellH: Float,
    strokeW: Float,
    headSize: Float,
    color: Color
) {
    if (arrow.points.size < 2) return

    val progress = arrow.flyOffset // 0f to 1f animated slithering progress
    val isFlying = arrow.isFlying || progress > 0f

    // Snake slithering shift along exit direction
    val exitDistance = maxOf(size.width, size.height) * 0.85f
    val flyShiftX = if (isFlying) arrow.headDirection.deltaCol * exitDistance * progress else 0f
    val flyShiftY = if (isFlying) arrow.headDirection.deltaRow * exitDistance * progress else 0f

    // Shake shift for wrong red tap
    val shakeShiftX = if (arrow.isShaking || arrow.status == ArrowStatus.WRONG_RED) 5f else 0f

    val path = Path()
    val pointsCount = arrow.points.size

    // Tail contraction effect as snake slithers forward
    val tailSkipFraction = if (isFlying) progress.coerceIn(0f, 0.7f) else 0f
    val startIdx = (pointsCount * tailSkipFraction).toInt().coerceIn(0, pointsCount - 2)

    val firstPt = arrow.points[startIdx]
    val startX = firstPt.col * cellW + cellW / 2f + flyShiftX + shakeShiftX
    val startY = firstPt.row * cellH + cellH / 2f + flyShiftY

    path.moveTo(startX, startY)

    for (i in (startIdx + 1) until pointsCount) {
        val pt = arrow.points[i]
        val px = pt.col * cellW + cellW / 2f + flyShiftX + shakeShiftX
        val py = pt.row * cellH + cellH / 2f + flyShiftY
        path.lineTo(px, py)
    }

    // Draw the continuous body with doubled thickness & selected skin color
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeW,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Draw the arrowhead (same selected skin color for the entire arrow!)
    val headPt = arrow.points.last()
    val headX = headPt.col * cellW + cellW / 2f + flyShiftX + shakeShiftX
    val headY = headPt.row * cellH + cellH / 2f + flyShiftY

    drawArrowHead(
        headX = headX,
        headY = headY,
        dir = arrow.headDirection,
        headSize = headSize,
        color = color
    )
}

private fun DrawScope.drawArrowHead(
    headX: Float,
    headY: Float,
    dir: Direction,
    headSize: Float,
    color: Color
) {
    val halfW = headSize * 0.5f
    val headPath = Path()

    when (dir) {
        Direction.UP -> {
            headPath.moveTo(headX, headY - headSize * 0.65f)
            headPath.lineTo(headX - halfW, headY + headSize * 0.35f)
            headPath.lineTo(headX + halfW, headY + headSize * 0.35f)
            headPath.close()
        }
        Direction.DOWN -> {
            headPath.moveTo(headX, headY + headSize * 0.65f)
            headPath.lineTo(headX - halfW, headY - headSize * 0.35f)
            headPath.lineTo(headX + halfW, headY - headSize * 0.35f)
            headPath.close()
        }
        Direction.LEFT -> {
            headPath.moveTo(headX - headSize * 0.65f, headY)
            headPath.lineTo(headX + headSize * 0.35f, headY - halfW)
            headPath.lineTo(headX + headSize * 0.35f, headY + halfW)
            headPath.close()
        }
        Direction.RIGHT -> {
            headPath.moveTo(headX + headSize * 0.65f, headY)
            headPath.lineTo(headX - headSize * 0.35f, headY - halfW)
            headPath.lineTo(headX - headSize * 0.35f, headY + halfW)
            headPath.close()
        }
    }

    drawPath(path = headPath, color = color)
}

private fun findArrowAtTap(
    tapOffset: Offset,
    arrows: List<ArrowMazeItem>,
    cellW: Float,
    cellH: Float
): ArrowMazeItem? {
    val touchThreshold = maxOf(cellW, cellH) * 1.5f
    var closestArrow: ArrowMazeItem? = null
    var minDistance = Float.MAX_VALUE

    for (arrow in arrows) {
        if (arrow.isCleared) continue

        for (i in 0 until arrow.points.size - 1) {
            val p1 = arrow.points[i]
            val p2 = arrow.points[i + 1]

            val x1 = p1.col * cellW + cellW / 2f
            val y1 = p1.row * cellH + cellH / 2f
            val x2 = p2.col * cellW + cellW / 2f
            val y2 = p2.row * cellH + cellH / 2f

            val dist = distanceToSegment(tapOffset.x, tapOffset.y, x1, y1, x2, y2)
            if (dist < minDistance && dist <= touchThreshold) {
                minDistance = dist
                closestArrow = arrow
            }
        }
    }

    return closestArrow
}

private fun distanceToSegment(
    px: Float,
    py: Float,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float
): Float {
    val dx = x2 - x1
    val dy = y2 - y1
    val lenSq = dx * dx + dy * dy
    if (lenSq == 0f) return hypot(px - x1, py - y1)

    val t = ((px - x1) * dx + (py - y1) * dy) / lenSq
    val clampedT = t.coerceIn(0f, 1f)
    val projX = x1 + clampedT * dx
    val projY = y1 + clampedT * dy

    return hypot(px - projX, py - projY)
}
