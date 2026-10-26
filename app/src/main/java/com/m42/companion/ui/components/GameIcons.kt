package com.m42.companion.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Icons with no Material equivalent, drawn on the same 24×24 grid as Material icons. */
object GameIcons {
    /**
     * A flexed arm, for Strength, shaped like the 💪 emoji: the forearm rises on the left to a fist, the biceps
     * bulges on the right, with the crease and fingers cut out. Drawn on a 72-unit grid.
     */
    val FlexedArm: ImageVector by lazy {
        ImageVector.Builder("FlexedArm", 24.dp, 24.dp, 72f, 72f).apply {
            addPath(
                pathData = addPathNodes(
                    "M11.9 62.1 C11.2 46 12.8 35 15.7 26.5 L15.7 16.5 C15.7 15.5 16 15 16.6 14.4 " +
                        "C21 10.3 24.9 8.7 24.9 8.7 L33.3 7.2 C35.2 8.6 37.8 12.2 36.9 18.7 " +
                        "C38.8 18.3 41.6 18.8 41.2 20.9 C41 21.8 40.4 22.2 39.6 22.4 L33.3 23.8 " +
                        "C30 24.6 27.2 25.4 25.5 26.8 C26.3 28 26.9 35 27.5 40.6 L27.8 48.5 " +
                        "C30.5 40.5 36.5 35.3 44.1 34.2 C57.6 32.3 64.3 42.7 64.3 42.7 " +
                        "C66.4 46.3 65.5 50.8 62.5 53.5 C52 63.5 36.6 65.8 16.5 65.7 C16.5 65.7 12.8 66.8 11.9 62.1 Z " +
                        // Biceps crease
                        "M33.6 52.2 C40.5 54.6 48 53 53.6 47.6 C54.6 46.8 55.6 48 54.8 49 " +
                        "C48.6 55.6 40 57 33.2 55 C31.4 54.4 31.8 51.6 33.6 52.2 Z " +
                        // Curled fingers
                        "M21.6 14.2 L23.2 13.6 L24.4 19.2 L22.8 19.8 Z M26.6 12.4 L28.2 11.9 L29.2 18.6 L27.6 19 Z " +
                        "M31.4 11.2 L33 10.9 L33.6 18.2 L32 18.4 Z",
                ),
                pathFillType = PathFillType.EvenOdd,
                fill = SolidColor(Color.Black),
            )
        }.build()
    }

    /** A bank note: a frame with a medallion in the middle, for money. */
    val BankNote: ImageVector by lazy {
        icon("BankNote") {
            path(fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd) {
                moveTo(3.5f, 6f); lineTo(20.5f, 6f)
                quadTo(22f, 6f, 22f, 7.5f); lineTo(22f, 16.5f)
                quadTo(22f, 18f, 20.5f, 18f); lineTo(3.5f, 18f)
                quadTo(2f, 18f, 2f, 16.5f); lineTo(2f, 7.5f)
                quadTo(2f, 6f, 3.5f, 6f); close()
                // Hollow inner border
                moveTo(4.6f, 7.6f); lineTo(19.4f, 7.6f); lineTo(20.4f, 8.6f); lineTo(20.4f, 15.4f)
                lineTo(19.4f, 16.4f); lineTo(4.6f, 16.4f); lineTo(3.6f, 15.4f); lineTo(3.6f, 8.6f); close()
            }
            path(fill = SolidColor(Color.Black)) {
                // Medallion and corner dots
                circle(12f, 12f, 2.7f)
                circle(6.6f, 12f, 1.1f)
                circle(17.4f, 12f, 1.1f)
            }
        }
    }

    /** A pentagram drawn as one continuous five-pointed star, for Eldritch tokens. */
    val Pentagram: ImageVector by lazy {
        icon("Pentagram") {
            val r = 9.2f
            val points = (0 until 5).map { i ->
                val angle = -PI / 2 + i * 2 * PI / 5
                (12f + r * cos(angle).toFloat()) to (12.6f + r * sin(angle).toFloat())
            }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineJoin = StrokeJoin.Round) {
                val order = listOf(0, 2, 4, 1, 3)
                moveTo(points[order[0]].first, points[order[0]].second)
                order.drop(1).forEach { lineTo(points[it].first, points[it].second) }
                close()
            }
        }
    }

    private fun icon(name: String, block: ImageVector.Builder.() -> Unit) =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply(block).build()

    private fun androidx.compose.ui.graphics.vector.PathBuilder.circle(cx: Float, cy: Float, r: Float) {
        moveTo(cx - r, cy)
        arcTo(r, r, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = cx + r, y1 = cy)
        arcTo(r, r, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = cx - r, y1 = cy)
        close()
    }
}
