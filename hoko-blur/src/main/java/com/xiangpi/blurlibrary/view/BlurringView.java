package com.xiangpi.blurlibrary.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.xiangpi.blurlibrary.Blur;
import com.xiangpi.blurlibrary.generator.IBlur;
import com.xiangpi.blurlibrary.generator.RenderScriptBlurGenerator;


/**
 * Created by xiangpi on 16/8/20.
 */
public class BlurringView extends View {

    private static final int DOWNSAMPLE_FACTOR = 5;

    private float mOldX;
    private float mOldY;

    private View mBlurredView;

    private Bitmap mToBlurBitmap;
    private Bitmap mBlurredBitmap;
    private Canvas mBlurringCanvas;
    private IBlur mGenerator;

    public BlurringView(Context context) {
        super(context);
        init();

    }

    public BlurringView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        mGenerator = Blur.with(getContext())
                .scheme(Blur.BlurScheme.NATIVE)
                .mode(Blur.BlurMode.GAUSSIAN)
                .radius(5)
                .sampleFactor(1.0f)
                .getBlurGenerator();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBlurredView != null) {

            if (prepare()) {

                if (mBlurredView.getBackground() != null && mBlurredView.getBackground() instanceof ColorDrawable) {
                    mToBlurBitmap.eraseColor(((ColorDrawable) mBlurredView.getBackground()).getColor());
                } else {
                    mToBlurBitmap.eraseColor(Color.TRANSPARENT);
                }

                mBlurredView.draw(mBlurringCanvas);
                mBlurredBitmap = mGenerator.doBlur(mToBlurBitmap);

                canvas.save();
                canvas.translate(mBlurredView.getX() - getX(), mBlurredView.getY() - getY());
                canvas.scale(DOWNSAMPLE_FACTOR, DOWNSAMPLE_FACTOR);
                canvas.drawBitmap(mBlurredBitmap, 0, 0, null);
                canvas.restore();
            }

        }

    }

    private boolean prepare() {
        final int width = mBlurredView.getWidth();
        final int height = mBlurredView.getHeight();

        if (mBlurringCanvas == null) {

            int scaledWidth = width / DOWNSAMPLE_FACTOR;
            int scaleHeight = height / DOWNSAMPLE_FACTOR;

            if (mToBlurBitmap == null) {
                mToBlurBitmap = Bitmap.createBitmap(scaledWidth, scaleHeight, Bitmap.Config.ARGB_8888);
            }

            if (mToBlurBitmap == null) {
                return false;
            }

            mBlurringCanvas = new Canvas(mToBlurBitmap);
            mBlurringCanvas.scale(1.0f / DOWNSAMPLE_FACTOR, 1.0f / DOWNSAMPLE_FACTOR);
        }

        return true;
    }


    public void setBlurredView(View blurredView) {
        mBlurredView = blurredView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mOldX = event.getRawX();
                mOldY = event.getRawY();
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - mOldX;
                float dy = event.getRawY() - mOldY;
                ViewCompat.offsetLeftAndRight(this, (int) dx);
                ViewCompat.offsetTopAndBottom(this, (int) dy);
                mOldX = event.getRawX();
                mOldY = event.getRawY();
                invalidate();
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                break;

        }


        return super.onTouchEvent(event);
    }



}