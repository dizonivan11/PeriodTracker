package com.streamside.periodtracker.views

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
    private var value = 0

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
            value = a.getInteger(R.styleable.CircleFillView_CircleFillValue, 0)
            adjustValue(value)
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
        adjustValue(value)
        setPaths()
        invalidate()
    }

    fun getValue(): Int {
        return value
    }

    private fun adjustValue(value: Int) {
        this.value = Math.min(MAX_VALUE, Math.max(MIN_VALUE, value))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center.x = (width / 2).toFloat()
        center.y = (height / 2).toFloat()
        radius = Math.min(width, height) / 2 - strokeWidth.toInt()
        circleRect[center.x - radius, center.y - radius, center.x + radius] = center.y + radius
        setPaths()
    }

    private fun setPaths() {
        var y = center.y + radius - (2 * radius * value / 100 - 1)
        var x = center.x - Math.sqrt(
            Math.pow(
                radius.toDouble(),
                2.0
            ) - Math.pow((y - center.y).toDouble(), 2.0)
        ).toFloat()
        val angle =
            Math.toDegrees(Math.atan(((center.y - y) / (x - center.x)).toDouble())).toFloat()
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