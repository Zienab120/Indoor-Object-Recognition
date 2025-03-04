package com.example.isee;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.camera.core.ImageAnalysis;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class OverlayView extends View {

    private List<BoundingBox> results;
    private Paint boxPaint;
    private Paint textBackgroundPaint;
    private Paint textPaint;
    private Rect bounds;
    private boolean isFrontCamera = false;
    private Yolov8TFLiteDetector detector;
    private ExecutorService cameraExecutor;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    private void initPaints() {
        textBackgroundPaint = new Paint();
        textBackgroundPaint.setColor(Color.BLACK);
        textBackgroundPaint.setStyle(Paint.Style.FILL);
        textBackgroundPaint.setTextSize(50f);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(50f);

        boxPaint = new Paint();
        boxPaint.setColor(ContextCompat.getColor(getContext(), R.color.black));
        boxPaint.setStrokeWidth(8F);
        boxPaint.setStyle(Paint.Style.STROKE);

        bounds = new Rect();
    }

    public void clear() {
        textPaint.reset();
        textBackgroundPaint.reset();
        boxPaint.reset();
        invalidate();
        initPaints();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (results != null) {
            for (BoundingBox box : results) {
                float left = box.getX1() * getWidth();
                float top = box.getY1() * getHeight();
                float right = box.getX2() * getWidth();
                float bottom = box.getY2() * getHeight();

                canvas.drawRect(left, top, right, bottom, boxPaint);
                String drawableText = box.getClsName();

                textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length(), bounds);
                float textWidth = bounds.width();
                float textHeight = bounds.height();
                canvas.drawRect(left, top, left + textWidth + BOUNDING_RECT_TEXT_PADDING, top + textHeight + BOUNDING_RECT_TEXT_PADDING, textBackgroundPaint);
                canvas.drawText(drawableText, left, top + bounds.height(), textPaint);
            }
        }
    }

    public void setResults(List<BoundingBox> boundingBoxes) {
        results = boundingBoxes;
        invalidate();
    }

    public void setAnalyzer(ImageAnalysis imageAnalyzer) {
        imageAnalyzer.setAnalyzer(cameraExecutor, imageProxy -> {
            Bitmap bitmapBuffer = Bitmap.createBitmap(imageProxy.getWidth(), imageProxy.getHeight(), Bitmap.Config.ARGB_8888);
            imageProxy.getPlanes()[0].getBuffer().rewind(); // Ensure the buffer is at the beginning
            bitmapBuffer.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());

            Matrix matrix = new Matrix();
            matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());

            if (isFrontCamera) {
                matrix.postScale(-1f, 1f, imageProxy.getWidth() / 2f, imageProxy.getHeight() / 2f);
            }

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapBuffer, 0, 0, bitmapBuffer.getWidth(), bitmapBuffer.getHeight(), matrix, true);

            detector.detect(rotatedBitmap);

            imageProxy.close();
        });
    }

    private static final int BOUNDING_RECT_TEXT_PADDING = 8;
}
