package com.example.ui.components

import com.example.util.Dates
import com.example.util.ScoreJson
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyEvaluation
import com.example.ui.theme.CourtGreenPrimary
import com.example.ui.theme.ScoreGreen
import com.example.ui.theme.ScoreOrange
import com.example.ui.theme.ScoreRed

@Composable
fun WeeklyPerformanceTrendChart(
    evaluations: List<DailyEvaluation>,
    modifier: Modifier = Modifier
) {
    // Process last 8 weeks average score
    val weekScores = mutableListOf<Double>()
    val weekLabels = mutableListOf<String>()

    val today = Dates.today()
    val weekStart = Dates.minusDays(today, today.dayOfWeek.ordinal) // Monday of this week

    for (w in 7 downTo 0) {
        val start = Dates.minusDays(weekStart, w * 7)
        val startDateStr = start.toString()
        val endDateStr = Dates.minusDays(start, -6).toString()

        val weekEvals = evaluations.filter { it.date >= startDateStr && it.date <= endDateStr }

        var total = 0.0
        var count = 0
        for (eval in weekEvals) {
            for (v in ScoreJson.decode(eval.skillScoresJson).values) {
                total += v
                count++
            }
        }

        val avg = if (count > 0) total / count else 0.0
        weekScores.add(avg)
        weekLabels.add("W${8 - w}")
    }

    val primaryColor = CourtGreenPrimary
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val textStyle = MaterialTheme.typography.labelSmall

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "8-WEEK PERFORMANCE TREND (1-10 SCORE)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height
                val paddingLeft = 40f
                val paddingBottom = 40f
                val chartWidth = width - paddingLeft - 20f
                val chartHeight = height - paddingBottom - 10f

                // Draw Y Axis Gridlines (0, 2.5, 5, 7.5, 10)
                val ySteps = 4
                for (i in 0..ySteps) {
                    val yVal = i * 2.5
                    val yPos = chartHeight - (i * (chartHeight / ySteps)) + 10f

                    drawLine(
                        color = gridColor,
                        start = Offset(paddingLeft, yPos),
                        end = Offset(width - 10f, yPos),
                        strokeWidth = 1f
                    )
                }

                // Draw Score Points & Path
                val points = mutableListOf<Offset>()
                val stepX = chartWidth / (weekScores.size - 1).coerceAtLeast(1)

                for (i in weekScores.indices) {
                    val score = weekScores[i].coerceIn(0.0, 10.0)
                    val x = paddingLeft + (i * stepX)
                    val y = chartHeight - ((score / 10.0) * chartHeight) + 10f
                    points.add(Offset(x, y.toFloat()))
                }

                // Connect points with smooth stroke line
                if (points.isNotEmpty()) {
                    val path = Path()
                    path.moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        path.lineTo(points[i].x, points[i].y)
                    }

                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )

                    // Draw circles at data points
                    for (i in points.indices) {
                        val pt = points[i]
                        val scoreVal = weekScores[i]
                        val ptColor = when {
                            scoreVal >= 7.5 -> ScoreGreen
                            scoreVal >= 5.0 -> ScoreOrange
                            scoreVal > 0 -> ScoreRed
                            else -> Color.Gray
                        }

                        drawCircle(
                            color = ptColor,
                            radius = 7f,
                            center = pt
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3f,
                            center = pt
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Week labels row
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (lbl in weekLabels) {
                Text(
                    text = lbl,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
