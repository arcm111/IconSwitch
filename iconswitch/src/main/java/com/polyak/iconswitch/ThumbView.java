package com.polyak.iconswitch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by yarolegovich on 31.03.2017.
 * https://github.com/yarolegovich
 */
class ThumbView extends View {

    private final Paint paint;
    private final RectF bounds;

    public ThumbView(Context context) {
        super(context);
    }

    public ThumbView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThumbView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ThumbView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GRAY);
        bounds = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bounds.set(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float radius = Math.min(bounds.height(), bounds.width()) * 0.5f;
        canvas.drawRoundRect(bounds, radius, radius, paint);
    }

    public void setColor(int color) {
        paint.setColor(color);
        invalidate();
    }

    public int getColor() {
        return paint.getColor();
    }
}
