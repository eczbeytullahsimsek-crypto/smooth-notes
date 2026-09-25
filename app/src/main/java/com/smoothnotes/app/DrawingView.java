package com.smoothnotes.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class DrawingView extends View {

    private final Paint paint;

    private final ArrayList<StrokePoint> points =
            new ArrayList<>();

    private float smoothX;
    private float smoothY;

    private float lastX;
    private float lastY;

    private float lastPressure = 0.5f;

    private static final float SMOOTHING = 0.70f;

    private static final float MIN_WIDTH = 1.8f;

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

        setBackgroundColor(Color.WHITE);

        setLayerType(
                View.LAYER_TYPE_HARDWARE,
                null
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        canvas.drawColor(Color.WHITE);

        if (points.size() < 2) {
            return;
        }

        for (int i = 1; i < points.size(); i++) {

            StrokePoint a = points.get(i - 1);

            StrokePoint b = points.get(i);

            float width =
                    (a.width + b.width) / 2f;

            paint.setStrokeWidth(width);

            canvas.drawLine(
                    a.x,
                    a.y,
                    b.x,
                    b.y,
                    paint
            );
        }
    }

    @Override
    public boolean onTouchEvent(
            MotionEvent event
    ) {

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:

                smoothX = event.getX();

                smoothY = event.getY();

                lastX = smoothX;

                lastY = smoothY;

                lastPressure =
                        getPressure(event);

                points.add(
                        new StrokePoint(
                                smoothX,
                                smoothY,
                                calculateWidth(
                                        lastPressure
                                )
                        )
                );

                invalidate();

                return true;

            case MotionEvent.ACTION_MOVE:

                processMotion(event);

                invalidate();

                return true;

            case MotionEvent.ACTION_UP:

                processMotion(event);

                invalidate();

                return true;

            default:

                return true;
        }
    }

    private void processMotion(
            MotionEvent event
    ) {

        int history =
                event.getHistorySize();

        for (int i = 0; i < history; i++) {

            addPoint(
                    event.getHistoricalX(i),
                    event.getHistoricalY(i),
                    event.getHistoricalPressure(i)
            );
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

        float filteredPressure =
                lastPressure * 0.35f +
                pressure * 0.65f;

        lastPressure =
                filteredPressure;

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
                        0.65f,
                        Math.min(
                                1.0f,
                                1.0f -
                                velocity * 0.01f
                        )
                );

        float width =
                MIN_WIDTH +
                (MAX_WIDTH - MIN_WIDTH)
                        * filteredPressure
                        * velocityFactor;

        points.add(
                new StrokePoint(
                        smoothX,
                        smoothY,
                        width
                )
        );

        lastX = smoothX;

        lastY = smoothY;
    }

    private float calculateWidth(
            float pressure
    ) {

        return MIN_WIDTH +
                (MAX_WIDTH - MIN_WIDTH)
                        * pressure;
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

    private static class StrokePoint {

        final float x;

        final float y;

        final float width;

        StrokePoint(
                float x,
                float y,
                float width
        ) {

            this.x = x;

            this.y = y;

            this.width = width;
        }
    }
}
