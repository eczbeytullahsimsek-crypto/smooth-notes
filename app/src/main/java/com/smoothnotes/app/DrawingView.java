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

    private float smoothX;
    private float smoothY;

    private static final float SMOOTHING = 0.65f;
    private static final float MIN_WIDTH = 2.0f;
    private static final float MAX_WIDTH = 8.0f;

    public DrawingView(Context context) {
        super(context);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

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

                path.moveTo(smoothX, smoothY);

                updatePressure(event);

                invalidate();

                return true;

            case MotionEvent.ACTION_MOVE:

                float x = event.getX();
                float y = event.getY();

                smoothX += (x - smoothX) * SMOOTHING;
                smoothY += (y - smoothY) * SMOOTHING;

                float midX = (lastX + smoothX) / 2f;
                float midY = (lastY + smoothY) / 2f;

                path.quadTo(
                        lastX,
                        lastY,
                        midX,
                        midY
                );

                lastX = smoothX;
                lastY = smoothY;

                updatePressure(event);

                invalidate();

                return true;

            case MotionEvent.ACTION_UP:

                path.lineTo(smoothX, smoothY);

                updatePressure(event);

                invalidate();

                return true;

            default:

                return true;
        }
    }

    private void updatePressure(MotionEvent event) {

        float pressure = event.getPressure();

        if (pressure <= 0f) {
            pressure = 0.5f;
        }

        pressure = Math.max(0f, Math.min(1f, pressure));

        float width =
                MIN_WIDTH +
                (MAX_WIDTH - MIN_WIDTH) * pressure;

        paint.setStrokeWidth(width);
    }
}
