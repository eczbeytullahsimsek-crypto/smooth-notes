package com.smoothnotes.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {

    private final Paint paint;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;

    private float lastX;
    private float lastY;
    private float previousX;
    private float previousY;

    private float lastPressure = 0.5f;

    private boolean hasPreviousPoint = false;

    private static final float MIN_WIDTH = 1.8f;
    private static final float MAX_WIDTH = 6.5f;

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
        paint.setAntiAlias(true);
        paint.setDither(true);

        setBackgroundColor(Color.WHITE);

        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onSizeChanged(
            int width,
            int height,
            int oldWidth,
            int oldHeight
    ) {

        super.onSizeChanged(
                width,
                height,
                oldWidth,
                oldHeight
        );

        if (width <= 0 || height <= 0) {
            return;
        }

        Bitmap newBitmap = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
        );

        Canvas newCanvas = new Canvas(newBitmap);

        newCanvas.drawColor(Color.WHITE);

        if (bitmap != null) {
            newCanvas.drawBitmap(
                    bitmap,
                    0,
                    0,
                    null
            );

            bitmap.recycle();
        }

        bitmap = newBitmap;
        bitmapCanvas = newCanvas;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if (bitmap != null) {
            canvas.drawBitmap(
                    bitmap,
                    0,
                    0,
                    null
            );
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:

                lastX = event.getX();
                lastY = event.getY();

                previousX = lastX;
                previousY = lastY;

                lastPressure =
                        getPressure(event);

                hasPreviousPoint = false;

                drawDot(
                        lastX,
                        lastY,
                        getWidthForPressure(
                                lastPressure
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

                hasPreviousPoint = false;

                invalidate();

                return true;

            case MotionEvent.ACTION_CANCEL:

                hasPreviousPoint = false;

                return true;
        }

        return true;
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

        if (bitmapCanvas == null) {
            return;
        }

        float dx = x - lastX;
        float dy = y - lastY;

        float distance =
                (float) Math.sqrt(
                        dx * dx +
                        dy * dy
                );

        if (distance < 0.7f) {
            return;
        }

        float filteredPressure =
                lastPressure * 0.30f +
                pressure * 0.70f;

        lastPressure =
                filteredPressure;

        float width =
                getWidthForPressure(
                        filteredPressure
                );

        paint.setStrokeWidth(width);

        if (!hasPreviousPoint) {

            bitmapCanvas.drawLine(
                    lastX,
                    lastY,
                    x,
                    y,
                    paint
            );

            previousX = lastX;
            previousY = lastY;

            hasPreviousPoint = true;

        } else {

            float midX =
                    (lastX + x) * 0.5f;

            float midY =
                    (lastY + y) * 0.5f;

            Path path = new Path();

            path.moveTo(
                    previousX,
                    previousY
            );

            path.quadTo(
                    lastX,
                    lastY,
                    midX,
                    midY
            );

            bitmapCanvas.drawPath(
                    path,
                    paint
            );

            previousX = midX;
            previousY = midY;
        }

        lastX = x;
        lastY = y;
    }

    private void drawDot(
            float x,
            float y,
            float width
    ) {

        if (bitmapCanvas == null) {
            return;
        }

        paint.setStyle(Paint.Style.FILL);

        bitmapCanvas.drawCircle(
                x,
                y,
                width * 0.5f,
                paint
        );

        paint.setStyle(Paint.Style.STROKE);
    }

    private float getWidthForPressure(
            float pressure
    ) {

        pressure =
                Math.max(
                        0f,
                        Math.min(
                                1f,
                                pressure
                        )
                );

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
}
