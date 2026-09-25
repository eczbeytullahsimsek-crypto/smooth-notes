package com.smoothnotes.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class DrawingView extends View {

    private final Paint paint;
    private final ArrayList<Stroke> strokes = new ArrayList<>();

    private Stroke currentStroke;

    private float smoothX;
    private float smoothY;

    private float lastPressure = 0.5f;

    private static final float SMOOTHING = 0.78f;
    private static final float MIN_WIDTH = 1.6f;
    private static final float MAX_WIDTH = 7.5f;

    public DrawingView(Context context) {

        super(context);

        paint = new Paint(
                Paint.ANTI_ALIAS_FLAG |
                Paint.DITHER_FLAG |
                Paint.SUBPIXEL_TEXT_FLAG
        );

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setDither(true);

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

        for (Stroke stroke : strokes) {
            drawStroke(canvas, stroke);
        }

        if (currentStroke != null) {
            drawStroke(canvas, currentStroke);
        }
    }

    private void drawStroke(
            Canvas canvas,
            Stroke stroke
    ) {

        int count = stroke.points.size();

        if (count == 0) {
            return;
        }

        if (count == 1) {

            StrokePoint p =
                    stroke.points.get(0);

            paint.setColor(Color.BLACK);

            canvas.drawCircle(
                    p.x,
                    p.y,
                    p.width / 2f,
                    paint
            );

            return;
        }

        for (int i = 0; i < count - 1; i++) {

            StrokePoint p0 =
                    stroke.points.get(
                            Math.max(0, i - 1)
                    );

            StrokePoint p1 =
                    stroke.points.get(i);

            StrokePoint p2 =
                    stroke.points.get(i + 1);

            StrokePoint p3 =
                    stroke.points.get(
                            Math.min(
                                    count - 1,
                                    i + 2
                            )
                    );

            int steps = 5;

            for (int j = 0; j < steps; j++) {

                float t =
                        j / (float) steps;

                float x =
                        catmull(
                                p0.x,
                                p1.x,
                                p2.x,
                                p3.x,
                                t
                        );

                float y =
                        catmull(
                                p0.y,
                                p1.y,
                                p2.y,
                                p3.y,
                                t
                        );

                float width =
                        p1.width +
                        (p2.width - p1.width)
                                * t;

                paint.setColor(Color.BLACK);

                canvas.drawCircle(
                        x,
                        y,
                        width / 2f,
                        paint
                );
            }
        }

        StrokePoint last =
                stroke.points.get(count - 1);

        paint.setColor(Color.BLACK);

        canvas.drawCircle(
                last.x,
                last.y,
                last.width / 2f,
                paint
        );
    }

    private float catmull(
            float p0,
            float p1,
            float p2,
            float p3,
            float t
    ) {

        float t2 = t * t;
        float t3 = t2 * t;

        return 0.5f * (
                (2f * p1) +
                (-p0 + p2) * t +
                (2f * p0 -
                        5f * p1 +
                        4f * p2 -
                        p3) * t2 +
                (-p0 +
                        3f * p1 -
                        3f * p2 +
                        p3) * t3
        );
    }

    @Override
    public boolean onTouchEvent(
            MotionEvent event
    ) {

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:

                currentStroke =
                        new Stroke();

                strokes.add(
                        currentStroke
                );

                smoothX =
                        event.getX();

                smoothY =
                        event.getY();

                lastPressure =
                        getPressure(event);

                addPoint(
                        smoothX,
                        smoothY,
                        lastPressure
                );

                invalidate();

                return true;

            case MotionEvent.ACTION_MOVE:

                processMotion(event);

                invalidate();

                return true;

            case MotionEvent.ACTION_UP:

                processMotion(event);

                currentStroke = null;

                invalidate();

                return true;

            case MotionEvent.ACTION_CANCEL:

                currentStroke = null;

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

        if (currentStroke == null) {
            return;
        }

        smoothX +=
                (x - smoothX) *
                SMOOTHING;

        smoothY +=
                (y - smoothY) *
                SMOOTHING;

        float filteredPressure =
                lastPressure * 0.25f +
                pressure * 0.75f;

        lastPressure =
                filteredPressure;

        float width =
                MIN_WIDTH +
                (MAX_WIDTH - MIN_WIDTH)
                        * filteredPressure;

        currentStroke.points.add(
                new StrokePoint(
                        smoothX,
                        smoothY,
                        width
                )
        );
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

    private static class Stroke {

        final ArrayList<StrokePoint> points =
                new ArrayList<>();
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
