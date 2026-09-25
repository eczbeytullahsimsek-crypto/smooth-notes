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

    private float lastX;
    private float lastY;
    private float lastPressure = 0.5f;

    private static final float MIN_WIDTH = 1.8f;
    private static final float MAX_WIDTH = 6.5f;

    public DrawingView(Context context) {
        super(context);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setAntiAlias(true);
        paint.setDither(true);

        setBackgroundColor(Color.WHITE);

        setLayerType(View.LAYER_TYPE_HARDWARE, null);
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

    private void drawStroke(Canvas canvas, Stroke stroke) {

        int count = stroke.points.size();

        if (count == 0) {
            return;
        }

        if (count == 1) {

            StrokePoint p = stroke.points.get(0);

            paint.setStyle(Paint.Style.FILL);

            canvas.drawCircle(
                    p.x,
                    p.y,
                    p.width * 0.5f,
                    paint
            );

            paint.setStyle(Paint.Style.STROKE);

            return;
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        for (int i = 1; i < count; i++) {

            StrokePoint p1 = stroke.points.get(i - 1);
            StrokePoint p2 = stroke.points.get(i);

            float dx = p2.x - p1.x;
            float dy = p2.y - p1.y;

            float distance =
                    (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < 0.5f) {
                continue;
            }

            float width =
                    (p1.width + p2.width) * 0.5f;

            paint.setStrokeWidth(width);

            canvas.drawLine(
                    p1.x,
                    p1.y,
                    p2.x,
                    p2.y,
                    paint
            );
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:

                currentStroke = new Stroke();
                strokes.add(currentStroke);

                lastX = event.getX();
                lastY = event.getY();

                lastPressure = getPressure(event);

                addPoint(
                        lastX,
                        lastY,
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
        }

        return true;
    }

    private void processMotion(MotionEvent event) {

        int history = event.getHistorySize();

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

        float dx = x - lastX;
        float dy = y - lastY;

        float distance =
                (float) Math.sqrt(dx * dx + dy * dy);

        /*
         * Çok yakın noktaları at.
         * Bu ciddi şekilde performansı artırır.
         */
        if (distance < 1.2f) {
            return;
        }

        /*
         * Hafif pozisyon yumuşatma.
         * Önceki 0.78 değerindeki agresif
         * smoothing yerine çok daha hızlı tepki.
         */
        float smoothing = 0.35f;

        float smoothX =
                lastX + dx * smoothing;

        float smoothY =
                lastY + dy * smoothing;

        /*
         * Basınç filtreleme.
         */
        float filteredPressure =
                lastPressure * 0.35f +
                pressure * 0.65f;

        lastPressure = filteredPressure;

        /*
         * Basınca göre kalınlık.
         */
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

        lastX = smoothX;
        lastY = smoothY;
    }

    private float getPressure(MotionEvent event) {

        float pressure = event.getPressure();

        if (pressure <= 0f) {
            pressure = 0.5f;
        }

        if (pressure > 1f) {
            pressure = 1f;
        }

        return pressure;
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
