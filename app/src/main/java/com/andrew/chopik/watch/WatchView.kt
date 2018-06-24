package com.andrew.chopik.watch

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Created by Andrew on 27.03.2018.
 */
class WatchView @JvmOverloads constructor(context: Context,
                                          attrs: AttributeSet? = null,
                                          defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    private val EXTRA_SECOND_COLOR = "second_color"
    private val EXTRA_SECOND_THICK = "second_thick"
    private val EXTRA_MINUTE_COLOR = "minute_color"
    private val EXTRA_MINUTE_THICK = "minute_thick"
    private val EXTRA_HOUR_COLOR = "hour_color"
    private val EXTRA_HOUR_THICK = "hour_thick"
    private val EXTRA_CLOCK_COLOR = "clock_color"
    private val EXTRA_CLOCK_THICK = "clock_thick"
    private val EXTRA_RADIUS = "radius"

    private val paintClockFace = Paint()
    private val paintMarks = Paint()
    private val paintSecondHand = Paint()
    private val paintMinuteHand = Paint()
    private val paintHourHand = Paint()

    private val rect = Rect()

    private val centerPoint = PointF()

    private val markCoords: FloatArray = FloatArray(240)

    var secondHandColor: Int = 0
        set(value) {
            paintSecondHand.color = value
            field = value
            invalidate()
        }
    var minuteHandColor: Int = 0
        set(value) {
            paintMinuteHand.color = value
            field = value
            invalidate()
        }
    var hourHandColor: Int = 0
        set(value) {
            paintHourHand.color = value
            field = value
            invalidate()
        }
    var clockFaceColor: Int = 0
        set(value) {
            paintClockFace.color = value
            field = value
            invalidate()
        }
    var secondHandThick: Float = 0f
        set(value) {
            paintSecondHand.strokeWidth = value
            field = value
            invalidate()
        }
    var minuteHandThick: Float = 0f
        set(value) {
            paintMinuteHand.strokeWidth = value
            field = value
            invalidate()
        }
    var hourHandThick: Float = 0f
        set(value) {
            paintHourHand.strokeWidth = value
            field = value
            invalidate()
        }
    var clockFaceThick: Float = 0f
        set(value) {
            paintClockFace.strokeWidth = value
            radius -= value / 2
            field = value
            invalidate()
        }

    var radius = 0f

    init {
        attrs?.let {
            val styledAttrs = context.theme.obtainStyledAttributes(it, R.styleable.WatchView, 0, 0)
            try {
                secondHandColor = styledAttrs.getColor(R.styleable.WatchView_secondHandColor, Color.RED)
                minuteHandColor = styledAttrs.getColor(R.styleable.WatchView_minuteHandColor, Color.BLACK)
                hourHandColor = styledAttrs.getColor(R.styleable.WatchView_hourHandColor, Color.BLACK)
                clockFaceColor = styledAttrs.getColor(R.styleable.WatchView_clockFaceColor, Color.BLACK)
                secondHandThick = styledAttrs.getDimension(R.styleable.WatchView_secondHandThick, 10f)
                minuteHandThick = styledAttrs.getDimension(R.styleable.WatchView_minuteHandThick, 15f)
                hourHandThick = styledAttrs.getDimension(R.styleable.WatchView_hourHandThick, 20f)
                clockFaceThick = styledAttrs.getDimension(R.styleable.WatchView_clockFaceThick, 0f)
            } finally {
                styledAttrs.recycle()
            }
        }
        paintSecondHand.apply {
            color = secondHandColor
            strokeWidth = secondHandThick
            isAntiAlias = true
        }
        paintMinuteHand.apply {
            color = minuteHandColor
            strokeWidth = minuteHandThick
            isAntiAlias = true
        }
        paintHourHand.apply {
            color = hourHandColor
            strokeWidth = hourHandThick
            isAntiAlias = true
        }
        paintMarks.apply {
            color = clockFaceColor
            isAntiAlias = true
            isSubpixelText = true
        }
        paintClockFace.apply {
            style = Paint.Style.STROKE
            color = clockFaceColor
            strokeWidth = clockFaceThick
            isAntiAlias = true
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var width: Int = 0
        var height: Int = 0
        val desiredSize = 3 * minOf(widthSize, heightSize) / 4

        when (widthMode) {
            MeasureSpec.EXACTLY -> width = widthSize
            MeasureSpec.AT_MOST -> width = minOf(desiredSize, widthSize)
            MeasureSpec.UNSPECIFIED -> width = desiredSize
        }
        when (heightMode) {
            MeasureSpec.EXACTLY -> height = heightSize
            MeasureSpec.AT_MOST -> height = minOf(desiredSize, widthSize)
            MeasureSpec.UNSPECIFIED -> height = desiredSize
        }

        width += paddingLeft + paddingRight
        height += paddingBottom + paddingTop

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        centerPoint.set((w + paddingLeft - paddingRight) / 2f, (h + paddingTop - paddingBottom) / 2f)
        radius = minOf(w - paddingRight - paddingLeft, h - paddingBottom - paddingTop) / 2f - clockFaceThick / 2

        paintMarks.textSize = radius / 6
        paintMarks.strokeWidth = radius / 100

        val markEndRadius = 95 * radius / 100
        for (minute in 1..60) {
            if (minute % 5 != 0) {
                val angle = minute * PI / 30 - PI / 2

                val startX = centerPoint.x + radius * cos(angle).toFloat()
                val startY = centerPoint.y + radius * sin(angle).toFloat()

                val minuteX = centerPoint.x + (markEndRadius) * cos(angle).toFloat()
                val minuteY = centerPoint.y + (markEndRadius) * sin(angle).toFloat()

                markCoords[minute * 4 - 4] = startX
                markCoords[minute * 4 - 3] = startY
                markCoords[minute * 4 - 2] = minuteX
                markCoords[minute * 4 - 1] = minuteY
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawClockFace(canvas)
        drawHands(canvas)
    }

    private fun drawClockFace(canvas: Canvas) {
        canvas.drawCircle(centerPoint.x, centerPoint.y, radius, paintClockFace)
        canvas.drawCircle(centerPoint.x, centerPoint.y, radius / 20, paintMarks)
        canvas.drawLines(markCoords, paintMarks)

        val textRadius = 90 * radius / 100
        for (hour in 1..12) {
            val angle = hour * PI / 6 - PI / 2

            paintMarks.getTextBounds(hour.toString(), 0, hour.toString().length, rect)
            val textX = centerPoint.x + (textRadius) * cos(angle).toFloat() - rect.width() / 2
            val textY = centerPoint.y + (textRadius) * sin(angle).toFloat() + rect.height() / 2
            canvas.drawText(hour.toString(), textX, textY, paintMarks)
        }
    }

    private fun drawHands(canvas: Canvas) {
        val calendar = Calendar.getInstance()
        val sec = calendar.get(Calendar.SECOND)
        val min = calendar.get(Calendar.MINUTE)
        val hour = calendar.get(Calendar.HOUR)

        val secAngle = ((PI * 2) * sec / 60 - PI/2).toFloat()
        val secTailAngle = (secAngle - PI).toFloat()
        val minAngle = ((PI * 2) * min / 60 + (secAngle + PI/2) / 60 - PI/2).toFloat()
        val minTailAngle = (minAngle - PI).toFloat()
        val hourAngle = ((PI * 2) * hour / 12 + (minAngle + PI/2) / 12 - PI/2).toFloat()
        val hourTailAngle = (hourAngle - PI).toFloat()

        val secRadius = 85 * radius / 100
        val secTailRadius = 15 * radius / 100
        val hourRadius = 60 * radius / 100
        val hourTailRadius = 10 * radius / 100

        val secStartX = centerPoint.x + secRadius * cos(secAngle)
        val secStartY = centerPoint.y + secRadius * sin(secAngle)
        val secEndX = centerPoint.x + secTailRadius * cos(secTailAngle)
        val secEndY = centerPoint.y + secTailRadius * sin(secTailAngle)
        val minStartX = centerPoint.x + secRadius * cos(minAngle)
        val minStartY = centerPoint.y + secRadius * sin(minAngle)
        val minEndX = centerPoint.x + secTailRadius * cos(minTailAngle)
        val minEndY = centerPoint.y + secTailRadius * sin(minTailAngle)
        val hourStartX = centerPoint.x + (hourRadius) * cos(hourAngle)
        val hourStartY = centerPoint.y + (hourRadius) * sin(hourAngle)
        val hourEndX = centerPoint.x + (hourTailRadius) * cos(hourTailAngle)
        val hourEndY = centerPoint.y + (hourTailRadius) * sin(hourTailAngle)

        canvas.drawLine(hourStartX, hourStartY, hourEndX, hourEndY, paintHourHand)
        canvas.drawLine(minStartX, minStartY, minEndX, minEndY, paintMinuteHand)
        canvas.drawLine(secStartX, secStartY, secEndX, secEndY, paintSecondHand)

        canvas.drawPoint(centerPoint.x, centerPoint.y, paintMarks)

        postInvalidateDelayed(500)
        invalidate()
    }

    public override fun onSaveInstanceState(): Parcelable? {
        super.onSaveInstanceState()

        return Bundle().apply {
            putParcelable("superState", super.onSaveInstanceState())
            putInt(EXTRA_SECOND_COLOR, secondHandColor)
            putInt(EXTRA_MINUTE_COLOR, minuteHandColor)
            putInt(EXTRA_HOUR_COLOR, hourHandColor)
            putInt(EXTRA_CLOCK_COLOR, clockFaceColor)
            putFloat(EXTRA_SECOND_THICK, secondHandThick)
            putFloat(EXTRA_MINUTE_THICK, minuteHandThick)
            putFloat(EXTRA_HOUR_THICK, hourHandThick)
            putFloat(EXTRA_CLOCK_THICK, clockFaceThick)
            putFloat(EXTRA_RADIUS, radius)
        }
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        var restoredState = state
        if (state is Bundle) {
            secondHandColor = state.getInt(EXTRA_SECOND_COLOR)
            minuteHandColor = state.getInt(EXTRA_MINUTE_COLOR)
            hourHandColor = state.getInt(EXTRA_HOUR_COLOR)
            clockFaceColor  = state.getInt(EXTRA_CLOCK_COLOR)
            secondHandThick = state.getFloat(EXTRA_SECOND_THICK)
            minuteHandThick = state.getFloat(EXTRA_MINUTE_THICK)
            hourHandThick = state.getFloat(EXTRA_HOUR_THICK)
            clockFaceThick = state.getFloat(EXTRA_CLOCK_THICK)
            radius = state.getFloat(EXTRA_RADIUS)
            restoredState = state.getParcelable("superState")
        }
        super.onRestoreInstanceState(restoredState)
    }
}