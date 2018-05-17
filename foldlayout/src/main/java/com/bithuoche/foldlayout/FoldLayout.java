package com.bithuoche.foldlayout;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class FoldLayout extends FrameLayout {

    private FoldDrawer left;
    private FoldDrawer right;
    private float foldDegree = 180f;
    private boolean foldBySelf = true;
    private boolean clipCoverPart = false;

    public FoldLayout(Context context) {
        super(context);
    }

    public FoldLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FoldLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setFoldDegree(float foldDegree) {
        this.foldDegree = foldDegree;
        postInvalidate();
    }

    public void config(boolean foldBySelf, boolean clipCoverPart) {
        this.foldBySelf = foldBySelf;
        this.clipCoverPart = clipCoverPart;
        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float scale = getContext().getResources().getDisplayMetrics().density;
        DrawDelegate drawDelegate = new DrawDelegate() {
            @Override
            public void draw(Canvas canvas) {
                FoldLayout.super.dispatchDraw(canvas);
            }
        };

        left = new FoldDrawer(drawDelegate, scale, clipCoverPart);
        left.setFirst(getWidth() / 2, 0, getWidth(), getHeight());
        left.setSecond(0, 0, getWidth() / 2, getHeight());

        right = new FoldDrawer(drawDelegate, scale, clipCoverPart);
        right.setFirst(0, 0, getWidth() / 2, getHeight());
        right.setSecond(getWidth() / 2, 0, getWidth(), getHeight());
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        foldDegree = Math.max(0, Math.min(360, foldDegree));
        if (foldDegree > 180 && left != null) {
            left.flip(canvas, foldDegree - 180);
        } else if (foldDegree < 180 && right != null) {
            right.flip(canvas, foldDegree - 180);
        } else {
            super.dispatchDraw(canvas);
        }
    }

    private float lastX = Float.MIN_VALUE;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!foldBySelf) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (lastX != Float.MIN_VALUE) {
                    foldDegree += (event.getX() - lastX) * 180 / getWidth();
                    postInvalidate();
                }
                lastX = event.getX();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (lastX != Float.MIN_VALUE) {
                    foldDegree += (event.getX() - lastX) * 180 / getWidth();
                    postInvalidate();
                }
                lastX = Float.MIN_VALUE;
                break;
        }
        return true;
    }

    public interface DrawDelegate {
        void draw(Canvas canvas);
    }

    private static class FoldDrawer {
        private final DrawDelegate drawDelegate;
        private final float scale;
        private final boolean clipCoverPart;
        private Rect first = new Rect();
        private Rect second = new Rect();
        private Camera camera = new Camera();
        private Matrix matrix = new Matrix();

        public FoldDrawer(DrawDelegate drawDelegate, float scale, boolean clipCoverPart) {
            this.drawDelegate = drawDelegate;
            this.scale = scale * 2;
            this.clipCoverPart = clipCoverPart;
        }

        public void setFirst(int left, int top, int right, int bottom) {
            first.set(left, top, right, bottom);
        }

        public void setSecond(int left, int top, int right, int bottom) {
            second.set(left, top, right, bottom);
        }

        private void flip(Canvas canvas, float degree) {
            if (clipCoverPart && (degree <= -90 || degree >= 90)) {
                double cos = Math.cos(Math.toRadians(degree));
                double over = Math.abs(cos * second.width());
                canvas.save();
                int left = first.left + (degree >= 90 ? (int) over : 0);
                int right = first.right - (degree <= -90 ? (int) over : 0);
                canvas.clipRect(left, first.top, right, first.bottom);
                drawDelegate.draw(canvas);
                canvas.restore();
                return;
            }

            canvas.save();
            canvas.clipRect(first);
            drawDelegate.draw(canvas);
            canvas.restore();

            canvas.save();
            camera.save();
            camera.rotateY(degree);
            camera.getMatrix(matrix);
            camera.restore();

            float centerX = (first.centerX() + second.centerX()) / 2;
            float centerY = (second.centerY() + second.centerY()) / 2;

            float[] mValues = new float[9];
            matrix.getValues(mValues);
            mValues[6] = mValues[6] / scale;
            mValues[7] = mValues[7] / scale;
            matrix.setValues(mValues);

            matrix.preTranslate(-centerX, -centerY);
            matrix.postTranslate(centerX, centerY);
            canvas.concat(matrix);
            canvas.clipRect(second);
            drawDelegate.draw(canvas);
            canvas.restore();
        }
    }
}
