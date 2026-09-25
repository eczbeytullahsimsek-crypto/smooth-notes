package com.smoothnotes.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {

    private final Paint paint;
    private final Path path;

    private float lastX;
    private float lastY;

    private float p0x;
    private float p0y;

    private float p1x;
    private float p1y;

    private float smoothX;
    private float smoothY;

    private float lastPressure = 0.5f;

    private static final float SMOOTHING = 0.72f;
    private static final float MIN_WIDTH = 1.5f;
    private static final float MAX_WIDTH = 8.0f;

    public DrawingView(Context context) {
        super(context);

        paint = new Paint(
                Paint.ANTI_ALIAS_FLAG |
                Paint.DITHER_FLAG
        );

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(4f);

        path = new Path();

        setBackgroundColor(Color.WHITE);

        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.WHITE);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:

                lastX = event.getX();
                lastY = event.getY();

                smoothX = lastX;
                smoothY = lastY;

                p0x = lastX;
                p0y = lastY;

                p1x = lastX;
                p1y = lastY;

                lastPressure = getPressure(event);

                path.moveTo(lastX, lastY);

                invalidate();

                return true;

            case MotionEvent.ACTION_MOVE:

                processMove(event);

                invalidate();

                return true;

            case MotionEvent.ACTION_UP:

                smoothX = event.getX();
                smoothY = event.getY();

                path.lineTo(smoothX, smoothY);

                invalidate();

                return true;

            default:

                return true;
        }
    }

    private void processMove(MotionEvent event) {

        int historySize = event.getHistorySize();

        for (int i = 0; i < historySize; i++) {

            float x = event.getHistoricalX(i);
            float y = event.getHistoricalY(i);
            float pressure = event.getHistoricalPressure(i);

            addPoint(x, y, pressure);
        }

        addPoint(
                event.getX(),
                event.getY(),
                getPressure(event)
        );
    }

    private void addPoint(
            float x,
            float y,
            float pressure
    ) {

        smoothX +=
                (x - smoothX) *
                SMOOTHING;

        smoothY +=
                (y - smoothY) *
                SMOOTHING;

        float velocityX =
                smoothX - lastX;

        float velocityY =
                smoothY - lastY;

        float velocity =
                (float) Math.sqrt(
                        velocityX * velocityX +
                        velocityY * velocityY
                );

        float velocityFactor =
                Math.max(
                        0.55f,
                        Math.min(
                                1.0f,
                                1.0f - velocity * 0.015f
                        )
                );

        float filteredPressure =
                lastPressure * 0.35f +
                pressure * 0.65f;

        lastPressure =
                filteredPressure;

        float width =
                MIN_WIDTH +
                (MAX_WIDTH - MIN_WIDTH)
                        * filteredPressure
                        * velocityFactor;

        paint.setStrokeWidth(width);

        float midX =
                (lastX + smoothX) / 2f;

        float midY =
                (lastY + smoothY) / 2f;

        path.quadTo(
                lastX,
                lastY,
                midX,
                midY
        );

        lastX = smoothX;
        lastY = smoothY;
    }

    private float getPressure(
            MotionEvent event
    ) {

        float pressure =
                event.getPressure();

        if (pressure <= 0f) {
            pressure = 0.5f;
        }

        return Math.max(
                0f,
                Math.min(
                        1f,
                        pressure
                )
        );
    }
}
