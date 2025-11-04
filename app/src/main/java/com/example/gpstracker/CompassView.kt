package com.example.gpstracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CompassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density * 3
        color = ContextCompat.getColor(context, R.color.compassStroke)
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = resources.displayMetrics.density * 2
        color = ContextCompat.getColor(context, R.color.compassTick)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.compassText)
        textSize = resources.displayMetrics.scaledDensity * 16
        textAlign = Paint.Align.CENTER
    }

    private val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.compassHeading)
        strokeWidth = resources.displayMetrics.density * 4
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val headingTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.compassHeading)
        textSize = resources.displayMetrics.scaledDensity * 32
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
    }

    private var heading: Float = 0f

    fun setHeading(value: Float) {
        heading = (value % 360 + 360) % 360
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width - paddingLeft - paddingRight
        val height = height - paddingTop - paddingBottom
        val radius = min(width, height) / 2f
        val centerX = paddingLeft + width / 2f
        val centerY = paddingTop + height / 2f

        canvas.drawCircle(centerX, centerY, radius, circlePaint)

        drawTicks(canvas, centerX, centerY, radius)
        drawPointer(canvas, centerX, centerY, radius)
        drawHeadingText(canvas, centerX, centerY)
    }

    private fun drawTicks(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val longTickLength = radius * 0.12f
        val shortTickLength = radius * 0.07f
        for (degree in 0 until 360 step 10) {
            val angle = Math.toRadians(degree.toDouble() - 90)
            val cosValue = cos(angle)
            val sinValue = sin(angle)
            val startRadius = radius - if (degree % 30 == 0) longTickLength else shortTickLength
            val startX = cx + startRadius * cosValue.toFloat()
            val startY = cy + startRadius * sinValue.toFloat()
            val endX = cx + radius * cosValue.toFloat()
            val endY = cy + radius * sinValue.toFloat()
            canvas.drawLine(startX, startY, endX, endY, tickPaint)

            if (degree % 90 == 0) {
                val labelRadius = radius - longTickLength - textPaint.textSize
                val textX = cx + labelRadius * cosValue.toFloat()
                val textY = cy + labelRadius * sinValue.toFloat() - (textPaint.descent() + textPaint.ascent()) / 2
                canvas.drawText(degree.toString(), textX, textY, textPaint)
            }
        }
    }

    private fun drawPointer(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        canvas.save()
        canvas.translate(cx, cy)
        canvas.rotate(heading)
        canvas.drawLine(0f, 0f, 0f, -radius + radius * 0.15f, headingPaint)
        canvas.drawCircle(0f, 0f, radius * 0.06f, headingPaint)
        canvas.restore()
    }

    private fun drawHeadingText(canvas: Canvas, cx: Float, cy: Float) {
        val text = String.format("%d°", heading.toInt())
        val y = cy - (headingTextPaint.descent() + headingTextPaint.ascent()) / 2
        canvas.drawText(text, cx, y, headingTextPaint)
    }
}
