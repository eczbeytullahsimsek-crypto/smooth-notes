package com.smoothnotes.app

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 5f
    }

    private val path = Path()
    private var lastX = 0f
    private var lastY = 0f
    private var active = false

    private var smoothX = 0f
    private var smoothY = 0f

    private val smoothing = 0.72f

    init {
        setBackgroundColor(Color.WHITE)
        isFocusable = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)

                lastX = event.x
                lastY = event.y

                smoothX = event.x
                smoothY = event.y

                path.reset()
                path.moveTo(smoothX, smoothY)

                active = true
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (!active) return true

                val x = event.x
                val y = event.y

                smoothX += (x - smoothX) * smoothing
                smoothY += (y - smoothY) * smoothing

                val distance = hypot(
                    smoothX - lastX,
                    smoothY - lastY
                )

                if (distance > 0.5f) {
                    path.quadTo(
                        lastX,
                        lastY,
                        (lastX + smoothX) / 2f,
                        (lastY + smoothY) / 2f
                    )

                    lastX = smoothX
                    lastY = smoothY
                }

                invalidate()
                return true
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (active) {
                    path.lineTo(smoothX, smoothY)
                }

                active = false
                invalidate()
                return true
            }
        }

        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawPath(path, paint)
    }
}
