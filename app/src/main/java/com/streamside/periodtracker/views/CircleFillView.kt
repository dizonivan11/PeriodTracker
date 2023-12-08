package com.streamside.periodtracker.views

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.streamside.periodtracker.OVULATION
import com.streamside.periodtracker.R
import com.streamside.periodtracker.SAFE_MAX
import com.streamside.periodtracker.SAFE_MIN
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt

class CircleFillView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val center = PointF()
    private val circleRect = RectF()
    private val segment = Path()
    private val strokePaint = Paint()
    private val fillPaint = Paint()
    private val safePaint = Paint()
    private val foreSafePaint = Paint()
    private var radius = 0
    private var fillColor = 0
    private var strokeColor = 0
    private var safeColor = 0
    private var strokeWidth = 0f
    private var circleFillValue = 0
    private var ovulationY = 0.0
    private var regularY = 0.0
    private var safePeriodY = 0.0
    private var periodMode = false

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.CircleFillView, 0, 0)
        try {
            fillColor = a.getColor(R.styleable.CircleFillView_CircleFillColor, Color.WHITE)
            strokeColor = a.getColor(R.styleable.CircleFillView_CircleFillStrokeColor, Color.BLACK)
            safeColor = a.getColor(R.styleable.CircleFillView_CircleFillSafeColor, Color.BLACK)
            strokeWidth = a.getFloat(R.styleable.CircleFillView_CircleFillStrokeWidth, 1f)
            setCircleFillValue(a.getInteger(R.styleable.CircleFillView_CircleFillValue, 0), 1000)
            periodMode = a.getBoolean(R.styleable.CircleFillView_CircleFillPeriodMode, false)
        } finally {
            a.recycle()
        }
        val tf = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        fillPaint.color = fillColor
        strokePaint.color = strokeColor
        strokePaint.strokeWidth = strokeWidth
        strokePaint.style = Paint.Style.STROKE
        safePaint.color = safeColor
        safePaint.strokeWidth = strokeWidth / 6
        safePaint.style = Paint.Style.FILL
        safePaint.typeface = tf
        safePaint.textAlign = Paint.Align.CENTER
        safePaint.textSize = 32f
        foreSafePaint.color = 0x40000000
        foreSafePaint.strokeWidth = safePaint.strokeWidth
        foreSafePaint.style = safePaint.style
        foreSafePaint.typeface = safePaint.typeface
        foreSafePaint.textAlign = safePaint.textAlign
        foreSafePaint.textSize = safePaint.textSize
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

    fun getPeriodMode(): Boolean {
        return periodMode
    }

    fun setPeriodMode(periodMode: Boolean) {
        this.periodMode = periodMode
        safePaint.textSize = 50f
        foreSafePaint.textSize = safePaint.textSize
        val h2 = height - (strokeWidth.toInt() * 2)
        safePeriodY = (0.45 * h2) + strokeWidth
        invalidate()
    }

    fun setCircleFillValue(value: Int, duration: Long) {
        ObjectAnimator.ofInt(this, "CircleFillValue", value).apply {
            this.duration = duration
            start()
        }
        setCircleFillValue(value)
    }

    fun setCircleFillValue(value: Int) {
        this.circleFillValue = MAX_VALUE.coerceAtMost(MIN_VALUE.coerceAtLeast(value))
        setPaths()
        invalidate()
    }

    fun getCircleFillValue(): Int {
        return this.circleFillValue
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center.x = (width / 2).toFloat()
        center.y = (height / 2).toFloat()
        radius = width.coerceAtMost(height) / 2 - strokeWidth.toInt()
        val h2 = height - (strokeWidth.toInt() * 2)
        if (!periodMode) {
            ovulationY = (((100.0 - ((OVULATION.toDouble() / SAFE_MAX.toDouble()) * 100.0)) / 100.0) * h2) + strokeWidth
            regularY = (((100.0 - ((SAFE_MIN.toDouble() / SAFE_MAX.toDouble()) * 100.0)) / 100.0) * h2) + strokeWidth
        } else {
            safePeriodY = (0.45 * h2) + strokeWidth
        }
        circleRect[
            center.x - radius + strokeWidth.toInt(),
            center.y - radius + strokeWidth.toInt(),
            center.x + radius - strokeWidth.toInt()] = center.y + radius - strokeWidth.toInt()
        setPaths()
    }

    private fun setPaths() {
        val y = center.y + radius - (2 * radius * circleFillValue / 100 - 1)
        val x = center.x - sqrt(radius.toDouble().pow(2.0) - (y - center.y).toDouble().pow(2.0)).toFloat()
        val angle = Math.toDegrees(atan(((center.y - y) / (x - center.x)).toDouble())).toFloat()
        val startAngle = 180 - angle
        val sweepAngle = 2 * angle - 180
        segment.rewind()
        segment.addArc(circleRect, startAngle, sweepAngle)
        segment.close()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!periodMode) {
            drawSection(canvas, "O V U L A T I O N", ovulationY, safePaint)
            drawSection(canvas, "P E R I O D", regularY, safePaint)
        } else {
            drawSection(canvas, "P E R I O D   P H A S E", safePeriodY, safePaint, false)
        }
        canvas.drawPath(segment, fillPaint)
        if (!periodMode) {
            drawSection(canvas, "O V U L A T I O N", ovulationY, foreSafePaint)
            drawSection(canvas, "P E R I O D", regularY, foreSafePaint)
        } else {
            drawSection(canvas, "P E R I O D   P H A S E", safePeriodY, foreSafePaint, false)
        }
        canvas.drawCircle(center.x, center.y, radius.toFloat(), strokePaint)
    }

    private fun getChordLength(radius: Int, y: Double): Double {
        val distanceToCenter = sqrt((y - center.y.toDouble()).pow(2.0))
        return 2.0 * sqrt(radius.toDouble().pow(2.0) - distanceToCenter.pow(2.0))
    }

    private fun drawSection(canvas: Canvas, text: String, y: Double, paint: Paint, displayLine: Boolean = true) {
        val length = getChordLength(radius, y)
        val startX = center.x - (length / 2.0)
        if (displayLine) canvas.drawLine(startX.toFloat(), y.toFloat(), startX.toFloat() + length.toFloat(), y.toFloat(), paint)
        canvas.drawText(text, center.x, y.toFloat() - 20f, paint)
    }

    companion object {
        const val MIN_VALUE = 0
        const val MAX_VALUE = 100
    }
}