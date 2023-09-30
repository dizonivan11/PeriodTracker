package com.streamside.periodtracker.views

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.streamside.periodtracker.R
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt


class CircleFillView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val center = PointF()
    private val circleRect = RectF()
    private val segment = Path()
    private val strokePaint = Paint()
    private val fillPaint = Paint()
    private var radius = 0
    private var fillColor = 0
    private var strokeColor = 0
    private var strokeWidth = 0f
    private var circleFillValue = 0

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CircleFillView,
            0, 0
        )
        try {
            fillColor = a.getColor(R.styleable.CircleFillView_CircleFillColor, Color.WHITE)
            strokeColor = a.getColor(R.styleable.CircleFillView_CircleFillStrokeColor, Color.BLACK)
            strokeWidth = a.getFloat(R.styleable.CircleFillView_CircleFillStrokeWidth, 1f)
            setValue(a.getInteger(R.styleable.CircleFillView_CircleFillValue, 0))
        } finally {
            a.recycle()
        }
        fillPaint.color = fillColor
        strokePaint.color = strokeColor
        strokePaint.strokeWidth = strokeWidth
        strokePaint.style = Paint.Style.STROKE
    }

    fun setFillColor(fillColor: Int) {
        this.fillColor = fillColor
        fillPaint.color = fillColor
        invalidate()
    }

    fun getFillColor(): Int {
        return fillColor
    }

    fun setStrokeColor(strokeColor: Int) {
        this.strokeColor = strokeColor
        strokePaint.color = strokeColor
        invalidate()
    }

    fun getStrokeColor(): Int {
        return strokeColor
    }

    fun setStrokeWidth(strokeWidth: Float) {
        this.strokeWidth = strokeWidth
        strokePaint.strokeWidth = strokeWidth
        invalidate()
    }

    fun getStrokeWidth(): Float {
        return strokeWidth
    }

    fun setValue(value: Int) {
        ObjectAnimator.ofInt(this, "CircleFillValue", value).apply {
            duration = 1000
            start()
        }
        setCircleFillValue(value)
    }

    private fun setCircleFillValue(value: Int) {
        this.circleFillValue = MAX_VALUE.coerceAtMost(MIN_VALUE.coerceAtLeast(value))
        setPaths()
        invalidate()
    }

    fun getValue(): Int {
        return circleFillValue
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center.x = (width / 2).toFloat()
        center.y = (height / 2).toFloat()
        radius = width.coerceAtMost(height) / 2 - (strokeWidth.toInt() * 2)
        circleRect[
            center.x - radius + strokeWidth.toInt(),
            center.y - radius + strokeWidth.toInt(),
            center.x + radius - strokeWidth.toInt()] = center.y + radius - strokeWidth.toInt()
        setPaths()
    }

    private fun setPaths() {
        val y = center.y + radius - (2 * radius * circleFillValue / 100 - 1)
        val x = center.x - sqrt(
            radius.toDouble().pow(2.0) - (y - center.y).toDouble().pow(2.0)
        ).toFloat()
        val angle =
            Math.toDegrees(atan(((center.y - y) / (x - center.x)).toDouble())).toFloat()
        val startAngle = 180 - angle
        val sweepAngle = 2 * angle - 180
        segment.rewind()
        segment.addArc(circleRect, startAngle, sweepAngle)
        segment.close()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(segment, fillPaint)
        canvas.drawCircle(center.x, center.y, radius.toFloat(), strokePaint)
    }

    companion object {
        const val MIN_VALUE = 0
        const val MAX_VALUE = 100
    }
}